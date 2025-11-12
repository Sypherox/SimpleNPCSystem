package Sypherox.manager;

import Sypherox.NPCSystem;
import Sypherox.data.NPC;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {

    private final NPCSystem plugin;
    private final StorageType storageType;
    private HikariDataSource dataSource;
    private File npcsFile;

    public DataManager(NPCSystem plugin) {
        this.plugin = plugin;
        String type = plugin.getConfig().getString("storage.type", "YAML").toUpperCase();
        this.storageType = StorageType.valueOf(type);

        if (storageType == StorageType.MYSQL) {
            setupMySQL();
        } else {
            setupYAML();
        }
    }

    private void setupYAML() {
        npcsFile = new File(plugin.getDataFolder(), "npcs.yml");
        if (!npcsFile.exists()) {
            try {
                npcsFile.createNewFile();
                plugin.getLogger().info("Created npcs.yml file");
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to create npcs.yml: " + e.getMessage());
            }
        }
    }

    private void setupMySQL() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" +
                    plugin.getConfig().getString("storage.mysql.host") + ":" +
                    plugin.getConfig().getInt("storage.mysql.port") + "/" +
                    plugin.getConfig().getString("storage.mysql.database"));
            config.setUsername(plugin.getConfig().getString("storage.mysql.username"));
            config.setPassword(plugin.getConfig().getString("storage.mysql.password"));
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            dataSource = new HikariDataSource(config);
            createTables();
            plugin.getLogger().info("MySQL connection pool established");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to setup MySQL: " + e.getMessage());
        }
    }

    private void createTables() {
        String createNPCTable = "CREATE TABLE IF NOT EXISTS npcs (" +
                "id VARCHAR(64) PRIMARY KEY," +
                "world VARCHAR(255) NOT NULL," +
                "x DOUBLE NOT NULL," +
                "y DOUBLE NOT NULL," +
                "z DOUBLE NOT NULL," +
                "yaw FLOAT NOT NULL," +
                "pitch FLOAT NOT NULL," +
                "skin_texture TEXT," +
                "skin_signature TEXT," +
                "mirror_skin BOOLEAN DEFAULT FALSE," +
                "action VARCHAR(32) DEFAULT 'NONE'," +
                "action_value TEXT," +
                "eye_contact BOOLEAN DEFAULT FALSE" +
                ")";

        String createHologramTable = "CREATE TABLE IF NOT EXISTS npc_holograms (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "npc_id VARCHAR(64) NOT NULL," +
                "line_index INT NOT NULL," +
                "text TEXT NOT NULL," +
                "FOREIGN KEY (npc_id) REFERENCES npcs(id) ON DELETE CASCADE" +
                ")";

        try (Connection conn = dataSource.getConnection()) {
            conn.createStatement().execute(createNPCTable);
            conn.createStatement().execute(createHologramTable);
            plugin.getLogger().info("MySQL tables created successfully");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create tables: " + e.getMessage());
        }
    }

    public Map<String, NPC> loadNPCs() {
        if (storageType == StorageType.MYSQL) {
            return loadNPCsFromMySQL();
        } else {
            return loadNPCsFromYAML();
        }
    }

    private Map<String, NPC> loadNPCsFromYAML() {
        Map<String, NPC> npcs = new ConcurrentHashMap<>();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(npcsFile);

        for (String key : config.getKeys(false)) {
            try {
                String world = config.getString(key + ".location.world");
                double x = config.getDouble(key + ".location.x");
                double y = config.getDouble(key + ".location.y");
                double z = config.getDouble(key + ".location.z");
                float yaw = (float) config.getDouble(key + ".location.yaw");
                float pitch = (float) config.getDouble(key + ".location.pitch");

                Location location = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
                NPC npc = new NPC(key, location);

                npc.setSkinTexture(config.getString(key + ".skin.texture"));
                npc.setSkinSignature(config.getString(key + ".skin.signature"));
                npc.setMirrorSkin(config.getBoolean(key + ".skin.mirror", false));
                npc.setEyeContact(config.getBoolean(key + ".eye_contact", false));

                List<String> hologramLines = config.getStringList(key + ".hologram");
                npc.setHologramLines(hologramLines);

                String actionStr = config.getString(key + ".action.type", "NONE");
                npc.setAction(NPC.NPCAction.valueOf(actionStr));
                npc.setActionValue(config.getString(key + ".action.value", ""));

                npcs.put(key, npc);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load NPC " + key + ": " + e.getMessage());
            }
        }

        return npcs;
    }

    private Map<String, NPC> loadNPCsFromMySQL() {
        Map<String, NPC> npcs = new ConcurrentHashMap<>();

        String query = "SELECT * FROM npcs";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("id");
                String world = rs.getString("world");
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double z = rs.getDouble("z");
                float yaw = rs.getFloat("yaw");
                float pitch = rs.getFloat("pitch");

                Location location = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
                NPC npc = new NPC(id, location);

                npc.setSkinTexture(rs.getString("skin_texture"));
                npc.setSkinSignature(rs.getString("skin_signature"));
                npc.setMirrorSkin(rs.getBoolean("mirror_skin"));
                npc.setEyeContact(rs.getBoolean("eye_contact"));
                npc.setAction(NPC.NPCAction.valueOf(rs.getString("action")));
                npc.setActionValue(rs.getString("action_value"));

                List<String> hologramLines = loadHologramLines(id);
                npc.setHologramLines(hologramLines);

                npcs.put(id, npc);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load NPCs from MySQL: " + e.getMessage());
        }

        return npcs;
    }

    private List<String> loadHologramLines(String npcId) {
        List<String> lines = new ArrayList<>();
        String query = "SELECT text FROM npc_holograms WHERE npc_id = ? ORDER BY line_index";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, npcId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lines.add(rs.getString("text"));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load hologram for NPC " + npcId);
        }

        return lines;
    }

    public void saveNPCs(Map<String, NPC> npcs) {
        if (storageType == StorageType.MYSQL) {
            saveNPCsToMySQL(npcs);
        } else {
            saveNPCsToYAML(npcs);
        }
    }

    private void saveNPCsToYAML(Map<String, NPC> npcs) {
        YamlConfiguration config = new YamlConfiguration();

        for (Map.Entry<String, NPC> entry : npcs.entrySet()) {
            String key = entry.getKey();
            NPC npc = entry.getValue();
            Location loc = npc.getLocation();

            config.set(key + ".location.world", loc.getWorld().getName());
            config.set(key + ".location.x", loc.getX());
            config.set(key + ".location.y", loc.getY());
            config.set(key + ".location.z", loc.getZ());
            config.set(key + ".location.yaw", loc.getYaw());
            config.set(key + ".location.pitch", loc.getPitch());

            config.set(key + ".skin.texture", npc.getSkinTexture());
            config.set(key + ".skin.signature", npc.getSkinSignature());
            config.set(key + ".skin.mirror", npc.isMirrorSkin());
            config.set(key + ".eye_contact", npc.hasEyeContact());

            config.set(key + ".hologram", npc.getHologramLines());

            config.set(key + ".action.type", npc.getAction().name());
            config.set(key + ".action.value", npc.getActionValue());
        }

        try {
            config.save(npcsFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save NPCs to YAML: " + e.getMessage());
        }
    }

    private void saveNPCsToMySQL(Map<String, NPC> npcs) {
        String deleteNPC = "DELETE FROM npcs WHERE id = ?";
        String insertNPC = "INSERT INTO npcs (id, world, x, y, z, yaw, pitch, skin_texture, " +
                "skin_signature, mirror_skin, action, action_value, eye_contact) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String deleteHolograms = "DELETE FROM npc_holograms WHERE npc_id = ?";
        String insertHologram = "INSERT INTO npc_holograms (npc_id, line_index, text) VALUES (?, ?, ?)";

        try (Connection conn = dataSource.getConnection()) {
            for (Map.Entry<String, NPC> entry : npcs.entrySet()) {
                NPC npc = entry.getValue();
                Location loc = npc.getLocation();

                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteNPC)) {
                    deleteStmt.setString(1, npc.getId());
                    deleteStmt.executeUpdate();
                }

                try (PreparedStatement insertStmt = conn.prepareStatement(insertNPC)) {
                    insertStmt.setString(1, npc.getId());
                    insertStmt.setString(2, loc.getWorld().getName());
                    insertStmt.setDouble(3, loc.getX());
                    insertStmt.setDouble(4, loc.getY());
                    insertStmt.setDouble(5, loc.getZ());
                    insertStmt.setFloat(6, loc.getYaw());
                    insertStmt.setFloat(7, loc.getPitch());
                    insertStmt.setString(8, npc.getSkinTexture());
                    insertStmt.setString(9, npc.getSkinSignature());
                    insertStmt.setBoolean(10, npc.isMirrorSkin());
                    insertStmt.setString(11, npc.getAction().name());
                    insertStmt.setString(12, npc.getActionValue());
                    insertStmt.setBoolean(13, npc.hasEyeContact());
                    insertStmt.executeUpdate();
                }

                try (PreparedStatement deleteHoloStmt = conn.prepareStatement(deleteHolograms)) {
                    deleteHoloStmt.setString(1, npc.getId());
                    deleteHoloStmt.executeUpdate();
                }

                List<String> lines = npc.getHologramLines();
                for (int i = 0; i < lines.size(); i++) {
                    try (PreparedStatement insertHoloStmt = conn.prepareStatement(insertHologram)) {
                        insertHoloStmt.setString(1, npc.getId());
                        insertHoloStmt.setInt(2, i);
                        insertHoloStmt.setString(3, lines.get(i));
                        insertHoloStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save NPCs to MySQL: " + e.getMessage());
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("MySQL connection pool closed");
        }
    }

    public enum StorageType {
        YAML,
        MYSQL
    }
}

package Sypherox;

import Sypherox.commands.NPCCommand;
import Sypherox.listeners.NPCInteractListener;
import Sypherox.manager.DataManager;
import Sypherox.manager.NPCManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class NPCSystem extends JavaPlugin {

    private static NPCSystem instance;
    private NPCManager npcManager;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        getLogger().info("✅ Configuration loaded successfully");

        this.dataManager = new DataManager(this);
        getLogger().info("✅ DataManager initialized");

        this.npcManager = new NPCManager(this);
        getLogger().info("✅ NPCManager initialized");

        if (getCommand("npc") != null) {
            getCommand("npc").setExecutor(new NPCCommand(this));
            getLogger().info("✅ Command /npc registered successfully");
        } else {
            getLogger().severe("❌ Failed to register command 'npc' - check plugin.yml");
        }

        getServer().getPluginManager().registerEvents(new NPCInteractListener(this), this);
        getLogger().info("✅ NPC interaction listener registered");

        npcManager.loadAllNPCs();
        getLogger().info("✅  All NPCs loaded successfully");

        getLogger().info("===========================");
        getLogger().info("✅ NPC System v" + getDescription().getVersion() + " enabled");
        getLogger().info("===========================");
    }

    @Override
    public void onDisable() {
        getLogger().info("⭐ Saving all NPCs...");

        if (npcManager != null) {
            npcManager.saveAllNPCs();
            npcManager.removeAllNPCs();
            getLogger().info("✅ All NPCs saved and removed");
        }

        if (dataManager != null) {
            dataManager.close();
            getLogger().info("❌ Database connection closed");
        }

        instance = null;

        getLogger().info("===========================");
        getLogger().info("❌ NPC System disabled");
        getLogger().info("===========================");
    }

    public static NPCSystem getInstance() {
        return instance;
    }

    public NPCManager getNPCManager() {
        return npcManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}

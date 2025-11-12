package Sypherox.manager;

import Sypherox.NPCSystem;
import Sypherox.data.NPC;
import Sypherox.util.SkinFetcher;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.util.Vector;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NPCManager {

    private final NPCSystem plugin;
    private final Map<String, NPC> npcs;
    private final Map<String, UUID> mannequinEntities;
    private final Map<String, List<UUID>> hologramEntities;

    public NPCManager(NPCSystem plugin) {
        this.plugin = plugin;
        this.npcs = new ConcurrentHashMap<>();
        this.mannequinEntities = new ConcurrentHashMap<>();
        this.hologramEntities = new ConcurrentHashMap<>();
        startEyeContactTask();
    }

    public NPC createNPC(String id, Location location) {
        if (npcs.containsKey(id)) {
            return null;
        }

        NPC npc = new NPC(id, location);
        npcs.put(id, npc);
        spawnNPC(npc);
        return npc;
    }

    public void spawnNPC(NPC npc) {
        Location loc = npc.getLocation();
        if (loc.getWorld() == null) {
            return;
        }

        Mannequin mannequin = (Mannequin) loc.getWorld().spawnEntity(loc, EntityType.MANNEQUIN);
        mannequin.setCustomName(npc.getId());
        mannequin.setCustomNameVisible(false);
        mannequin.setPersistent(true);
        mannequin.setInvulnerable(true);
        mannequin.setCollidable(false);
        mannequin.setGravity(false);

        if (npc.getSkinTexture() != null && npc.getSkinSignature() != null) {
            applySkinTexture(mannequin, npc.getSkinTexture(), npc.getSkinSignature());
        }

        mannequinEntities.put(npc.getId(), mannequin.getUniqueId());
        spawnHologram(npc);
    }

    private void applySkinTexture(Mannequin mannequin, String textureValue, String signatureValue) {
        try {
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();

            String decodedTexture = new String(Base64.getDecoder().decode(textureValue));
            String textureUrl = extractTextureUrl(decodedTexture);

            if (textureUrl != null) {
                textures.setSkin(new URL(textureUrl));
                profile.setTextures(textures);
                mannequin.setPlayerProfile(profile);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to apply skin texture: " + e.getMessage());
        }
    }

    private String extractTextureUrl(String decodedJson) {
        try {
            int urlStart = decodedJson.indexOf("\"url\" : \"") + 9;
            int urlEnd = decodedJson.indexOf("\"", urlStart);
            return decodedJson.substring(urlStart, urlEnd);
        } catch (Exception e) {
            return null;
        }
    }

    public void updateNPCSkin(String npcId, String playerName) {
        NPC npc = npcs.get(npcId);
        if (npc == null) return;

        plugin.getLogger().info("Fetching skin for player: " + playerName);

        SkinFetcher.fetchSkin(playerName).thenAccept(skinData -> {
            if (skinData == null) {
                plugin.getLogger().warning("Could not fetch skin for player: " + playerName);
                return;
            }

            npc.setSkinTexture(skinData.getTexture());
            npc.setSkinSignature(skinData.getSignature());
            npc.setMirrorSkin(false);

            Bukkit.getScheduler().runTask(plugin, () -> {
                Mannequin mannequin = getMannequin(npcId);
                if (mannequin != null) {
                    applySkinTexture(mannequin, skinData.getTexture(), skinData.getSignature());
                    plugin.getLogger().info("Applied skin for NPC: " + npcId);
                }
            });
        });
    }

    private void startEyeContactTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (NPC npc : npcs.values()) {
                if (!npc.hasEyeContact()) continue;

                Mannequin mannequin = getMannequin(npc.getId());
                if (mannequin == null) continue;

                Player nearest = getNearestPlayer(mannequin.getLocation(), 10.0);
                if (nearest != null) {
                    lookAt(mannequin, nearest.getEyeLocation());
                }
            }
        }, 0L, 2L);
    }

    private Player getNearestPlayer(Location location, double maxDistance) {
        Player nearest = null;
        double minDistance = maxDistance * maxDistance;

        for (Player player : location.getWorld().getPlayers()) {
            double distance = player.getLocation().distanceSquared(location);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = player;
            }
        }

        return nearest;
    }

    private void lookAt(Mannequin mannequin, Location target) {
        Location entityLoc = mannequin.getLocation();
        Vector direction = target.toVector().subtract(entityLoc.toVector()).normalize();

        double x = direction.getX();
        double z = direction.getZ();
        double y = direction.getY();

        double yaw = Math.toDegrees(Math.atan2(-x, z));
        double pitch = Math.toDegrees(Math.atan(-y / Math.sqrt(x * x + z * z)));

        Location newLoc = entityLoc.clone();
        newLoc.setYaw((float) yaw);
        newLoc.setPitch((float) pitch);

        mannequin.teleport(newLoc);
    }

    private void spawnHologram(NPC npc) {
        if (npc.getHologramLines().isEmpty()) {
            return;
        }

        removeHologram(npc.getId());

        Location loc = npc.getLocation().clone();
        List<UUID> hologramIds = new ArrayList<>();

        double lineHeight = 0.3;
        double startHeight = 2.0 + (npc.getHologramLines().size() - 1) * lineHeight;

        for (int i = 0; i < npc.getHologramLines().size(); i++) {
            String line = npc.getHologramLines().get(i);
            Location textLoc = loc.clone().add(0, startHeight - (i * lineHeight), 0);

            TextDisplay textDisplay = loc.getWorld().spawn(textLoc, TextDisplay.class);
            textDisplay.setText(line);
            textDisplay.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
            textDisplay.setSeeThrough(true);

            hologramIds.add(textDisplay.getUniqueId());
        }

        hologramEntities.put(npc.getId(), hologramIds);
    }

    public void updateHologram(String npcId) {
        NPC npc = npcs.get(npcId);
        if (npc != null) {
            spawnHologram(npc);
        }
    }

    private void removeHologram(String npcId) {
        List<UUID> hologramIds = hologramEntities.get(npcId);
        if (hologramIds != null) {
            for (UUID id : hologramIds) {
                Bukkit.getWorlds().forEach(world -> {
                    world.getEntities().stream()
                            .filter(e -> e.getUniqueId().equals(id))
                            .forEach(org.bukkit.entity.Entity::remove);
                });
            }
            hologramEntities.remove(npcId);
        }
    }

    public void removeNPC(String id) {
        NPC npc = npcs.remove(id);
        if (npc == null) return;

        Mannequin mannequin = getMannequin(id);
        if (mannequin != null) {
            mannequin.remove();
        }

        removeHologram(id);
        mannequinEntities.remove(id);
    }

    public Mannequin getMannequin(String npcId) {
        UUID entityId = mannequinEntities.get(npcId);
        if (entityId == null) return null;

        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (org.bukkit.entity.Entity entity : world.getEntities()) {
                if (entity.getUniqueId().equals(entityId) && entity instanceof Mannequin) {
                    return (Mannequin) entity;
                }
            }
        }
        return null;
    }

    public NPC getNPC(String id) {
        return npcs.get(id);
    }

    public NPC getNPCByMannequin(UUID entityId) {
        for (Map.Entry<String, UUID> entry : mannequinEntities.entrySet()) {
            if (entry.getValue().equals(entityId)) {
                return npcs.get(entry.getKey());
            }
        }
        return null;
    }

    public Collection<NPC> getAllNPCs() {
        return new ArrayList<>(npcs.values());
    }

    public void loadAllNPCs() {
        Map<String, NPC> loadedNPCs = plugin.getDataManager().loadNPCs();
        for (NPC npc : loadedNPCs.values()) {
            npcs.put(npc.getId(), npc);
            spawnNPC(npc);
        }
        plugin.getLogger().info("Loaded " + npcs.size() + " NPCs");
    }

    public void saveAllNPCs() {
        plugin.getDataManager().saveNPCs(npcs);
        plugin.getLogger().info("Saved " + npcs.size() + " NPCs");
    }

    public void removeAllNPCs() {
        for (String id : new ArrayList<>(npcs.keySet())) {
            removeNPC(id);
        }
    }
}

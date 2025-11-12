package Sypherox.listeners;

import Sypherox.NPCSystem;
import Sypherox.data.NPC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class NPCInteractListener implements Listener {

    private final NPCSystem plugin;

    public NPCInteractListener(NPCSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNPCInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Mannequin)) {
            return;
        }

        Mannequin mannequin = (Mannequin) event.getRightClicked();
        Player player = event.getPlayer();

        NPC npc = plugin.getNPCManager().getNPCByMannequin(mannequin.getUniqueId());
        if (npc == null) {
            return;
        }

        event.setCancelled(true);

        switch (npc.getAction()) {
            case COMMAND:
                String command = npc.getActionValue().replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                break;

            case GUI:
                player.sendMessage("§cGUI system not implemented yet");
                break;

            case SERVER:
                if (plugin.getServer().getPluginManager().isPluginEnabled("BungeeCord")) {
                    sendToServer(player, npc.getActionValue());
                } else {
                    player.sendMessage("§cBungeeCord not enabled");
                }
                break;

            case NONE:
            default:
                break;
        }
    }

    private void sendToServer(Player player, String server) {
        com.google.common.io.ByteArrayDataOutput out = com.google.common.io.ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }
}

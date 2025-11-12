package Sypherox.commands;

import Sypherox.NPCSystem;
import Sypherox.data.NPC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NPCCommand implements CommandExecutor, TabCompleter {

    private final NPCSystem plugin;

    public NPCCommand(NPCSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                handleCreate(player, args);
                break;

            case "delete":
            case "remove":
                handleDelete(player, args);
                break;

            case "skin":
                handleSkin(player, args);
                break;

            case "hologram":
            case "holo":
                handleHologram(player, args);
                break;

            case "action":
                handleAction(player, args);
                break;

            case "eyecontact":
            case "eye":
                handleEyeContact(player, args);
                break;

            case "list":
                handleList(player);
                break;

            case "teleport":
            case "tp":
                handleTeleport(player, args);
                break;

            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /npc create <id>");
            return;
        }

        String id = args[1];

        if (plugin.getNPCManager().getNPC(id) != null) {
            player.sendMessage("§cNPC with ID '" + id + "' already exists");
            return;
        }

        NPC npc = plugin.getNPCManager().createNPC(id, player.getLocation());
        if (npc != null) {
            player.sendMessage("§aNPC '" + id + "' created successfully");
        } else {
            player.sendMessage("§cFailed to create NPC");
        }
    }

    private void handleDelete(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /npc delete <id>");
            return;
        }

        String id = args[1];

        if (plugin.getNPCManager().getNPC(id) == null) {
            player.sendMessage("§cNPC with ID '" + id + "' does not exist");
            return;
        }

        plugin.getNPCManager().removeNPC(id);
        player.sendMessage("§aNPC '" + id + "' deleted successfully");
    }

    private void handleSkin(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage: /npc skin <id> <player|mirror>");
            return;
        }

        String id = args[1];
        NPC npc = plugin.getNPCManager().getNPC(id);

        if (npc == null) {
            player.sendMessage("§cNPC with ID '" + id + "' does not exist");
            return;
        }

        String skinTarget = args[2];

        if (skinTarget.equalsIgnoreCase("mirror")) {
            npc.setMirrorSkin(true);
            player.sendMessage("§aNPC '" + id + "' will now mirror player skins");
        } else {
            player.sendMessage("§eFetching skin for player: " + skinTarget);
            plugin.getNPCManager().updateNPCSkin(id, skinTarget);
            player.sendMessage("§aSkin applied to NPC '" + id + "'");
        }
    }

    private void handleHologram(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage: /npc hologram <id> <add|remove|clear> [text/index]");
            return;
        }

        String id = args[1];
        NPC npc = plugin.getNPCManager().getNPC(id);

        if (npc == null) {
            player.sendMessage("§cNPC with ID '" + id + "' does not exist");
            return;
        }

        String action = args[2].toLowerCase();

        switch (action) {
            case "add":
                if (args.length < 4) {
                    player.sendMessage("§cUsage: /npc hologram <id> add <text>");
                    return;
                }

                String text = String.join(" ", Arrays.copyOfRange(args, 3, args.length))
                        .replace("&", "§");

                if (npc.getHologramLines().size() >= 3) {
                    player.sendMessage("§cHologram already has maximum of 3 lines");
                    return;
                }

                npc.addHologramLine(text);
                plugin.getNPCManager().updateHologram(id);
                player.sendMessage("§aHologram line added to NPC '" + id + "'");
                break;

            case "remove":
                if (args.length < 4) {
                    player.sendMessage("§cUsage: /npc hologram <id> remove <line_index>");
                    return;
                }

                try {
                    int index = Integer.parseInt(args[3]) - 1;
                    npc.removeHologramLine(index);
                    plugin.getNPCManager().updateHologram(id);
                    player.sendMessage("§aHologram line removed from NPC '" + id + "'");
                } catch (NumberFormatException e) {
                    player.sendMessage("§cInvalid line index");
                }
                break;

            case "clear":
                npc.clearHologramLines();
                plugin.getNPCManager().updateHologram(id);
                player.sendMessage("§aHologram cleared for NPC '" + id + "'");
                break;

            default:
                player.sendMessage("§cUsage: /npc hologram <id> <add|remove|clear> [text/index]");
                break;
        }
    }

    private void handleAction(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage("§cUsage: /npc action <id> <none|command|gui|server> <value>");
            return;
        }

        String id = args[1];
        NPC npc = plugin.getNPCManager().getNPC(id);

        if (npc == null) {
            player.sendMessage("§cNPC with ID '" + id + "' does not exist");
            return;
        }

        String actionType = args[2].toUpperCase();

        try {
            NPC.NPCAction action = NPC.NPCAction.valueOf(actionType);

            if (action == NPC.NPCAction.NONE) {
                npc.setAction(NPC.NPCAction.NONE);
                npc.setActionValue("");
                player.sendMessage("§aAction removed from NPC '" + id + "'");
            } else {
                String value = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                npc.setAction(action);
                npc.setActionValue(value);
                player.sendMessage("§aAction set for NPC '" + id + "': " + action + " -> " + value);
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid action type. Use: none, command, gui, server");
        }
    }

    private void handleEyeContact(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage: /npc eyecontact <id> <true|false>");
            return;
        }

        String id = args[1];
        NPC npc = plugin.getNPCManager().getNPC(id);

        if (npc == null) {
            player.sendMessage("§cNPC with ID '" + id + "' does not exist");
            return;
        }

        boolean enabled = Boolean.parseBoolean(args[2]);
        npc.setEyeContact(enabled);
        player.sendMessage("§aEye contact " + (enabled ? "enabled" : "disabled") + " for NPC '" + id + "'");
    }

    private void handleList(Player player) {
        if (plugin.getNPCManager().getAllNPCs().isEmpty()) {
            player.sendMessage("§eNo NPCs exist");
            return;
        }

        player.sendMessage("§6§l=== NPC List ===");
        for (NPC npc : plugin.getNPCManager().getAllNPCs()) {
            player.sendMessage("§e- §f" + npc.getId() + " §7(Action: " + npc.getAction() + ")");
        }
    }

    private void handleTeleport(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /npc teleport <id>");
            return;
        }

        String id = args[1];
        NPC npc = plugin.getNPCManager().getNPC(id);

        if (npc == null) {
            player.sendMessage("§cNPC with ID '" + id + "' does not exist");
            return;
        }

        player.teleport(npc.getLocation());
        player.sendMessage("§aTeleported to NPC '" + id + "'");
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6§l=== NPC Commands ===");
        player.sendMessage("§e/npc create <id> §7- Create a new NPC");
        player.sendMessage("§e/npc delete <id> §7- Delete an NPC");
        player.sendMessage("§e/npc skin <id> <player|mirror> §7- Set NPC skin");
        player.sendMessage("§e/npc hologram <id> <add|remove|clear> §7- Manage hologram");
        player.sendMessage("§e/npc action <id> <type> <value> §7- Set NPC action");
        player.sendMessage("§e/npc eyecontact <id> <true|false> §7- Toggle eye contact");
        player.sendMessage("§e/npc list §7- List all NPCs");
        player.sendMessage("§e/npc teleport <id> §7- Teleport to NPC");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "delete", "skin", "hologram", "action",
                    "eyecontact", "list", "teleport"));
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create")) {
                return completions;
            }

            return plugin.getNPCManager().getAllNPCs().stream()
                    .map(NPC::getId)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3) {
            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "hologram":
                case "holo":
                    completions.addAll(Arrays.asList("add", "remove", "clear"));
                    break;

                case "action":
                    completions.addAll(Arrays.asList("none", "command", "gui", "server"));
                    break;

                case "eyecontact":
                case "eye":
                    completions.addAll(Arrays.asList("true", "false"));
                    break;

                case "skin":
                    completions.add("mirror");
                    break;
            }

            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return completions;
    }
}

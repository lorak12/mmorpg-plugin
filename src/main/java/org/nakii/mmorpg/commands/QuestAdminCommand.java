package org.nakii.mmorpg.commands;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.QuestManager;
import org.nakii.mmorpg.quest.data.ActiveObjective;
import org.nakii.mmorpg.quest.data.PlayerQuestData;
import org.nakii.mmorpg.quest.model.NPCVisibilityRule;
import org.nakii.mmorpg.quest.model.QuestPackage;
import org.nakii.mmorpg.util.ChatUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class QuestAdminCommand implements CommandExecutor, TabCompleter {

    private final MMORPGCore plugin;
    private final QuestManager questManager;
    private final String noPerms = "<red>You do not have permission to use this command.</red>";
    private final String prefix = "<dark_gray>[<gold>QuestAdmin<dark_gray>] ";

    public QuestAdminCommand(MMORPGCore plugin) {
        this.plugin = plugin;
        this.questManager = plugin.getQuestManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                handleReload(sender);
                break;
            case "player":
                handlePlayer(sender, args);
                break;
            case "npc":
                handleNpc(sender, args);
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("mmorpg.questadmin.reload")) {
            sender.sendMessage(ChatUtils.format(prefix + noPerms));
            return;
        }

        // --- START OF MODIFICATION ---

        // 1. Reload all quest configurations from disk
        questManager.loadQuests();
        plugin.getHologramManager().reload();

        // 2. Force a visibility update for all currently online players
        // This ensures that any changes to npc_visibility rules are applied immediately.
        if (plugin.getNpcVisibilityManager() != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                plugin.getNpcVisibilityManager().forceFullUpdateForPlayer(player);
            }
        }

        // --- END OF MODIFICATION ---

        sender.sendMessage(ChatUtils.format(prefix + "<green>All quest packages, holograms, and visibility rules have been reloaded from disk and applied to online players.</green>"));
    }

    private void handlePlayer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mmorpg.questadmin.player")) {
            sender.sendMessage(ChatUtils.format(prefix + noPerms));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatUtils.format(prefix + "<red>Usage: /questadmin player <name> <view|tag|objective|event> ...</red>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatUtils.format(prefix + "<red>Player '" + args[1] + "' is not online.</red>"));
            return;
        }

        String action = args[2].toLowerCase();
        switch (action) {
            case "view":
                handlePlayerView(sender, target);
                break;
            case "tag":
                handlePlayerTag(sender, target, Arrays.copyOfRange(args, 3, args.length));
                break;
            case "objective":
                handlePlayerObjective(sender, target, Arrays.copyOfRange(args, 3, args.length));
                break;
            case "event":
                handlePlayerEvent(sender, target, Arrays.copyOfRange(args, 3, args.length));
                break;
            default:
                sender.sendMessage(ChatUtils.format(prefix + "<red>Unknown player action. Use view, tag, objective, or event.</red>"));
                break;
        }
    }

    private void handlePlayerView(CommandSender sender, Player target) {
        PlayerQuestData data = questManager.getPlayerData(target);
        if (data == null) {
            sender.sendMessage(ChatUtils.format(prefix + "<red>Could not load quest data for " + target.getName() + ".</red>"));
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<gold><st>----------------</st>[ Quest Data for ").append(target.getName()).append(" ]<st>----------------</st></gold>\n");

        sb.append("<aqua>Tags:</aqua>\n");
        if (data.getTags().isEmpty()) {
            sb.append("<gray>  - None\n");
        } else {
            data.getTags().forEach(tag -> sb.append("<gray>  - <white>").append(tag).append("</white>\n"));
        }

        sb.append("<aqua>Active Objectives:</aqua>\n");
        if (data.getActiveObjectives().isEmpty()) {
            sb.append("<gray>  - None\n");
        } else {
            for (ActiveObjective obj : data.getActiveObjectives()) {
                sb.append("<gray>  - <white>").append(obj.getObjectiveId())
                        .append("</white> <dark_gray>(Progress: ").append(obj.getProgress()).append(")</dark_gray>\n");
            }
        }

        // --- THIS BLOCK IS NOW CORRECTED ---
        sb.append("<aqua>Reputation:</aqua>\n");
        // We now call our new getReputations() method
        if (data.getReputations().isEmpty()) {
            sb.append("<gray>  - None\n");
        } else {
            data.getReputations().forEach((faction, value) ->
                    sb.append("<gray>  - <white>").append(ChatUtils.capitalizeWords(faction))
                            .append(":</white> <green>").append(value).append("</green>\n")
            );
        }
        // --- END OF CORRECTION ---

        sb.append("<gold><st>----------------------------------------------------</st></gold>");

        sender.sendMessage(ChatUtils.format(sb.toString()));
    }

    private void handlePlayerTag(CommandSender sender, Player target, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatUtils.format(prefix + "<red>Usage: /qa player <name> tag <add|remove> <tag_name></red>"));
            return;
        }
        PlayerQuestData data = questManager.getPlayerData(target);
        String operation = args[0].toLowerCase();
        String tagName = args[1];

        if ("add".equals(operation)) {
            data.addTag(tagName);
            sender.sendMessage(ChatUtils.format(prefix + "<green>Tag '<white>" + tagName + "</white>' added to " + target.getName() + ".</green>"));
        } else if ("remove".equals(operation)) {
            data.removeTag(tagName);
            sender.sendMessage(ChatUtils.format(prefix + "<green>Tag '<white>" + tagName + "</white>' removed from " + target.getName() + ".</green>"));
        } else {
            sender.sendMessage(ChatUtils.format(prefix + "<red>Invalid tag operation. Use 'add' or 'remove'.</red>"));
        }
    }

    private void handlePlayerObjective(CommandSender sender, Player target, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatUtils.format(prefix + "<red>Usage: /qa player <name> objective <start|delete> ...</red>"));
            return;
        }
        String operation = args[0].toLowerCase();

        if ("start".equals(operation)) {
            if (args.length < 3) {
                sender.sendMessage(ChatUtils.format(prefix + "<red>Usage: /qa player <name> objective start <package> <objective_id></red>"));
                return;
            }
            String packageName = args[1];
            String objectiveId = args[2];
            QuestPackage context = questManager.getPackage(packageName);
            if (context == null) {
                sender.sendMessage(ChatUtils.format(prefix + "<red>Invalid package name: " + packageName + "</red>"));
                return;
            }
            questManager.startObjective(target, objectiveId, context);
            sender.sendMessage(ChatUtils.format(prefix + "<green>Attempted to start objective '<white>" + objectiveId + "</white>' for " + target.getName() + ".</green>"));
        } else if ("delete".equals(operation)) {
            String objectiveId = args[1];
            questManager.deleteObjective(target, objectiveId);
            sender.sendMessage(ChatUtils.format(prefix + "<green>Objective '<white>" + objectiveId + "</white>' deleted for " + target.getName() + ".</green>"));
        } else {
            sender.sendMessage(ChatUtils.format(prefix + "<red>Invalid objective operation. Use 'start' or 'delete'.</red>"));
        }
    }

    private void handlePlayerEvent(CommandSender sender, Player target, String[] args) {
        if (args.length < 3 || !"trigger".equalsIgnoreCase(args[0])) {
            sender.sendMessage(ChatUtils.format(prefix + "<red>Usage: /qa player <name> event trigger <package> <event_alias></red>"));
            return;
        }
        String packageName = args[1];
        String eventAlias = args[2];
        QuestPackage context = questManager.getPackage(packageName);
        if (context == null) {
            sender.sendMessage(ChatUtils.format(prefix + "<red>Invalid package name: " + packageName + "</red>"));
            return;
        }
        questManager.executeEvent(target, eventAlias, context);
        sender.sendMessage(ChatUtils.format(prefix + "<green>Event '<white>" + eventAlias + "</white>' triggered for " + target.getName() + ".</green>"));
    }

    private void handleNpc(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatUtils.format(prefix + "<red>This command must be run by a player.</red>"));
            return;
        }
        if (!sender.hasPermission("mmorpg.questadmin.info")) {
            sender.sendMessage(ChatUtils.format(prefix + noPerms));
            return;
        }
        if (args.length < 2 || !"info".equalsIgnoreCase(args[1])) {
            sender.sendMessage(ChatUtils.format(prefix + "<red>Usage: /questadmin npc info</red>"));
            return;
        }

        NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(player);
        if (npc == null) {
            sender.sendMessage(ChatUtils.format(prefix + "<red>You must have an NPC selected. Use /npc sel.</red>"));
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<gold><st>-----</st>[ Quest Info for ").append(npc.getName()).append(" (ID: ").append(npc.getId()).append(") ]<st>-----</st></gold>\n");

        Optional<QuestManager.ConversationContext> convoCtx = questManager.findConversationForNPC(npc.getId());
        sb.append("<aqua>Conversation Link:</aqua>\n");
        convoCtx.ifPresentOrElse(
                ctx -> sb.append("<gray>  - <white>").append(ctx.conversation().id())
                        .append("</white> (in package '").append(ctx.questPackage().getName()).append("')\n"),
                () -> sb.append("<gray>  - None\n")
        );

        Optional<NPCVisibilityRule> ruleOpt = questManager.getAllVisibilityRules().stream()
                .filter(r -> r.getNpcId() == npc.getId()).findFirst();

        sb.append("<aqua>Visibility Rule:</aqua>\n");
        if (ruleOpt.isPresent()) {
            NPCVisibilityRule rule = ruleOpt.get();
            sb.append("<gray>  - NPC is visible if ALL conditions are met:</gray>\n");
            rule.getConditions().forEach(cond -> sb.append("<gray>    - <white>\"").append(cond).append("\"</white>\n"));

            boolean meetsConditions = questManager.checkConditions(player, rule.getConditions());
            if (meetsConditions) {
                sb.append("<aqua>Your Status:</aqua> <green>[CONDITIONS MET]</green>\n");
                sb.append("<gray>(This NPC is currently visible to you)</gray>");
            } else {
                sb.append("<aqua>Your Status:</aqua> <red>[CONDITIONS NOT MET]</red>\n");
                sb.append("<gray>(This NPC is currently hidden from you)</gray>");
            }
        } else {
            sb.append("<gray>  - None (NPC is always visible)</gray>");
        }

        player.sendMessage(ChatUtils.format(sb.toString()));
    }

    private void sendHelp(CommandSender sender) {
        String help = """
            <gold><st>------------------</st>[ QuestAdmin Help ]<st>------------------</st></gold>
            <gray>/qa reload</gray> <dark_gray>- Reloads all quest files from disk.</dark_gray>
            <gray>/qa player <name> view</gray> <dark_gray>- View a player's quest data.</dark_gray>
            <gray>/qa player <name> tag <add|remove> <tag></gray> <dark_gray>- Modify a player's tags.</dark_gray>
            <gray>/qa player <name> objective <start|del> ...</gray> <dark_gray>- Manage objectives.</dark_gray>
            <gray>/qa player <name> event trigger ...</gray> <dark_gray>- Force trigger a quest event.</dark_gray>
            <gray>/qa npc info</gray> <dark_gray>- Get quest info for your selected NPC.</dark_gray>
            """;
        sender.sendMessage(ChatUtils.format(help));
    }

    // You'll need to add a public getter for packages in QuestManager for this to work
    // public QuestPackage getPackage(String name) { return questPackages.get(name); }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            return filterAndSort(Arrays.asList("reload", "player", "npc"), args[0]);
        }

        if (args[0].equalsIgnoreCase("player")) {
            if (args.length == 2) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .sorted().collect(Collectors.toList());
            }
            if (args.length == 3) {
                return filterAndSort(Arrays.asList("view", "tag", "objective", "event"), args[2]);
            }
            // Tab completion for /qa player <name> tag ...
            if (args.length == 4 && args[2].equalsIgnoreCase("tag")) {
                return filterAndSort(Arrays.asList("add", "remove"), args[3]);
            }
            // Tab completion for /qa player <name> objective ...
            if (args.length == 4 && args[2].equalsIgnoreCase("objective")) {
                return filterAndSort(Arrays.asList("start", "delete"), args[3]);
            }
        }

        if (args[0].equalsIgnoreCase("npc")) {
            if (args.length == 2) {
                return filterAndSort(List.of("info"), args[1]);
            }
        }

        return completions;
    }

    private List<String> filterAndSort(List<String> source, String input) {
        return source.stream()
                .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                .sorted()
                .collect(Collectors.toList());
    }
}
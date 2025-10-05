package org.nakii.mmorpg.quest;

import com.google.gson.Gson;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.data.ActiveObjective;
import org.nakii.mmorpg.quest.data.PlayerQuestData;
import org.nakii.mmorpg.quest.engine.ObjectiveFactory;
import org.nakii.mmorpg.quest.engine.condition.ConditionFactory;
import org.nakii.mmorpg.quest.engine.EventFactory;
import org.nakii.mmorpg.quest.engine.event.QuestEvent;
import org.nakii.mmorpg.quest.engine.objective.QuestObjective;
import org.nakii.mmorpg.quest.model.QuestPackage;
import org.nakii.mmorpg.quest.conversation.Conversation;
import org.nakii.mmorpg.quest.conversation.NPCOption;
import org.nakii.mmorpg.quest.conversation.PlayerOption;
import org.nakii.mmorpg.util.ChatUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QuestManager {

    private final MMORPGCore plugin;
    private final Map<String, QuestPackage> questPackages = new HashMap<>();
    private final ConcurrentHashMap<UUID, PlayerQuestData> playerData = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    private final EventFactory eventFactory;
    private final ConditionFactory conditionFactory;

    private final ObjectiveFactory objectiveFactory; // <-- NEW
    private final Map<Integer, String> npcConversationLinks = new HashMap<>();
    private final Map<String, QuestPackage> conversationPackageMap = new HashMap<>();
    private final Map<String, QuestPackage> objectivePackageMap = new HashMap<>();

    // A simple record to hold a conversation and its parent package together.
    public record ConversationContext(Conversation conversation, QuestPackage questPackage) {}

    public QuestManager(MMORPGCore plugin) {
        this.plugin = plugin;
        this.eventFactory = new EventFactory(plugin);
        this.conditionFactory = new ConditionFactory(plugin);
        this.objectiveFactory = new ObjectiveFactory(plugin);
        loadQuests();
    }

    public void loadQuests() {
        questPackages.clear();
        File questsFolder = new File(plugin.getDataFolder(), "quests");
        if (!questsFolder.exists()) {
            questsFolder.mkdirs();
        }

        // --- PASS 1: Indexing all packages and named components ---
        for (File packageDir : questsFolder.listFiles()) {
            if (packageDir.isDirectory()) {
                String packageName = packageDir.getName();
                QuestPackage questPackage = new QuestPackage(packageName);
                questPackages.put(packageName, questPackage);

                for (File questFile : packageDir.listFiles(f -> f.getName().endsWith(".yml"))) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(questFile);
                    // Index named events, conditions, objectives, etc.
                    indexSection(config, "events", questPackage::addEvent);
                    indexSection(config, "conditions", questPackage::addCondition);
                }
            }
        }

        // --- PASS 2: Linking and parsing complex objects like conversations ---
        for (File packageDir : questsFolder.listFiles()) {
            if (packageDir.isDirectory()) {
                QuestPackage questPackage = questPackages.get(packageDir.getName());
                for (File questFile : packageDir.listFiles(f -> f.getName().endsWith(".yml"))) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(questFile);
                    parseNpcLinks(config);
                    parseObjectives(config, questPackage);
                    parseConversations(config, questPackage);
                }
            }
        }

        plugin.getLogger().info("QuestManager initialized. Loaded " + questPackages.size() + " quest packages.");
    }

    // Helper for Pass 1
    @FunctionalInterface
    private interface SectionIndexer { void index(String key, String value); }
    private void indexSection(YamlConfiguration config, String section, SectionIndexer indexer) {
        if (config.isConfigurationSection(section)) {
            for (String key : config.getConfigurationSection(section).getKeys(false)) {
                indexer.index(key, config.getString(section + "." + key));
            }
        }
    }

    // Helper for Pass 2
    private void parseConversations(YamlConfiguration config, QuestPackage questPackage) {
        if (!config.isConfigurationSection("conversations")) return;
        ConfigurationSection convoSection = config.getConfigurationSection("conversations");

        for (String convoId : convoSection.getKeys(false)) {
            ConfigurationSection cs = convoSection.getConfigurationSection(convoId);
            Map<String, NPCOption> npcOptions = new HashMap<>();
            Map<String, PlayerOption> playerOptions = new HashMap<>();

            // Parse NPC options
            if (cs.isConfigurationSection("NPC_options")) {
                for (String key : cs.getConfigurationSection("NPC_options").getKeys(false)) {
                    ConfigurationSection opt = cs.getConfigurationSection("NPC_options." + key);
                    npcOptions.put(key, new NPCOption(
                            key,
                            opt.getString("text", ""),
                            split(opt.getString("conditions", "")),
                            split(opt.getString("events", "")),
                            split(opt.getString("pointers", ""))
                    ));
                }
            }

            // Parse Player options
            if (cs.isConfigurationSection("player_options")) {
                for (String key : cs.getConfigurationSection("player_options").getKeys(false)) {
                    ConfigurationSection opt = cs.getConfigurationSection("player_options." + key);
                    playerOptions.put(key, new PlayerOption(
                            key,
                            opt.getString("text", ""),
                            split(opt.getString("condition", "")), // Note: can be singular
                            split(opt.getString("event", "")),   // Note: can be singular
                            opt.getString("pointer", "")
                    ));
                }
            }

            questPackage.addConversation(convoId, new Conversation(
                    convoId,
                    cs.getString("quester", "Unknown"),
                    cs.getString("first", ""),
                    npcOptions,
                    playerOptions
            ));
            conversationPackageMap.put(convoId, questPackage);
        }
    }

    // Utility to split comma-separated strings into a list
    private List<String> split(String str) {
        if (str == null || str.isEmpty()) return Collections.emptyList();
        return Arrays.asList(str.split(","));
    }

    public void loadPlayerData(Player player) {
        try {
            String jsonData = plugin.getDatabaseManager().loadPlayerQuestData(player.getUniqueId());
            PlayerQuestData data = (jsonData == null || jsonData.isEmpty()) ? new PlayerQuestData() : gson.fromJson(jsonData, PlayerQuestData.class);

            data.postLoad();

            playerData.put(player.getUniqueId(), data);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load quest data for " + player.getName());
            e.printStackTrace();
        }
    }

    public void unloadPlayerData(Player player) {
        PlayerQuestData data = playerData.get(player.getUniqueId());
        if (data != null) {
            try {
                plugin.getDatabaseManager().savePlayerQuestData(player.getUniqueId(), gson.toJson(data));
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save quest data for " + player.getName());
                e.printStackTrace();
            }
        }
        playerData.remove(player.getUniqueId());
    }

    /**
     * Finds a conversation for a given NPC ID and returns it along with its package context.
     * @param npcId The Citizens NPC ID.
     * @return An Optional containing the ConversationContext if found.
     */
    public Optional<ConversationContext> findConversationForNPC(int npcId) {
        String conversationId = npcConversationLinks.get(npcId);
        if (conversationId == null) return Optional.empty();

        QuestPackage questPackage = conversationPackageMap.get(conversationId);
        if (questPackage == null) return Optional.empty();

        Conversation conversation = questPackage.getConversation(conversationId);
        if (conversation == null) return Optional.empty();

        return Optional.of(new ConversationContext(conversation, questPackage));
    }

    /**
     * The main entry point for executing an event for a player.
     * This method handles resolving aliases from quest packages.
     * @param player The player to execute the event for.
     * @param eventString The raw event string or an alias.
     * @param contextPackage The package from which this event is being called.
     */
    public void executeEvent(Player player, String eventString, QuestPackage contextPackage) {
        String resolvedEventString = resolveEventAlias(eventString, contextPackage);
        Optional<QuestEvent> event = eventFactory.createEvent(resolvedEventString);
        // FIX: Pass the contextPackage to the event's execute method
        event.ifPresent(e -> e.execute(player, plugin, contextPackage));
    }

    /**
     * Resolves a named alias into a full event string, prioritizing the local package.
     * @param alias The name of the event to resolve.
     * @param contextPackage The local package to check first.
     * @return The full event string, or the original alias if not found.
     */
    private String resolveEventAlias(String alias, QuestPackage contextPackage) {
        // A string with spaces is an inline definition, not an alias.
        if (alias.contains(" ")) {
            return alias;
        }

        // 1. Check for the alias in the local context package first.
        String resolved = contextPackage.getEvent(alias);
        if (resolved != null) {
            return resolved;
        }

        // 2. If not found, check all other packages (global search).
        for (QuestPackage pkg : questPackages.values()) {
            if (pkg.equals(contextPackage)) continue; // Skip the one we already checked
            resolved = pkg.getEvent(alias);
            if (resolved != null) {
                return resolved;
            }
        }

        // 3. If still not found, assume it's an inline definition with no arguments.
        return alias;
    }

    private void parseNpcLinks(YamlConfiguration config) {
        if (config.isConfigurationSection("npcs")) {
            for (String npcIdStr : config.getConfigurationSection("npcs").getKeys(false)) {
                int npcId = Integer.parseInt(npcIdStr);
                String conversationId = config.getString("npcs." + npcIdStr);
                npcConversationLinks.put(npcId, conversationId);
            }
        }
    }

    private void parseObjectives(YamlConfiguration config, QuestPackage questPackage) {
        if (config.isConfigurationSection("objectives")) {
            for (String key : config.getConfigurationSection("objectives").getKeys(false)) {
                String fullString = config.getString("objectives." + key);
                // FIX: Store the raw string in the package for alias resolution
                questPackage.addObjective(key, fullString);
                // Also parse and store the template for direct access
                objectiveFactory.createTemplate(key, fullString)
                        .ifPresent(template -> {
                            questPackage.addQuestObjective(key, template);
                            objectivePackageMap.put(key, questPackage); // Store context
                        });
            }
        }
    }

    public void startObjective(Player player, String objectiveId, QuestPackage context) {
        PlayerQuestData data = getPlayerData(player);
        if (data == null || data.getActiveObjective(objectiveId) != null) return;

        // FIX: Call the completed alias resolver
        Optional<QuestObjective> templateOpt = resolveObjectiveAlias(objectiveId, context);
        if (templateOpt.isPresent()) {
            QuestObjective template = templateOpt.get();
            data.addObjective(new ActiveObjective(template));
            player.sendMessage(ChatUtils.format("[QUEST] <gray>New Objective: " + template.getDescription()));
        } else {
            plugin.getLogger().warning("Could not start unknown objective: " + objectiveId);
        }
    }

    // --- MISSING METHOD IMPLEMENTED ---
    private Optional<QuestObjective> resolveObjectiveAlias(String alias, QuestPackage context) {
        if (alias.contains(" ")) { // It's an inline definition
            return objectiveFactory.createTemplate("inline-" + UUID.randomUUID(), alias);
        }

        // 1. Check local context package
        QuestObjective template = context.getQuestObjective(alias);
        if (template != null) {
            return Optional.of(template);
        }

        // 2. Check globally
        for (QuestPackage pkg : questPackages.values()) {
            if (pkg.equals(context)) continue;
            template = pkg.getQuestObjective(alias);
            if (template != null) {
                return Optional.of(template);
            }
        }
        return Optional.empty();
    }

    // --- NEW HELPER METHOD for ObjectiveProgressListener ---
    public QuestPackage findPackageForObjective(String objectiveId) {
        return objectivePackageMap.get(objectiveId);
    }

    public void deleteObjective(Player player, String objectiveId) {
        PlayerQuestData data = getPlayerData(player);
        if (data != null) {
            data.removeObjective(objectiveId);
        }
    }

    public PlayerQuestData getPlayerData(Player player) {
        return playerData.get(player.getUniqueId());
    }
    public ConditionFactory getConditionFactory() {
        return conditionFactory;
    }
    public EventFactory getEventFactory() {
        return eventFactory;
    }
    public ObjectiveFactory getObjectiveFactory() {
        return objectiveFactory;
    }
}
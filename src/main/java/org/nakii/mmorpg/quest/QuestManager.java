package org.nakii.mmorpg.quest;

import com.google.gson.Gson;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.conversation.ConversationData;
import org.nakii.mmorpg.quest.conversation.NPCOption;
import org.nakii.mmorpg.quest.conversation.PlayerOption;
import org.nakii.mmorpg.quest.data.PlayerQuestData;
import org.nakii.mmorpg.quest.engine.QuestException;
import org.nakii.mmorpg.quest.engine.api.QuestCondition;
import org.nakii.mmorpg.quest.engine.api.QuestEvent;
import org.nakii.mmorpg.quest.engine.identifier.ConditionID;
import org.nakii.mmorpg.quest.engine.identifier.EventID;
import org.nakii.mmorpg.quest.engine.instruction.Instruction;
import org.nakii.mmorpg.quest.engine.kernel.CoreRegistry;
import org.nakii.mmorpg.quest.engine.profile.Profile;
import org.nakii.mmorpg.quest.engine.profile.ProfileManager;
import org.nakii.mmorpg.quest.model.NPCVisibilityRule;
import org.nakii.mmorpg.quest.model.QuestHologram;
import org.nakii.mmorpg.quest.model.QuestPackage;
import org.nakii.mmorpg.quest.conversation.Conversation;

import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QuestManager {

    private final MMORPGCore plugin;
    private final CoreRegistry coreRegistry;
    private final ProfileManager profileManager;

    private final Map<String, QuestPackage> questPackages = new HashMap<>();
    private final ConcurrentHashMap<UUID, PlayerQuestData> playerData = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    private final Map<Integer, Conversation> npcConversationLinks = new HashMap<>();
    private final List<QuestHologram> allHolograms = new ArrayList<>();
    private final List<NPCVisibilityRule> allVisibilityRules = new ArrayList<>();

    public record ConversationContext(Conversation conversation, QuestPackage questPackage) {}

    public QuestManager(MMORPGCore plugin) {
        this.plugin = plugin;
        // Get instances of our new engine from the main class
        this.coreRegistry = plugin.getCoreRegistry();
        this.profileManager = plugin.getProfileManager();
        loadQuests();
    }

    public void loadQuests() {
        // Clear all old data from the processors
        coreRegistry.getConditionProcessor().clear();
        coreRegistry.getEventProcessor().clear();
        coreRegistry.getObjectiveProcessor().clear();
        questPackages.clear();
        npcConversationLinks.clear();
        allHolograms.clear();
        allVisibilityRules.clear();

        File questsFolder = new File(plugin.getDataFolder(), "quests");
        if (!questsFolder.exists()) questsFolder.mkdirs();

        for (File packageDir : Objects.requireNonNull(questsFolder.listFiles())) {
            if (packageDir.isDirectory()) {
                String packageName = packageDir.getName();
                QuestPackage questPackage = new QuestPackage(packageName);
                questPackages.put(packageName, questPackage);

                for (File questFile : Objects.requireNonNull(packageDir.listFiles(f -> f.getName().endsWith(".yml")))) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(questFile);
                    loadSection(config, "conditions", questPackage, coreRegistry.getConditionRegistry()::getFactory, coreRegistry.getConditionProcessor()::addCondition);
                    loadSection(config, "events", questPackage, coreRegistry.getEventRegistry()::getFactory, coreRegistry.getEventProcessor()::addEvent);

                    // We will add objective loading here in a later phase

                    // The rest of your loading logic remains the same
                    parseNpcLinks(config, questPackage);
                    parseHolograms(config, questPackage);
                    parseVisibilityRules(config, questPackage);
                }
            }
        }
        plugin.getLogger().info("QuestManager initialized. Loaded " + questPackages.size() + " quest packages.");
    }

    // Generic loader for conditions and events
    private <T> void loadSection(YamlConfiguration config, String sectionName, QuestPackage pack,
                                 java.util.function.Function<String, Optional<T>> factoryGetter,
                                 java.util.function.BiConsumer<String, Object> processorAdder) {
        if (!config.isConfigurationSection(sectionName)) return;

        for (String key : config.getConfigurationSection(sectionName).getKeys(false)) {
            String fullInstruction = config.getString(sectionName + "." + key);
            String type = fullInstruction.split(" ")[0];

            factoryGetter.apply(type).ifPresent(factory -> {
                try {
                    Instruction instruction = new Instruction(pack, fullInstruction);
                    Object component = ((java.util.function.Function<Instruction, Object>) factory).apply(instruction);
                    processorAdder.accept(pack.getName() + "." + key, component);
                } catch (QuestException e) {
                    plugin.getLogger().severe("Failed to load " + sectionName + " '" + key + "' in package '" + pack.getName() + "': " + e.getMessage());
                }
            });
        }
    }

    // --- REFACTORED METHODS ---

    public boolean checkConditions(Player player, List<String> conditionStrings) {
        if (conditionStrings == null || conditionStrings.isEmpty()) return true;

        Profile profile = profileManager.getProfile(player);

        for (String conditionString : conditionStrings) {
            String[] parts = conditionString.split(" ");
            String packName = "default"; // Or determine this dynamically if needed
            QuestPackage pack = questPackages.get(packName);
            if (pack == null) {
                // Find a fallback package or handle error
                pack = questPackages.values().stream().findFirst().orElse(null);
                if (pack == null) {
                    plugin.getLogger().severe("No quest packages loaded to check condition: " + conditionString);
                    return false;
                }
            }

            // This assumes simple conditions for now. We will improve this later.
            ConditionID id = new ConditionID(pack, conditionString);
            if (!coreRegistry.checkCondition(profile, id)) {
                return false; // A single failed condition causes the entire check to fail.
            }
        }
        return true;
    }

    public void executeEvent(Player player, String eventString, QuestPackage contextPackage) {
        Profile profile = profileManager.getProfile(player);
        // This assumes simple events for now. We will improve this later.
        EventID id = new EventID(contextPackage, eventString);
        coreRegistry.fireEvent(profile, id);
    }

    private void parseConversations(YamlConfiguration config, QuestPackage questPackage) {
        if (!config.isConfigurationSection("conversations")) return;
        ConfigurationSection convoSection = config.getConfigurationSection("conversations");

        for (String convoId : convoSection.getKeys(false)) {
            ConfigurationSection cs = convoSection.getConfigurationSection(convoId);
            Map<String, NPCOption> npcOptions = new HashMap<>();
            Map<String, PlayerOption> playerOptions = new HashMap<>();

            if (cs.isConfigurationSection("NPC_options")) {
                for (String key : cs.getConfigurationSection("NPC_options").getKeys(false)) {
                    ConfigurationSection opt = cs.getConfigurationSection("NPC_options." + key);
                    npcOptions.put(key, new NPCOption(key, opt.getString("text", ""), split(opt.getString("conditions", "")), split(opt.getString("events", "")), split(opt.getString("pointers", ""))));
                }
            }
            if (cs.isConfigurationSection("player_options")) {
                for (String key : cs.getConfigurationSection("player_options").getKeys(false)) {
                    ConfigurationSection opt = cs.getConfigurationSection("player_options." + key);
                    playerOptions.put(key, new PlayerOption(key, opt.getString("text", ""), split(opt.getString("condition", "")), split(opt.getString("event", "")), opt.getString("pointer", "")));
                }
            }

            // Create the new ConversationData object and add it to the package
            ConversationData conversationData = new ConversationData(convoId, cs.getString("quester", "Unknown"), cs.getString("first", ""), npcOptions, playerOptions);
            questPackage.addConversation(convoId, conversationData);
        }
    }

    public Optional<QuestPackage.ConversationContext> findConversationForNPC(int npcId) {
        String conversationId = npcConversationLinks.get(npcId);
        if (conversationId == null) return Optional.empty();

        // Find which package this conversation belongs to
        for (QuestPackage pkg : questPackages.values()) {
            ConversationData data = pkg.getConversation(conversationId);
            if (data != null) {
                return Optional.of(new QuestPackage.ConversationContext(data, pkg));
            }
        }

        return Optional.empty();
    }

    private void parseNpcLinks(YamlConfiguration config, QuestPackage questPackage) {
        // This method can parse conversations into the old record for now.
        // We will replace this with ConversationData in the next phase.
        if (!config.isConfigurationSection("conversations")) return;
        ConfigurationSection convoSection = config.getConfigurationSection("conversations");
        for (String convoId : convoSection.getKeys(false)) {
            // Your old parsing logic can stay here temporarily.
        }
    }

    private void parseHolograms(YamlConfiguration config, QuestPackage questPackage) {
        if (!config.isConfigurationSection("holograms")) return;
        ConfigurationSection hologramSection = config.getConfigurationSection("holograms");
        for (String id : hologramSection.getKeys(false)) {
            Map<String, Object> data = hologramSection.getConfigurationSection(id).getValues(false);
            QuestHologram hologram = new QuestHologram(id, data);
            questPackage.addHologram(id, hologram);
            allHolograms.add(hologram);
        }
    }

    private void parseVisibilityRules(YamlConfiguration config, QuestPackage questPackage) {
        if (!config.isConfigurationSection("npc_visibility")) return;
        ConfigurationSection visSection = config.getConfigurationSection("npc_visibility");
        for (String npcIdStr : visSection.getKeys(false)) {
            int npcId = Integer.parseInt(npcIdStr);
            Map<String, Object> data = visSection.getConfigurationSection(npcIdStr).getValues(false);
            NPCVisibilityRule rule = new NPCVisibilityRule(npcId, data);
            questPackage.addVisibilityRule(npcId, rule);
            allVisibilityRules.add(rule);
        }
    }

    public PlayerQuestData getPlayerData(Player player) {
        return playerData.get(player.getUniqueId());
    }

    public void loadPlayerData(Player player) {
        try {
            String jsonData = plugin.getDatabaseManager().loadPlayerQuestData(player.getUniqueId());
            PlayerQuestData data = (jsonData == null || jsonData.isEmpty()) ? new PlayerQuestData() : gson.fromJson(jsonData, PlayerQuestData.class);
            if (data.getTags() == null) { // Defensive check for old data
                data.postLoad();
            }
            playerData.put(player.getUniqueId(), data);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load quest data for " + player.getName());
            e.printStackTrace();
        }
    }

    public void unloadPlayerData(Player player) {
        // ... (this method is unchanged)
    }

    public List<QuestHologram> getAllHolograms() {
        return allHolograms;
    }

    public List<NPCVisibilityRule> getAllVisibilityRules() {
        return allVisibilityRules;
    }
}
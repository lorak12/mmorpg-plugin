package org.nakii.mmorpg.quest.model;

import org.nakii.mmorpg.quest.conversation.Conversation;
import org.nakii.mmorpg.quest.engine.objective.QuestObjective;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single quest package (a folder). It acts as a namespaced container
 * for all quest components defined within it.
 */
public class QuestPackage {

    private final String name;
    // These maps will hold the reusable, named components from the YAML files.
    private final Map<String, String> events = new HashMap<>();
    private final Map<String, String> conditions = new HashMap<>();
    private final Map<String, String> objectives = new HashMap<>();
    private final Map<String, Conversation> conversations = new HashMap<>();
    private final Map<String, QuestObjective> objectiveTemplates = new HashMap<>();
    private final Map<String, QuestHologram> holograms = new HashMap<>();
    private final Map<Integer, NPCVisibilityRule> visibilityRules = new HashMap<>();

    public QuestPackage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Getters and Adders for events, conditions, objectives, etc.
    public void addEvent(String key, String value) { this.events.put(key, value); }
    public String getEvent(String key) { return this.events.get(key); }

    public void addCondition(String key, String value) { this.conditions.put(key, value); }
    public String getCondition(String key) { return this.conditions.get(key); }

    public void addConversation(String key, Conversation conversation) { this.conversations.put(key, conversation); }
    public Conversation getConversation(String key) { return this.conversations.get(key); }

    // FIX: Changed this to store the full string from the YAML
    public void addObjective(String key, String value) { this.objectives.put(key, value); }
    public String getObjective(String key) { return this.objectives.get(key); }

    // NEW: Methods to store the parsed QuestObjective objects
    public void addQuestObjective(String key, QuestObjective template) { this.objectiveTemplates.put(key, template); }
    public QuestObjective getQuestObjective(String key) { return this.objectiveTemplates.get(key); }

    public void addHologram(String key, QuestHologram hologram) { this.holograms.put(key, hologram); }

    public void addVisibilityRule(int npcId, NPCVisibilityRule rule) { this.visibilityRules.put(npcId, rule); }

}
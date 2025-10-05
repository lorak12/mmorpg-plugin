package org.nakii.mmorpg.quest.data;

import java.util.*;

public class PlayerQuestData {

    // transient keyword prevents Gson from serializing this field, which is correct
    // as we want to save the raw string set, not this case-insensitive wrapper.
    private final transient Set<String> caseInsensitiveTags;
    private final Set<String> tags;
    private final Map<String, Double> reputation;
    private final List<ActiveObjective> activeObjectives;

    public PlayerQuestData() {
        this.tags = new HashSet<>();
        this.caseInsensitiveTags = new HashSet<>();
        this.reputation = new HashMap<>();
        this.activeObjectives = new ArrayList<>();
    }

    // --- NEW METHODS for TagEvent and TagCondition ---

    public boolean hasTag(String tag) {
        return caseInsensitiveTags.contains(tag.toLowerCase());
    }

    public void addTag(String tag) {
        tags.add(tag);
        caseInsensitiveTags.add(tag.toLowerCase());
    }

    public void removeTag(String tag) {
        tags.remove(tag);
        caseInsensitiveTags.remove(tag.toLowerCase());
    }

    public double getReputation(String faction) {
        return reputation.getOrDefault(faction.toLowerCase(), 0.0);
    }

    public void addReputation(String faction, double amount) {
        reputation.put(faction.toLowerCase(), getReputation(faction) + amount);
    }

    public List<ActiveObjective> getActiveObjectives() {
        return activeObjectives;
    }

    public void addObjective(ActiveObjective objective) {
        // Prevent duplicate objectives
        if (getActiveObjective(objective.getObjectiveId()) == null) {
            this.activeObjectives.add(objective);
        }
    }

    public void removeObjective(String objectiveId) {
        this.activeObjectives.removeIf(obj -> obj.getObjectiveId().equalsIgnoreCase(objectiveId));
    }

    public ActiveObjective getActiveObjective(String objectiveId) {
        for (ActiveObjective obj : activeObjectives) {
            if (obj.getObjectiveId().equalsIgnoreCase(objectiveId)) {
                return obj;
            }
        }
        return null;
    }

    /**
     * Called by Gson after deserialization to rebuild the transient cache.
     */
    public void postLoad() {
        caseInsensitiveTags.clear();
        for (String tag : tags) {
            caseInsensitiveTags.add(tag.toLowerCase());
        }
    }
}
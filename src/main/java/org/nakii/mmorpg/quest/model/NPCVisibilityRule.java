package org.nakii.mmorpg.quest.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a rule for a single NPC's visibility, loaded from a quest file.
 * The NPC will only be visible to a player if they meet all the conditions.
 */
public class NPCVisibilityRule {

    private final int npcId;
    private final List<String> conditions;
    private final boolean defaultVisibility;

    @SuppressWarnings("unchecked")
    public NPCVisibilityRule(int npcId, Map<String, Object> data) {
        this.npcId = npcId;
        this.conditions = (List<String>) data.getOrDefault("conditions", Collections.emptyList());
        // Default to visible if no conditions are present, invisible otherwise.
        this.defaultVisibility = this.conditions.isEmpty();
    }

    public int getNpcId() {
        return npcId;
    }

    public List<String> getConditions() {
        return conditions;
    }

    public boolean hasConditions() {
        return conditions != null && !conditions.isEmpty();
    }
}
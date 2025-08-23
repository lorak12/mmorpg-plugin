package org.nakii.mmorpg.skills;

import java.util.EnumMap;
import java.util.Map;

/**
 * Stores all skill-related data for a single player.
 * XP is stored as TOTAL accumulated XP for that skill.
 */
public class PlayerSkillData {

    private final Map<Skill, Integer> levels;
    private final Map<Skill, Double> totalExperience; // Renamed for clarity

    public PlayerSkillData() {
        this.levels = new EnumMap<>(Skill.class);
        this.totalExperience = new EnumMap<>(Skill.class);
    }

    public int getLevel(Skill skill) {
        return levels.getOrDefault(skill, 0);
    }

    public void setLevel(Skill skill, int level) {
        levels.put(skill, level);
    }

    /**
     * @return The TOTAL accumulated XP for this skill.
     */
    public double getXp(Skill skill) {
        return totalExperience.getOrDefault(skill, 0.0);
    }

    public void setXp(Skill skill, double totalXp) {
        totalExperience.put(skill, totalXp);
    }

    public void addXp(Skill skill, double amount) {
        setXp(skill, getXp(skill) + amount);
    }
}
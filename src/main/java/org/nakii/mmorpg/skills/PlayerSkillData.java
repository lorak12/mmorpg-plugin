package org.nakii.mmorpg.skills;

import java.util.EnumMap;
import java.util.Map;

/**
 * Stores the level and XP for all skills for a single player.
 */
public class PlayerSkillData {

    private final Map<Skill, Integer> skillLevels;
    private final Map<Skill, Double> skillXp;

    public PlayerSkillData() {
        this.skillLevels = new EnumMap<>(Skill.class);
        this.skillXp = new EnumMap<>(Skill.class);
        // Initialize all skills to Level 1, 0 XP
        for (Skill skill : Skill.values()) {
            skillLevels.put(skill, 1);
            skillXp.put(skill, 0.0);
        }
    }

    public int getLevel(Skill skill) {
        return skillLevels.getOrDefault(skill, 1);
    }

    public void setLevel(Skill skill, int level) {
        skillLevels.put(skill, level);
    }

    public double getXp(Skill skill) {
        return skillXp.getOrDefault(skill, 0.0);
    }

    public void setXp(Skill skill, double xp) {
        skillXp.put(skill, xp);
    }

    public void addXp(Skill skill, double amount) {
        skillXp.put(skill, getXp(skill) + amount);
    }
}
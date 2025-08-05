package org.nakii.mmorpg.skills;

import java.util.HashMap;
import java.util.Map;

public class PlayerSkillData {
    private final Map<Skill, Integer> levels;
    private final Map<Skill, Double> experience;

    public PlayerSkillData() {
        this.levels = new HashMap<>();
        this.experience = new HashMap<>();
        for (Skill skill : Skill.values()) {
            this.levels.put(skill, 1);
            this.experience.put(skill, 0.0);
        }
    }

    public int getLevel(Skill skill) {
        return levels.getOrDefault(skill, 1);
    }

    public void setLevel(Skill skill, int level) {
        this.levels.put(skill, level);
    }

    public double getExperience(Skill skill) {
        return experience.getOrDefault(skill, 0.0);
    }

    public void setExperience(Skill skill, double exp) {
        this.experience.put(skill, exp);
    }
}

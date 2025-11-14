package org.nakii.mmorpg.requirements;

import org.nakii.mmorpg.skills.Skill;

public class SkillRequirement implements Requirement {

    private final Skill requiredSkill;
    private final int requiredLevel;

    public SkillRequirement(String skillName, int level) {
        this.requiredSkill = Skill.valueOf(skillName);
        this.requiredLevel = level;
    }

    public Skill getRequiredSkill() {
        return requiredSkill;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }
}
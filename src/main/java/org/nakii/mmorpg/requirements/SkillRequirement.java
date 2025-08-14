package org.nakii.mmorpg.requirements;

import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.skills.Skill;

public class SkillRequirement implements Requirement {

    private final Skill requiredSkill;
    private final int requiredLevel;

    public SkillRequirement(String skillName, int level) {
        this.requiredSkill = Skill.valueOf(skillName);
        this.requiredLevel = level;
    }

    @Override
    public boolean meets(Player player) {
        int playerLevel = MMORPGCore.getInstance().getSkillManager().getLevel(player, requiredSkill);
        return playerLevel >= requiredLevel;
    }
}
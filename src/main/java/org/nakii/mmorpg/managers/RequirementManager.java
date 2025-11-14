package org.nakii.mmorpg.managers;

import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.requirements.CollectionRequirement;
import org.nakii.mmorpg.requirements.Requirement;
import org.nakii.mmorpg.requirements.SkillRequirement;
import org.nakii.mmorpg.requirements.SlayerRequirement;
import org.nakii.mmorpg.slayer.PlayerSlayerData;

import java.util.List;
import java.util.logging.Level;

public class RequirementManager {

    private final MMORPGCore plugin;
    private final SkillManager skillManager;
    private final SlayerDataManager slayerDataManager;
    private final CollectionManager collectionManager;

    public RequirementManager(MMORPGCore plugin, SkillManager skillManager, SlayerDataManager slayerDataManager, CollectionManager collectionManager) {
        this.plugin = plugin;
        this.skillManager = skillManager;
        this.slayerDataManager = slayerDataManager;
        this.collectionManager = collectionManager;
    }

    /**
     * Checks if a player meets all requirements from a given list of strings.
     */
    public boolean meetsAll(Player player, List<String> requirementStrings) {
        if (requirementStrings == null || requirementStrings.isEmpty()) {
            return true;
        }
        for (String reqString : requirementStrings) {
            Requirement requirement = Requirement.fromString(reqString);
            if (requirement == null) {
                plugin.getLogger().log(Level.WARNING, "Invalid or unrecognized requirement string: '" + reqString + "'");
                continue;
            }
            if (!meets(player, requirement)) {
                return false;
            }
        }
        return true;
    }

    /**
     * The central logic hub for checking any type of requirement.
     * @param player The player to check.
     * @param requirement The requirement data object.
     * @return True if the player meets the requirement.
     */
    private boolean meets(Player player, Requirement requirement) {
        if (requirement instanceof SkillRequirement skillReq) {
            int playerLevel = skillManager.getLevel(player, skillReq.getRequiredSkill());
            return playerLevel >= skillReq.getRequiredLevel();

        } else if (requirement instanceof SlayerRequirement slayerReq) {
            PlayerSlayerData data = slayerDataManager.getData(player);
            return data != null && data.getLevel(slayerReq.getRequiredSlayerType()) >= slayerReq.getRequiredLevel();

        } else if (requirement instanceof CollectionRequirement collectionReq) {
            int playerTier = collectionManager.getTierForPlayer(player, collectionReq.getRequiredCollectionId());
            return playerTier >= collectionReq.getRequiredTier();
        }
        // If we add new requirement types, we add their logic here.
        return true;
    }
}
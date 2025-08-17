package org.nakii.mmorpg.requirements;

import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.SlayerDataManager;
import org.nakii.mmorpg.slayer.PlayerSlayerData;

public class SlayerRequirement implements Requirement {

    private final String requiredSlayerType;
    private final int requiredLevel;

    public SlayerRequirement(String slayerType, int level) {
        this.requiredSlayerType = slayerType;
        this.requiredLevel = level;
    }

    @Override
    public boolean meets(Player player) {
        SlayerDataManager slayerDataManager = MMORPGCore.getInstance().getSlayerDataManager();
        PlayerSlayerData data = slayerDataManager.getData(player);
        if (data == null) {
            // Should not happen if player is online, but a safe check.
            return false;
        }
        return data.getLevel(requiredSlayerType) >= requiredLevel;
    }
}
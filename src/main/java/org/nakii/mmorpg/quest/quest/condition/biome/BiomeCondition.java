package org.nakii.mmorpg.quest.quest.condition.biome;

import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.condition.online.OnlineCondition;
import org.bukkit.block.Biome;

/**
 * Requires the player to be in a specified biome.
 */
public class BiomeCondition implements OnlineCondition {

    /**
     * The biome to check for.
     */
    private final Biome biome;

    /**
     * Creates a new BiomeCondition.
     *
     * @param biome The biome to check for
     */
    public BiomeCondition(final Biome biome) {
        this.biome = biome;
    }

    @Override
    public boolean check(final OnlineProfile profile) {
        return profile.getPlayer().getLocation().getBlock().getBiome() == biome;
    }
}

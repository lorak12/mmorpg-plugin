package org.nakii.mmorpg.quest.quest.condition.advancement;

import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.condition.online.OnlineCondition;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;

/**
 * Checks if the player has specified advancement.
 */
public class AdvancementCondition implements OnlineCondition {
    /**
     * Advancement which is required.
     */
    private final Advancement advancement;

    /**
     * Create a new Advancement condition.
     *
     * @param advancement the required advancement
     */
    public AdvancementCondition(final Advancement advancement) {
        this.advancement = advancement;
    }

    @Override
    public boolean check(final OnlineProfile profile) {
        final AdvancementProgress progress = profile.getPlayer().getAdvancementProgress(advancement);
        return progress.isDone();
    }
}

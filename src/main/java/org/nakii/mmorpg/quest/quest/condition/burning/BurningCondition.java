package org.nakii.mmorpg.quest.quest.condition.burning;

import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.condition.online.OnlineCondition;

/**
 * Checks if the player is burning.
 */
public class BurningCondition implements OnlineCondition {

    /**
     * Constructor of the BurningCondition.
     */
    public BurningCondition() {
    }

    @Override
    public boolean check(final OnlineProfile profile) {
        return profile.getPlayer().getFireTicks() > 0;
    }
}

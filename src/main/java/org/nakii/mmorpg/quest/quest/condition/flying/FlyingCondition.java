package org.nakii.mmorpg.quest.quest.condition.flying;

import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.condition.online.OnlineCondition;

/**
 * Checks if the player is gliding with elytra.
 */
public class FlyingCondition implements OnlineCondition {

    /**
     * Create a new flying condition.
     */
    public FlyingCondition() {
    }

    @Override
    public boolean check(final OnlineProfile profile) {
        return profile.getPlayer().isGliding();
    }
}

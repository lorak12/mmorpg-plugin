package org.nakii.mmorpg.quest.quest.condition.world;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.online.OnlineCondition;
import org.bukkit.World;

/**
 * A condition that checks if the player is in a specific world.
 */
public class WorldCondition implements OnlineCondition {

    /**
     * The world to check.
     */
    private final Variable<World> variableWorld;

    /**
     * Create a new World condition.
     *
     * @param world the world to check
     */
    public WorldCondition(final Variable<World> world) {
        this.variableWorld = world;
    }

    @Override
    public boolean check(final OnlineProfile profile) throws QuestException {
        final World world = variableWorld.getValue(profile);
        return profile.getPlayer().getWorld().equals(world);
    }
}

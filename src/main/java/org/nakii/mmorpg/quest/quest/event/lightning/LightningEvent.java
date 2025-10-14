package org.nakii.mmorpg.quest.quest.event.lightning;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.nullable.NullableEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

/**
 * Strikes a lightning at specified location.
 */
public class LightningEvent implements NullableEvent {
    /**
     * The location to strike the lightning at.
     */
    private final Variable<Location> location;

    /**
     * Whether the lightning should do damage.
     */
    private final boolean noDamage;

    /**
     * Creates a new lightning event.
     *
     * @param location the location to strike the lightning at
     * @param noDamage whether the lightning should do damage
     */
    public LightningEvent(final Variable<Location> location, final boolean noDamage) {
        this.location = location;
        this.noDamage = noDamage;
    }

    @Override
    public void execute(@Nullable final Profile profile) throws QuestException {
        final Location loc = location.getValue(profile);
        final World world = loc.getWorld();
        if (noDamage) {
            world.strikeLightningEffect(loc);
        } else {
            world.strikeLightning(loc);
        }
    }
}

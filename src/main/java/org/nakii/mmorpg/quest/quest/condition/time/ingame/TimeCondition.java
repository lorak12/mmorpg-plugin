package org.nakii.mmorpg.quest.quest.condition.time.ingame;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.nullable.NullableCondition;
import org.nakii.mmorpg.quest.quest.condition.time.Time;
import org.nakii.mmorpg.quest.quest.condition.time.TimeFrame;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

/**
 * A condition that checks the time.
 */
public class TimeCondition implements NullableCondition {

    /**
     * The time frame in which the time should be.
     */
    private final TimeFrame timeFrame;

    /**
     * The variable world.
     */
    private final Variable<World> variableWorld;

    /**
     * Creates a new TimeCondition.
     *
     * @param timeFrame     the time frame
     * @param variableWorld the variable world
     */
    public TimeCondition(final TimeFrame timeFrame, final Variable<World> variableWorld) {
        this.timeFrame = timeFrame;
        this.variableWorld = variableWorld;
    }

    @Override
    public boolean check(@Nullable final Profile profile) throws QuestException {
        return timeFrame.isTimeBetween(currentTime(variableWorld.getValue(profile)));
    }

    private Time currentTime(final World world) {
        final long time = world.getTime() + 6_000;
        final int hours = (int) (time / 1_000) % 24;
        final int minutes = (int) (time % 1000) * 60 / 1000;
        return new Time(hours, minutes);
    }
}

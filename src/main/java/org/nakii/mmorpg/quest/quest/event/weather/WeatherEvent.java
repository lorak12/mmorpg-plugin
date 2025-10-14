package org.nakii.mmorpg.quest.quest.event.weather;

import org.nakii.mmorpg.quest.api.common.function.Selector;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.nullable.NullableEvent;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

/**
 * The weather event, changing the weather on the server.
 */
public class WeatherEvent implements NullableEvent {

    /**
     * The weather that will be set when the event is executed.
     */
    private final Weather weather;

    /**
     * The selector to get the world for that the weather should be set.
     */
    private final Selector<World> worldSelector;

    /**
     * The time weather will not change naturally.
     */
    private final Variable<Number> duration;

    /**
     * Creates the weather event to set the given state.
     *
     * @param weather       the weather to set
     * @param worldSelector to get the world that should be affected
     * @param duration      how long the weather will not change - values &lt;= 0 won't set a duration
     */
    public WeatherEvent(final Weather weather, final Selector<World> worldSelector, final Variable<Number> duration) {
        this.weather = weather;
        this.worldSelector = worldSelector;
        this.duration = duration;
    }

    @Override
    public void execute(@Nullable final Profile profile) throws QuestException {
        final World world = worldSelector.selectFor(profile);
        weather.applyTo(world, duration.getValue(profile).intValue());
    }
}

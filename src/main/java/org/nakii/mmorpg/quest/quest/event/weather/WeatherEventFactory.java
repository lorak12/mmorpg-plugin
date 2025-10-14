package org.nakii.mmorpg.quest.quest.event.weather;

import org.nakii.mmorpg.quest.api.common.function.ConstantSelector;
import org.nakii.mmorpg.quest.api.common.function.Selector;
import org.nakii.mmorpg.quest.api.common.function.Selectors;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.quest.PrimaryServerThreadData;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEventFactory;
import org.nakii.mmorpg.quest.api.quest.event.PlayerlessEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerlessEventFactory;
import org.nakii.mmorpg.quest.api.quest.event.nullable.NullableEventAdapter;
import org.nakii.mmorpg.quest.api.quest.event.online.OnlineEventAdapter;
import org.nakii.mmorpg.quest.api.quest.event.thread.PrimaryServerThreadEvent;
import org.nakii.mmorpg.quest.api.quest.event.thread.PrimaryServerThreadPlayerlessEvent;
import org.nakii.mmorpg.quest.quest.event.DoNothingPlayerlessEvent;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Factory to create weather events from {@link Instruction}s.
 */
public class WeatherEventFactory implements PlayerEventFactory, PlayerlessEventFactory {
    /**
     * Logger factory to create a logger for the events.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * Data for primary server thread access.
     */
    private final PrimaryServerThreadData data;

    /**
     * Creates the weather event factory.
     *
     * @param loggerFactory the logger factory to create a logger for the events
     * @param data          the data for primary server thread access
     */
    public WeatherEventFactory(final BetonQuestLoggerFactory loggerFactory, final PrimaryServerThreadData data) {
        this.loggerFactory = loggerFactory;
        this.data = data;
    }

    @Override
    public PlayerEvent parsePlayer(final Instruction instruction) throws QuestException {
        final PlayerEvent weatherPlayerEvent = parseWeatherEvent(instruction);
        final PlayerEvent playerEvent;
        if (requiresPlayer(instruction)) {
            playerEvent = new OnlineEventAdapter(
                    weatherPlayerEvent::execute,
                    loggerFactory.create(WeatherEvent.class),
                    instruction.getPackage()
            );
        } else {
            playerEvent = weatherPlayerEvent;
        }
        return new PrimaryServerThreadEvent(playerEvent, data);
    }

    @Override
    public PlayerlessEvent parsePlayerless(final Instruction instruction) throws QuestException {
        if (requiresPlayer(instruction)) {
            return new DoNothingPlayerlessEvent();
        } else {
            return new PrimaryServerThreadPlayerlessEvent(parseWeatherEvent(instruction), data);
        }
    }

    private boolean requiresPlayer(final Instruction instruction) {
        return instruction.copy().getValue("world") == null;
    }

    private NullableEventAdapter parseWeatherEvent(final Instruction instruction) throws QuestException {
        final Weather weather = Weather.parseWeather(instruction.next());
        final Selector<World> worldSelector = parseWorld(instruction.getValue("world"));
        final Variable<Number> duration = instruction.getValue("duration", Argument.NUMBER, 0);
        return new NullableEventAdapter(new WeatherEvent(weather, worldSelector, duration));
    }

    private Selector<World> parseWorld(@Nullable final String worldName) {
        if (worldName == null) {
            return Selectors.fromPlayer(Player::getWorld);
        } else {
            final World world = data.server().getWorld(worldName);
            return new ConstantSelector<>(world);
        }
    }
}

package org.nakii.mmorpg.quest.kernel.registry.feature;

import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.schedule.Schedule;
import org.nakii.mmorpg.quest.api.schedule.Scheduler;
import org.nakii.mmorpg.quest.kernel.registry.FactoryRegistry;
import org.nakii.mmorpg.quest.schedule.EventScheduling;
import org.nakii.mmorpg.quest.schedule.ScheduleFactory;

import java.util.Set;

/**
 * Registry for usable schedule type in the Event scheduler.
 */
public class ScheduleRegistry extends FactoryRegistry<EventScheduling.ScheduleType<?, ?>> {

    /**
     * Create a new type registry.
     *
     * @param log the logger that will be used for logging
     */
    public ScheduleRegistry(final BetonQuestLogger log) {
        super(log, "Scheduler");
    }

    /**
     * Register a new schedule type.
     *
     * @param name      name of the schedule type
     * @param schedule  class object of the schedule type
     * @param scheduler instance of the scheduler
     * @param <S>       type of schedule
     */
    public <S extends Schedule> void register(final String name, final ScheduleFactory<S> schedule, final Scheduler<S, ?> scheduler) {
        register(name, new EventScheduling.ScheduleType<>(schedule, scheduler));
    }

    /**
     * Get all registered Schedule types.
     *
     * @return an unmodifiable copy
     */
    public Set<EventScheduling.ScheduleType<?, ?>> values() {
        return Set.copyOf(types.values());
    }
}

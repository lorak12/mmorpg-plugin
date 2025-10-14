package org.nakii.mmorpg.quest.schedule.impl.realtime.cron;

import org.nakii.mmorpg.quest.api.config.quest.QuestPackageManager;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.schedule.CronSchedule;
import org.nakii.mmorpg.quest.api.schedule.ScheduleID;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Schedules events to occur at a specific real time, using cron syntax.
 */
public class RealtimeCronSchedule extends CronSchedule {

    /**
     * Creates a new instance of the schedule.
     *
     * @param packManager the quest package manager to get quest packages from
     * @param scheduleID  id of the new schedule
     * @param instruction config defining the schedule
     * @throws QuestException if parsing the config failed
     */
    public RealtimeCronSchedule(final QuestPackageManager packManager, final ScheduleID scheduleID,
                                final ConfigurationSection instruction) throws QuestException {
        super(packManager, scheduleID, instruction, REBOOT_CRON_DEFINITION);
    }
}

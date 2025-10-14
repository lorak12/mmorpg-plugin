package org.nakii.mmorpg.quest.config.patcher.migration.migrator.from1to2;

import org.nakii.mmorpg.quest.api.bukkit.config.custom.multi.MultiConfiguration;
import org.nakii.mmorpg.quest.config.patcher.migration.QuestMigration;
import org.nakii.mmorpg.quest.config.quest.Quest;

/**
 * Handles the Ride migration.
 */
public class RideUpdates implements QuestMigration {

    /**
     * Creates a new ride migrator.
     */
    public RideUpdates() {
    }

    @Override
    public void migrate(final Quest quest) {
        final MultiConfiguration config = quest.getQuestConfig();
        replaceStartValueInSection(config, "objectives", "vehicle", "ride");
        replaceStartValueInSection(config, "conditions", "riding", "ride");
    }
}

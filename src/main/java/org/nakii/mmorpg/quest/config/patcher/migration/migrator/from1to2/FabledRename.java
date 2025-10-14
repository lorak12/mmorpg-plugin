package org.nakii.mmorpg.quest.config.patcher.migration.migrator.from1to2;

import org.nakii.mmorpg.quest.api.bukkit.config.custom.multi.MultiConfiguration;
import org.nakii.mmorpg.quest.config.patcher.migration.QuestMigration;
import org.nakii.mmorpg.quest.config.quest.Quest;

/**
 * Handles the fabled rename migration.
 */
public class FabledRename implements QuestMigration {

    /**
     * Creates a new fabled migrator.
     */
    public FabledRename() {
    }

    @Override
    public void migrate(final Quest quest) {
        final MultiConfiguration config = quest.getQuestConfig();
        replaceStartValueInSection(config, "conditions", "skillapiclass", "fabledclass");
        replaceStartValueInSection(config, "conditions", "skillapilevel", "fabledlevel");
    }
}

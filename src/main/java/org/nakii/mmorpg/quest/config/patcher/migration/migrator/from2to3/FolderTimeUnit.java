package org.nakii.mmorpg.quest.config.patcher.migration.migrator.from2to3;

import org.nakii.mmorpg.quest.api.bukkit.config.custom.multi.MultiConfiguration;
import org.nakii.mmorpg.quest.config.patcher.migration.QuestMigration;
import org.nakii.mmorpg.quest.config.quest.Quest;

/**
 * Migrates the folder time unit to the new format "unit:TimeUnit".
 */
public class FolderTimeUnit implements QuestMigration {
    /**
     * Creates a new folder time unit migrator.
     */
    public FolderTimeUnit() {
    }

    @Override
    public void migrate(final Quest quest) {
        final MultiConfiguration config = quest.getQuestConfig();
        replaceValueInSection(config, "events", "folder", " ticks", " unit:ticks");
        replaceValueInSection(config, "events", "folder", " seconds", " unit:seconds");
        replaceValueInSection(config, "events", "folder", " minutes", " unit:minutes");
    }
}

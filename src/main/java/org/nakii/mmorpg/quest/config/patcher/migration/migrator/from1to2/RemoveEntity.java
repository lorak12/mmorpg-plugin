package org.nakii.mmorpg.quest.config.patcher.migration.migrator.from1to2;

import org.nakii.mmorpg.quest.api.bukkit.config.custom.multi.MultiConfiguration;
import org.nakii.mmorpg.quest.config.patcher.migration.QuestMigration;
import org.nakii.mmorpg.quest.config.quest.Quest;

/**
 * Handles the remove entity migration.
 */
public class RemoveEntity implements QuestMigration {

    /**
     * Creates a new mmo_updates migrator.
     */
    public RemoveEntity() {
    }

    @Override
    public void migrate(final Quest quest) {
        final MultiConfiguration config = quest.getQuestConfig();
        replaceStartValueInSection(config, "events", "clear", "removeentity");
        replaceStartValueInSection(config, "events", "killmob", "removeentity");
    }
}

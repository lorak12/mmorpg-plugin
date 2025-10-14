package org.nakii.mmorpg.quest.config.patcher.migration.migrator.from1to2;

import org.nakii.mmorpg.quest.api.bukkit.config.custom.multi.MultiConfiguration;
import org.nakii.mmorpg.quest.config.patcher.migration.QuestMigration;
import org.nakii.mmorpg.quest.config.quest.Quest;

/**
 * Handles the aura_skills rename migration.
 */
public class AuraSkillsRename implements QuestMigration {

    /**
     * Creates a new aura_skills migrator.
     */
    public AuraSkillsRename() {
    }

    @Override
    public void migrate(final Quest quest) {
        final MultiConfiguration config = quest.getQuestConfig();
        replaceStartValueInSection(config, "conditions", "aureliumskillslevel", "auraskillslevel");
        replaceStartValueInSection(config, "conditions", "aureliumstatslevel", "auraskillsstatslevel");
        replaceStartValueInSection(config, "events", "aureliumskillsxp", "auraskillsxp");
    }
}

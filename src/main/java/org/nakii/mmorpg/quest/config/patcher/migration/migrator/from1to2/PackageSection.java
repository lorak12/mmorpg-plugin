package org.nakii.mmorpg.quest.config.patcher.migration.migrator.from1to2;

import org.nakii.mmorpg.quest.api.bukkit.config.custom.multi.MultiConfiguration;
import org.nakii.mmorpg.quest.api.config.ConfigAccessor;
import org.nakii.mmorpg.quest.config.patcher.migration.QuestMigration;
import org.nakii.mmorpg.quest.config.quest.Quest;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.FileNotFoundException;

/**
 * Handles the PackageSection migration.
 */
public class PackageSection implements QuestMigration {

    /**
     * The enabled string.
     */
    public static final String ENABLED = "enabled";

    /**
     * Creates a new PackageSection migrator.
     */
    public PackageSection() {
    }

    @Override
    public void migrate(final Quest quest) throws InvalidConfigurationException {
        final MultiConfiguration config = quest.getQuestConfig();
        if (config.contains(ENABLED, true)) {
            final boolean section = config.getBoolean(ENABLED);
            config.set("package.enabled", section);
            try {
                final ConfigAccessor packageFile = quest.getOrCreateConfigAccessor("package.yml");
                config.associateWith("package.enabled", packageFile.getConfig());
            } catch (final FileNotFoundException e) {
                throw new IllegalStateException("Could not load package file: " + e.getMessage(), e);
            }
            config.set(ENABLED, null);
        }
    }
}

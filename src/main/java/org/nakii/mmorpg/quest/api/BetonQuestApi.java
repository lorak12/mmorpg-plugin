package org.nakii.mmorpg.quest.api;

import org.nakii.mmorpg.quest.api.config.quest.QuestPackageManager;
import org.nakii.mmorpg.quest.api.feature.FeatureApi;
import org.nakii.mmorpg.quest.api.feature.FeatureRegistries;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.profile.ProfileProvider;
import org.nakii.mmorpg.quest.api.quest.PrimaryServerThreadData;
import org.nakii.mmorpg.quest.api.quest.QuestTypeApi;
import org.nakii.mmorpg.quest.api.quest.QuestTypeRegistries;

/**
 * The main API interface for BetonQuest, providing access to core functionalities.
 * This interface allows interaction with the quest system and features of BetonQuest.
 */
public interface BetonQuestApi {

    /**
     * Gets the profile provider to get profiles for players.
     *
     * @return the currently used Profile Provider instance
     */
    ProfileProvider getProfileProvider();

    /**
     * Gets the {@link QuestPackageManager} which provides access to the
     * {@link org.nakii.mmorpg.quest.api.config.quest.QuestPackage}s.
     *
     * @return the Quest Package Manager instance
     */
    QuestPackageManager getQuestPackageManager();

    /**
     * Gets the core QuestTypeRegistries to access and add new core implementations.
     *
     * @return registries for core types
     */
    QuestTypeRegistries getQuestRegistries();

    /**
     * Gets the QuestTypeApi which provides access to the core quest logic.
     *
     * @return the Quest Type API instance
     */
    QuestTypeApi getQuestTypeApi();

    /**
     * Gets the FeatureRegistries to access and add new feature implementations.
     *
     * @return registries for feature types
     */
    FeatureRegistries getFeatureRegistries();

    /**
     * Gets the FeatureApi which provides access to the BetonQuest features.
     *
     * @return the Feature API instance
     */
    FeatureApi getFeatureApi();

    /**
     * Gets the BetonQuest Logger factory to create new class specific loggers.
     *
     * @return the logger factory.
     */
    BetonQuestLoggerFactory getLoggerFactory();

    /**
     * Gets the data required to run tasks on the Bukkit main thread.
     *
     * @return the data
     */
    PrimaryServerThreadData getPrimaryServerThreadData();
}

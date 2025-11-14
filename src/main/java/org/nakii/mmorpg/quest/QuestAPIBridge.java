package org.nakii.mmorpg.quest;

import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.ItemLoreGenerator;
import org.nakii.mmorpg.managers.ItemManager;
import org.nakii.mmorpg.managers.MobManager;

/**
 * A bridge to provide safe, static access to core MMORPG managers for the QuestModule.
 * This class now uses the MMORPGCore Singleton to retrieve manager instances,
 * ensuring they are always available after the plugin has been enabled.
 * This class is now stateless and does not need to be initialized.
 */
public final class QuestAPIBridge {

    // Private constructor to prevent this utility class from being instantiated.
    private QuestAPIBridge() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * A private helper method that throws an exception if the bridge is accessed
     * before the main plugin has finished its initial loading.
     */
    private static void checkInitialized() {
        if (MMORPGCore.getInstance() == null) {
            // This is the error that will be thrown if something tries to access managers too early.
            // It is much clearer than a NullPointerException.
            throw new IllegalStateException("QuestAPIBridge cannot be used because MMORPGCore has not been enabled yet.");
        }
    }

    /**
     * Gets the active ItemManager instance directly from the main plugin class.
     * @return The ItemManager.
     * @throws IllegalStateException if called before the plugin is enabled.
     */
    @NotNull
    public static ItemManager getItemManager() {
        checkInitialized();
        return MMORPGCore.getInstance().getItemManager();
    }

    /**
     * Gets the active ItemLoreGenerator instance directly from the main plugin class.
     * @return The ItemLoreGenerator.
     * @throws IllegalStateException if called before the plugin is enabled.
     */
    @NotNull
    public static ItemLoreGenerator getItemLoreGenerator() {
        checkInitialized();
        return MMORPGCore.getInstance().getItemLoreGenerator();
    }

    /**
     * Gets the active MobManager instance directly from the main plugin class.
     * @return The MobManager.
     * @throws IllegalStateException if called before the plugin is enabled.
     */
    @NotNull
    public static MobManager getMobManager() {
        checkInitialized();
        return MMORPGCore.getInstance().getMobManager();
    }
}
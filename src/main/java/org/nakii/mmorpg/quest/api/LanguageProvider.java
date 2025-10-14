package org.nakii.mmorpg.quest.api;

/**
 * Interface for getting the default language from the config.
 * This is used to provide a default language for the plugin.
 */
@FunctionalInterface
public interface LanguageProvider {
    /**
     * Get the default language from the config.
     *
     * @return the default language
     */
    String getDefaultLanguage();
}

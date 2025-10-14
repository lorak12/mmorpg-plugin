package org.nakii.mmorpg.quest.compatibility.placeholderapi;

import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.QuestModule;
import org.nakii.mmorpg.quest.api.BetonQuestApi;
import org.nakii.mmorpg.quest.compatibility.Integrator;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 * Integrator for PlaceholderAPI.
 */
public class PlaceholderAPIIntegrator implements Integrator {

    /**
     * The BetonQuest plugin instance.
     */
    private final QuestModule plugin;

    /**
     * The default constructor.
     */
    public PlaceholderAPIIntegrator() {
        plugin = MMORPGCore.getInstance().getQuestModule();
    }

    @Override
    public void hook(final BetonQuestApi api) {
        api.getQuestRegistries().variable().registerCombined("ph", new PlaceholderVariableFactory());
        final PluginDescriptionFile description = MMORPGCore.getInstance().getDescription();
        new BetonQuestPlaceholder(api.getLoggerFactory().create(BetonQuestPlaceholder.class, "PlaceholderAPI Integration"),
                api.getProfileProvider(), plugin.getVariableProcessor(), description.getAuthors().toString(), description.getVersion()).register();
    }

    @Override
    public void reload() {
        // Empty
    }

    @Override
    public void close() {
        // Empty
    }
}

package org.nakii.mmorpg.quest.compatibility.npc.fancynpcs;

import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.QuestModule;
import org.nakii.mmorpg.quest.api.BetonQuestApi;
import org.nakii.mmorpg.quest.api.profile.ProfileProvider;
import org.nakii.mmorpg.quest.api.quest.npc.NpcRegistry;
import org.nakii.mmorpg.quest.compatibility.Integrator;
import org.bukkit.Bukkit;

/**
 * Integrator implementation for the FancyNpcs plugin.
 */
public class FancyNpcsIntegrator implements Integrator {
    /**
     * The prefix used before any registered name for distinguishing.
     */
    public static final String PREFIX = "FancyNpcs";

    /**
     * The empty default Constructor.
     */
    public FancyNpcsIntegrator() {
    }

    @Override
    public void hook(final BetonQuestApi api) {
        final QuestModule questModule = MMORPGCore.getInstance().getQuestModule();
        final NpcRegistry npcRegistry = api.getFeatureRegistries().npc();
        final ProfileProvider profileProvider = api.getProfileProvider();
        Bukkit.getPluginManager().registerEvents(new FancyCatcher(profileProvider, npcRegistry), MMORPGCore.getInstance());
        final FancyHider hider = new FancyHider(api.getFeatureApi().getNpcHider());
        Bukkit.getPluginManager().registerEvents(hider, MMORPGCore.getInstance());
        npcRegistry.register(PREFIX, new FancyFactory());
        npcRegistry.registerIdentifier(new FancyIdentifier(PREFIX));
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

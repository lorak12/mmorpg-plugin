package org.nakii.mmorpg.quest.compatibility.effectlib;

import de.slikey.effectlib.EffectManager;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.QuestModule;
import org.nakii.mmorpg.quest.api.BetonQuestApi;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.quest.PrimaryServerThreadData;
import org.nakii.mmorpg.quest.compatibility.Integrator;
import org.nakii.mmorpg.quest.compatibility.effectlib.event.ParticleEventFactory;
import org.jetbrains.annotations.Nullable;

/**
 * Integrator for <a href="https://github.com/elBukkit/EffectLib/">EffectLib</a>.
 */
public class EffectLibIntegrator implements Integrator {
    /**
     * BetonQuest plugin.
     */
    private final QuestModule plugin;

    /**
     * Effect manager starting and controlling effects.
     */
    @Nullable
    private EffectManager manager;

    /**
     * The default Constructor.
     */
    public EffectLibIntegrator() {
        plugin = MMORPGCore.getInstance().getQuestModule(); }

    @Override
    public void hook(final BetonQuestApi api) {
        manager = new EffectManager(MMORPGCore.getInstance());
        final BetonQuestLoggerFactory loggerFactory = api.getLoggerFactory();
        final PrimaryServerThreadData data = api.getPrimaryServerThreadData();
        api.getQuestRegistries().event().register("particle", new ParticleEventFactory(loggerFactory, data, manager));

        plugin.addProcessor(new EffectLibParticleManager(loggerFactory.create(EffectLibParticleManager.class), loggerFactory,
                api.getQuestPackageManager(), api.getQuestTypeApi(), api.getFeatureApi(), api.getProfileProvider(),
                plugin.getVariableProcessor(), manager, MMORPGCore.getInstance()));
    }

    @Override
    public void reload() {
        // Empty
    }

    @Override
    public void close() {
        if (manager != null) {
            manager.dispose();
        }
    }
}

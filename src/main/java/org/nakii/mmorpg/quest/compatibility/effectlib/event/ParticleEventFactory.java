package org.nakii.mmorpg.quest.compatibility.effectlib.event;

import de.slikey.effectlib.EffectManager;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.quest.PrimaryServerThreadData;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEventFactory;
import org.nakii.mmorpg.quest.api.quest.event.online.OnlineEventAdapter;
import org.nakii.mmorpg.quest.api.quest.event.thread.PrimaryServerThreadEvent;
import org.nakii.mmorpg.quest.util.Utils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Factory to create {@link ParticleEvent}s from {@link Instruction}s.
 */
public class ParticleEventFactory implements PlayerEventFactory {
    /**
     * Logger Factory to create new class specific logger.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * Data for primary server thread access.
     */
    private final PrimaryServerThreadData data;

    /**
     * Effect manager which will create and control the particles.
     */
    private final EffectManager manager;

    /**
     * Create a factory for particle events.
     *
     * @param loggerFactory the logger factory to create new class specific logger
     * @param data          the data for primary server thread access
     * @param manager       the effect manager which will create and control the particles
     */
    public ParticleEventFactory(final BetonQuestLoggerFactory loggerFactory, final PrimaryServerThreadData data, final EffectManager manager) {
        this.loggerFactory = loggerFactory;
        this.data = data;
        this.manager = manager;
    }

    @Override
    public PlayerEvent parsePlayer(final Instruction instruction) throws QuestException {
        final String string = instruction.next();
        final ConfigurationSection parameters = Utils.getNN(instruction.getPackage().getConfig().getConfigurationSection("effects." + string),
                "Effect '" + string + "' does not exist!");
        final String effectClass = Utils.getNN(parameters.getString("class"), "Effect '" + string + "' is incorrectly defined");
        final Variable<Location> loc = instruction.getValue("loc", Argument.LOCATION);
        final boolean privateParticle = instruction.hasArgument("private");
        final ParticleEvent particleEvent = new ParticleEvent(manager, effectClass, parameters, loc, privateParticle);
        final PlayerEvent playerEvent = new OnlineEventAdapter(particleEvent, loggerFactory.create(ParticleEvent.class), instruction.getPackage());
        return new PrimaryServerThreadEvent(playerEvent, data);
    }
}

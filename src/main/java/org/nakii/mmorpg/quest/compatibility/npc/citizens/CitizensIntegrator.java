package org.nakii.mmorpg.quest.compatibility.npc.citizens;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.QuestModule;
import org.nakii.mmorpg.quest.api.BetonQuestApi;
import org.nakii.mmorpg.quest.api.common.component.font.FontRegistry;
import org.nakii.mmorpg.quest.api.config.ConfigAccessor;
import org.nakii.mmorpg.quest.api.feature.FeatureApi;
import org.nakii.mmorpg.quest.api.feature.FeatureRegistries;
import org.nakii.mmorpg.quest.api.kernel.FeatureRegistry;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.quest.PrimaryServerThreadData;
import org.nakii.mmorpg.quest.api.quest.QuestTypeRegistries;
import org.nakii.mmorpg.quest.api.quest.event.EventRegistry;
import org.nakii.mmorpg.quest.api.quest.npc.NpcRegistry;
import org.nakii.mmorpg.quest.compatibility.Compatibility;
import org.nakii.mmorpg.quest.compatibility.Integrator;
import org.nakii.mmorpg.quest.compatibility.npc.citizens.event.move.CitizensMoveController;
import org.nakii.mmorpg.quest.compatibility.npc.citizens.event.move.CitizensMoveEvent;
import org.nakii.mmorpg.quest.compatibility.npc.citizens.event.move.CitizensMoveEventFactory;
import org.nakii.mmorpg.quest.compatibility.npc.citizens.event.move.CitizensStopEventFactory;
import org.nakii.mmorpg.quest.compatibility.npc.citizens.objective.NPCKillObjectiveFactory;
import org.nakii.mmorpg.quest.compatibility.protocollib.hider.CitizensHider;
import org.nakii.mmorpg.quest.conversation.ConversationColors;
import org.nakii.mmorpg.quest.conversation.ConversationIOFactory;
import org.bukkit.Server;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;

/**
 * Integrator for Citizens.
 */
public class CitizensIntegrator implements Integrator {

    /**
     * Handles NPC movement of the {@link CitizensMoveEvent}.
     */
    @SuppressWarnings("NullAway.Init")
    private static CitizensMoveController citizensMoveController;

    /**
     * The BetonQuest plugin instance.
     */
    private final QuestModule plugin;

    /**
     * The compatibility instance to use for checking other hooks.
     */
    private final Compatibility compatibility;

    /**
     * The default Constructor.
     *
     * @param compatibility the compatibility instance to use for checking other hooks
     */
    public CitizensIntegrator(final Compatibility compatibility) {
        this.compatibility = compatibility;
        plugin = MMORPGCore.getInstance().getQuestModule();
    }

    /**
     * Gets the move controller used to start and stop NPC movement.
     *
     * @return the move controller of this NPC integration
     */
    public static CitizensMoveController getCitizensMoveInstance() {
        return citizensMoveController;
    }

    @Override
    public void hook(final BetonQuestApi api) {
        final Server server = MMORPGCore.getInstance().getServer();
        final NPCRegistry citizensNpcRegistry = CitizensAPI.getNPCRegistry();
        final CitizensWalkingListener citizensWalkingListener = new CitizensWalkingListener(MMORPGCore.getInstance(), citizensNpcRegistry);
        server.getPluginManager().registerEvents(citizensWalkingListener, MMORPGCore.getInstance());

        final BetonQuestLoggerFactory loggerFactory = api.getLoggerFactory();
        citizensMoveController = new CitizensMoveController(loggerFactory.create(CitizensMoveController.class),
                MMORPGCore.getInstance(), api.getQuestTypeApi(), citizensWalkingListener);

        final QuestTypeRegistries questRegistries = api.getQuestRegistries();
        questRegistries.objective().register("npckill", new NPCKillObjectiveFactory(citizensNpcRegistry));

        final PrimaryServerThreadData data = api.getPrimaryServerThreadData();

        final PluginManager manager = server.getPluginManager();
        manager.registerEvents(citizensMoveController, MMORPGCore.getInstance());

        final EventRegistry eventRegistry = questRegistries.event();
        final FeatureApi featureApi = api.getFeatureApi();
        eventRegistry.register("npcmove", new CitizensMoveEventFactory(featureApi, data, citizensMoveController));
        eventRegistry.registerCombined("npcstop", new CitizensStopEventFactory(featureApi, data, citizensMoveController));

        final FeatureRegistries featureRegistries = api.getFeatureRegistries();
        final FeatureRegistry<ConversationIOFactory> conversationIORegistry = featureRegistries.conversationIO();
        final ConfigAccessor pluginConfig = plugin.getPluginConfig();
        final FontRegistry fontRegistry = plugin.getFontRegistry();
        final ConversationColors colors = plugin.getConversationColors();
        conversationIORegistry.register("chest", new CitizensInventoryConvIOFactory(loggerFactory,
                api.getQuestPackageManager(), fontRegistry, colors, pluginConfig, false));
        conversationIORegistry.register("combined", new CitizensInventoryConvIOFactory(loggerFactory,
                api.getQuestPackageManager(), fontRegistry, colors, pluginConfig, true));

        final NpcRegistry npcRegistry = featureRegistries.npc();
        manager.registerEvents(new CitizensInteractCatcher(plugin.getProfileProvider(), npcRegistry, citizensNpcRegistry,
                citizensMoveController), MMORPGCore.getInstance());
        npcRegistry.register("citizens", new CitizensNpcFactory(citizensNpcRegistry));
        npcRegistry.registerIdentifier(new CitizensReverseIdentifier(citizensNpcRegistry));
    }

    @Override
    public void postHook() {
        if (compatibility.getHooked().contains("ProtocolLib")) {
            CitizensHider.start(MMORPGCore.getInstance());
        } else {
            plugin.getLoggerFactory().create(CitizensIntegrator.class)
                    .warn("ProtocolLib Integration not found! Hiding Citizens NPCs won't be available.");
        }
    }

    @Override
    public void reload() {
        if (CitizensHider.getInstance() != null) {
            CitizensHider.start(MMORPGCore.getInstance());
        }
    }

    @Override
    public void close() {
        HandlerList.unregisterAll(citizensMoveController);
    }
}

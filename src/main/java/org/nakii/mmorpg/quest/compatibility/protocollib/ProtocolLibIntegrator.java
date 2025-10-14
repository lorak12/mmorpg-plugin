package org.nakii.mmorpg.quest.compatibility.protocollib;

import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.QuestModule;
import org.nakii.mmorpg.quest.api.BetonQuestApi;
import org.nakii.mmorpg.quest.api.quest.PrimaryServerThreadData;
import org.nakii.mmorpg.quest.compatibility.HookException;
import org.nakii.mmorpg.quest.compatibility.Integrator;
import org.nakii.mmorpg.quest.compatibility.UnsupportedVersionException;
import org.nakii.mmorpg.quest.compatibility.protocollib.conversation.MenuConvIOFactory;
import org.nakii.mmorpg.quest.compatibility.protocollib.conversation.PacketInterceptorFactory;
import org.nakii.mmorpg.quest.versioning.UpdateStrategy;
import org.nakii.mmorpg.quest.versioning.Version;
import org.nakii.mmorpg.quest.versioning.VersionComparator;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Integrator for ProtocolLib.
 */
public class ProtocolLibIntegrator implements Integrator {
    /**
     * The BetonQuest plugin instance.
     */
    private final QuestModule plugin;

    /**
     * The default constructor.
     */
    public ProtocolLibIntegrator() {
        plugin = MMORPGCore.getInstance().getQuestModule();
    }

    @Override
    public void hook(final BetonQuestApi api) throws HookException {
        final Plugin protocolLib = Bukkit.getPluginManager().getPlugin("ProtocolLib");
        final Version protocolLibVersion = new Version(protocolLib.getDescription().getVersion());
        final VersionComparator comparator = new VersionComparator(UpdateStrategy.MAJOR, "SNAPSHOT-");
        if (comparator.isOtherNewerThanCurrent(protocolLibVersion, new Version("5.0.0-SNAPSHOT-636"))) {
            throw new UnsupportedVersionException(protocolLib, "5.0.0-SNAPSHOT-636");
        }

        api.getFeatureRegistries().conversationIO().register("menu", new MenuConvIOFactory(MMORPGCore.getInstance(), plugin.getTextParser(),
                plugin.getFontRegistry(), plugin.getPluginConfig(), plugin.getConversationColors()));
        api.getFeatureRegistries().interceptor().register("packet", new PacketInterceptorFactory());

        final PrimaryServerThreadData data = api.getPrimaryServerThreadData();
        api.getQuestRegistries().event().register("freeze", new FreezeEventFactory(api.getLoggerFactory(), data));
    }

    @Override
    public void reload() {
        // Empty
    }

    @Override
    public void close() {
        FreezeEvent.cleanup();
    }
}

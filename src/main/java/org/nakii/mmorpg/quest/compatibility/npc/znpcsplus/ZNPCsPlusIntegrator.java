package org.nakii.mmorpg.quest.compatibility.npc.znpcsplus;

import lol.pyr.znpcsplus.api.NpcApiProvider;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.QuestModule;
import org.nakii.mmorpg.quest.api.BetonQuestApi;
import org.nakii.mmorpg.quest.api.profile.ProfileProvider;
import org.nakii.mmorpg.quest.api.quest.npc.NpcRegistry;
import org.nakii.mmorpg.quest.compatibility.HookException;
import org.nakii.mmorpg.quest.compatibility.Integrator;
import org.nakii.mmorpg.quest.compatibility.UnsupportedVersionException;
import org.nakii.mmorpg.quest.versioning.UpdateStrategy;
import org.nakii.mmorpg.quest.versioning.Version;
import org.nakii.mmorpg.quest.versioning.VersionComparator;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Integrator implementation for the
 * <a href="https://www.spigotmc.org/resources/znpcsplus.109380/">ZNPCsPlus plugin</a>.
 */
public class ZNPCsPlusIntegrator implements Integrator {
    /**
     * The prefix used before any registered name for distinguishing.
     */
    public static final String PREFIX = "ZNPCsPlus";

    /**
     * The default Constructor.
     */
    public ZNPCsPlusIntegrator() {
    }

    @Override
    public void hook(final BetonQuestApi api) throws HookException {
        validateVersion();
        final QuestModule questModule = MMORPGCore.getInstance().getQuestModule();
        final NpcRegistry npcRegistry = api.getFeatureRegistries().npc();
        final ProfileProvider profileProvider = api.getProfileProvider();
        Bukkit.getPluginManager().registerEvents(new ZNPCsPlusCatcher(profileProvider, npcRegistry), MMORPGCore.getInstance());
        final ZNPCsPlusHider hider = new ZNPCsPlusHider(api.getFeatureApi().getNpcHider());
        Bukkit.getPluginManager().registerEvents(hider, MMORPGCore.getInstance());
        npcRegistry.register(PREFIX, new ZNPCsPlusFactory(NpcApiProvider.get().getNpcRegistry()));
        npcRegistry.registerIdentifier(new ZNPCsPlusIdentifier(PREFIX));
    }

    private void validateVersion() throws UnsupportedVersionException {
        final Plugin plugin = Bukkit.getPluginManager().getPlugin(PREFIX);
        final Version version = new Version(plugin.getDescription().getVersion());
        final VersionComparator comparator = new VersionComparator(UpdateStrategy.MAJOR, "SNAPSHOT-");
        if (!comparator.isOtherNewerOrEqualThanCurrent(new Version("2.1.0-SNAPSHOT"), version)) {
            throw new UnsupportedVersionException(plugin, "2.1.0-SNAPSHOT+");
        }
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

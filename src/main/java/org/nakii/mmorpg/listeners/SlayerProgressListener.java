package org.nakii.mmorpg.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.events.PlayerGainCombatXpEvent;
import org.nakii.mmorpg.events.SlayerProgressUpdateEvent;
import org.nakii.mmorpg.managers.MobManager;
import org.nakii.mmorpg.managers.SlayerManager;
import org.nakii.mmorpg.slayer.ActiveSlayerQuest;
import org.nakii.mmorpg.util.ChatUtils;

public class SlayerProgressListener implements Listener {

    private final MMORPGCore plugin;
    private final SlayerManager slayerManager;
    private final MobManager mobManager;

    public SlayerProgressListener(MMORPGCore plugin, SlayerManager slayerManager, MobManager mobManager) {
        this.plugin = plugin;
        this.slayerManager = slayerManager;
        this.mobManager = mobManager;
    }

    @EventHandler
    public void onCombatXpGain(PlayerGainCombatXpEvent event) {
        Player player = event.getPlayer();
        if (!slayerManager.hasActiveQuest(player)) {
            return;
        }

        ActiveSlayerQuest quest = slayerManager.getActiveSlayerQuest(player);
        String mobCategory = mobManager.getMobCategory(event.getVictim());
        if (mobCategory == null) return;

        String targetCategory = slayerManager.getTargetCategoryForQuest(quest);
        if (!mobCategory.equalsIgnoreCase(targetCategory)) {
            return;
        }

        quest.addXp(event.getXpAmount());
        String progressMessage = String.format("<red>Slayer Quest: <green>%.0f / %d XP</green>", quest.getCurrentXp(), quest.getXpToSpawn());
        player.sendActionBar(ChatUtils.format(progressMessage));

        plugin.getServer().getPluginManager().callEvent(new SlayerProgressUpdateEvent(player, quest));

        if (quest.isComplete()) {
            slayerManager.spawnSlayerBoss(player, quest);
        }
    }
}
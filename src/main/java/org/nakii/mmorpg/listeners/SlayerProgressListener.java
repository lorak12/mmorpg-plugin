package org.nakii.mmorpg.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.events.PlayerGainCombatXpEvent;
import org.nakii.mmorpg.events.SlayerProgressUpdateEvent;
import org.nakii.mmorpg.managers.SlayerManager;
import org.nakii.mmorpg.slayer.ActiveSlayerQuest;
import org.nakii.mmorpg.util.ChatUtils;

public class SlayerProgressListener implements Listener {

    private final MMORPGCore plugin;
    private final SlayerManager slayerManager;

    public SlayerProgressListener(MMORPGCore plugin) {
        this.plugin = plugin;
        this.slayerManager = plugin.getSlayerManager();
    }

    @EventHandler
    public void onCombatXpGain(PlayerGainCombatXpEvent event) {
        Player player = event.getPlayer();

        // 1. Check if the player even has an active slayer quest.
        if (!slayerManager.hasActiveQuest(player)) {
            return;
        }

        ActiveSlayerQuest quest = slayerManager.getActiveSlayerQuest(player);

        // 2. Get the category of the mob that was killed (e.g., "ZOMBIE").
        String mobCategory = plugin.getMobManager().getMobCategory(event.getVictim());
        if (mobCategory == null) return;

        // 3. Get the target category for the player's quest.
        String targetCategory = slayerManager.getTargetCategoryForQuest(quest);

        // 4. Compare them. If they don't match, this kill doesn't count.
        if (!mobCategory.equalsIgnoreCase(targetCategory)) {
            return;
        }

        // 5. The kill counts! Add progress to the quest.
        quest.addXp(event.getXpAmount());

        // 6. Update the player's HUD with their progress.
        String progressMessage = String.format("<red>Slayer Quest: <green>%.0f / %d XP</green>",
                quest.getCurrentXp(), quest.getXpToSpawn());
        player.sendActionBar(ChatUtils.format(progressMessage));

        // 7. Announce the progress update
        plugin.getServer().getPluginManager().callEvent(new SlayerProgressUpdateEvent(player, quest));

        // 8. Check if the quest is now complete.
        if (quest.isComplete()) {
            slayerManager.spawnSlayerBoss(player, quest);
        }
    }
}
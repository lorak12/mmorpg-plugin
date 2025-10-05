package org.nakii.mmorpg.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.QuestManager;
import org.nakii.mmorpg.quest.data.ActiveObjective;
import org.nakii.mmorpg.quest.data.PlayerQuestData;
import org.nakii.mmorpg.quest.engine.objective.KillObjective;
import org.nakii.mmorpg.quest.engine.objective.QuestObjective;

import java.util.Iterator;
import java.util.List;

public class ObjectiveProgressListener implements Listener {

    private final MMORPGCore plugin;

    public ObjectiveProgressListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Player player = event.getEntity().getKiller();
        QuestManager questManager = plugin.getQuestManager();

        PlayerQuestData data = questManager.getPlayerData(player);
        if (data == null || data.getActiveObjectives().isEmpty()) return;

        String killedMobId = plugin.getMobManager().getMobId(event.getEntity());
        if (killedMobId == null) return;

        // Use an iterator to safely remove objectives while looping
        Iterator<ActiveObjective> iterator = data.getActiveObjectives().iterator();
        while (iterator.hasNext()) {
            ActiveObjective activeObjective = iterator.next();
            QuestObjective template = activeObjective.getTemplate();

            if (template instanceof KillObjective killObjective) {
                if (killObjective.getMobId().equalsIgnoreCase(killedMobId)) {
                    activeObjective.incrementProgress(1);

                    plugin.getHUDManager().updateActionBar(player, "<gray>Progress: " + killObjective.getMobId() + " " + activeObjective.getProgress() + "/" + killObjective.getAmount(), 2);

                    if (activeObjective.isComplete()) {
                        iterator.remove(); // IMPORTANT: Remove before firing events to prevent loops
                        List<String> completionEvents = killObjective.getCompletionEvents(); // Add this method to KillObjective
                        for (String eventString : completionEvents) {
                            questManager.executeEvent(player, eventString, questManager.findPackageForObjective(template.getObjectiveId()));
                        }
                    }
                }
            }
        }
    }
}
package org.nakii.mmorpg.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.slayer.ActiveSlayerQuest;

/**
 * Called whenever a player's active Slayer Quest progress is updated.
 * This event is fired after the XP has been added to the quest object.
 */
public class SlayerProgressUpdateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final ActiveSlayerQuest quest;

    public SlayerProgressUpdateEvent(Player player, ActiveSlayerQuest quest) {
        this.player = player;
        this.quest = quest;
    }

    /**
     * @return The player whose quest progress was updated.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return The ActiveSlayerQuest object, containing the latest progress.
     */
    public ActiveSlayerQuest getQuest() {
        return quest;
    }

    /**
     * Standard Bukkit event boilerplate.
     */
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Standard Bukkit event boilerplate.
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
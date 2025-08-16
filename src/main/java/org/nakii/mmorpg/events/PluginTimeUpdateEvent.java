package org.nakii.mmorpg.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called every 10 plugin minutes, signaling a display-worthy time update.
 */
public class PluginTimeUpdateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final int hour;
    private final int minute;

    public PluginTimeUpdateEvent(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
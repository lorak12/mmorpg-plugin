package org.nakii.mmorpg.events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player is about to gain Combat XP from killing a mob.
 */
public class PlayerGainCombatXpEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final LivingEntity victim;
    private final double xpAmount;

    public PlayerGainCombatXpEvent(Player player, LivingEntity victim, double xpAmount) {
        this.player = player;
        this.victim = victim;
        this.xpAmount = xpAmount;
    }

    public Player getPlayer() {
        return player;
    }

    public LivingEntity getVictim() {
        return victim;
    }

    public double getXpAmount() {
        return xpAmount;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
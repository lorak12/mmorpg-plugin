package org.nakii.mmorpg.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.skills.Skill;

import java.util.Optional;

public class AlchemyListener implements Listener {
    private final MMORPGCore plugin;
    public AlchemyListener(MMORPGCore plugin) { this.plugin = plugin; }

    @EventHandler
    public void onBrew(BrewEvent event) {
        // Note: BrewEvent doesn't provide a player. The best way is to check for a nearby player.
        // This is not perfectly accurate but is the standard approach.
        Optional<Player> brewer = event.getBlock().getLocation().getNearbyPlayers(5).stream().findFirst();
        brewer.ifPresent(player -> {
            plugin.getSkillManager().addXpForAction(player, Skill.ALCHEMY, "DEFAULT_POTION");
        });
    }
}
package org.nakii.mmorpg.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.nakii.mmorpg.managers.SkillManager;
import org.nakii.mmorpg.skills.Skill;

import java.util.Optional;

public class AlchemyListener implements Listener {

    private final SkillManager skillManager;

    public AlchemyListener(SkillManager skillManager) {
        this.skillManager = skillManager;
    }

    @EventHandler
    public void onBrew(BrewEvent event) {
        Optional<Player> brewer = event.getBlock().getLocation().getNearbyPlayers(5).stream().findFirst();
        brewer.ifPresent(player -> {
            skillManager.addXpForAction(player, Skill.ALCHEMY, "DEFAULT_POTION");
        });
    }
}
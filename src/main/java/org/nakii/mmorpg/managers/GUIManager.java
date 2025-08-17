package org.nakii.mmorpg.managers;

import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.guis.AbstractGui;
import org.nakii.mmorpg.guis.EnchantingGui;
import org.nakii.mmorpg.guis.SkillsGui;
// You will add more imports here, like StatsGui, AnvilGui, etc.

/**
 * A lightweight manager that acts as a navigator or factory for opening GUIs.
 * It does not contain any GUI building logic itself.
 */
public class GUIManager {

    private final MMORPGCore plugin;

    public GUIManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates and opens the main skills GUI for a player.
     * @param player The player to open the GUI for.
     */
    public void openSkillsGUI(Player player) {
        new SkillsGui(plugin, player).open();
    }
}
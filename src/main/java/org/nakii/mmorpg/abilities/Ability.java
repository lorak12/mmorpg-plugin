package org.nakii.mmorpg.abilities;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Ability {
    /**
     * Gets the unique key for this ability.
     * @return The ability key (e.g., "DAGGER_THROW").
     */
    String getKey();

    /**
     * Executes the ability's logic.
     * @param player The player activating the ability.
     * @param item The item the ability was triggered from.
     */
    void execute(Player player, ItemStack item);
}
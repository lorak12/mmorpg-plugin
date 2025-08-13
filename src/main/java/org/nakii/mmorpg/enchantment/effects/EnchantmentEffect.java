package org.nakii.mmorpg.enchantment.effects;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.nakii.mmorpg.enchantment.CustomEnchantment;

public interface EnchantmentEffect {

    /**
     * Called before damage is finalized to allow for modification.
     * @return The new damage value after this effect's modification.
     */
    default double onDamageModify(double initialDamage, EntityDamageByEntityEvent event, CustomEnchantment enchantment, int level, boolean isCritical) {
        return initialDamage; // By default, return the damage unmodified
    }

    /**
     * Called when a player attacks an entity.
     * @param event The original damage event.
     * @param enchantment The CustomEnchantment object for context.
     * @param level The level of the enchantment on the item.
     */
    default void onAttack(EntityDamageByEntityEvent event, CustomEnchantment enchantment, int level, boolean isCritical) {}

    /**
     * Called when a player kills an entity.
     * @param event The original death event.
     * @param enchantment The CustomEnchantment object for context.
     * @param level The level of the enchantment.
     */
    default void onKill(EntityDeathEvent event, Player killer, CustomEnchantment enchantment, int level) {}

    /**
     * Called when the wearer of an item with this enchantment is damaged.
     * @param event The original damage event.
     * @param enchantment The CustomEnchantment object.
     * @param level The level of the enchantment on the worn item.
     */
    default void onDamaged(EntityDamageByEntityEvent event, CustomEnchantment enchantment, int level) {}
}
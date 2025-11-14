package org.nakii.mmorpg.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.item.CustomItemTemplate;
import org.nakii.mmorpg.managers.AbilityManager;
import org.nakii.mmorpg.managers.CooldownManager;
import org.nakii.mmorpg.managers.ItemManager;
import org.nakii.mmorpg.managers.PlayerManager;
import org.nakii.mmorpg.util.Keys;

import java.util.concurrent.TimeUnit;

public class AbilityListener implements Listener {

    private final MMORPGCore plugin;
    private final ItemManager itemManager;
    private final AbilityManager abilityManager;
    private final PlayerManager playerManager;
    private final CooldownManager cooldownManager;

    public AbilityListener(MMORPGCore plugin, ItemManager itemManager, AbilityManager abilityManager, PlayerManager playerManager, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.itemManager = itemManager;
        this.abilityManager = abilityManager;
        this.playerManager = playerManager;
        this.cooldownManager = cooldownManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Must be a right-click action
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Must be holding an item
        ItemStack item = event.getItem();
        if (item == null || item.getType().isAir()) {
            return;
        }

        // The item must be one of our custom items with an ability
        String abilityKey = item.getItemMeta().getPersistentDataContainer().get(Keys.ABILITY_KEY, PersistentDataType.STRING);
        if (abilityKey == null) {
            return;
        }

        Player player = event.getPlayer();

        // Check for cooldown
        if (cooldownManager.isOnCooldown(player.getUniqueId(), abilityKey)) {
            long remainingMillis = cooldownManager.getCooldownRemaining(player.getUniqueId(), abilityKey);
            double remainingSeconds = remainingMillis / 1000.0;
            player.sendActionBar(plugin.getMiniMessage().deserialize("<red>Ability on cooldown for " + String.format("%.1f", remainingSeconds) + "s</red>"));
            return;
        }

        // Get the template to find mana cost and cooldown duration
        String itemId = itemManager.getItemId(item);
        CustomItemTemplate template = itemManager.getTemplate(itemId);
        if (template == null || template.getAbilityInfo().isEmpty()) {
            return;
        }

        CustomItemTemplate.AbilityInfo abilityInfo = template.getAbilityInfo().get();
        double manaCost = abilityInfo.manaCost();

        // Check for mana
        if (!playerManager.hasEnoughMana(player, manaCost)) {
            player.sendActionBar(plugin.getMiniMessage().deserialize("<red>Not enough mana!</red>"));
            return;
        }

        // All checks passed, execute the ability
        abilityManager.getAbility(abilityKey).ifPresent(ability -> {
            // Spend mana
            playerManager.spendMana(player, manaCost);

            // Set cooldown
            cooldownManager.setCooldown(player.getUniqueId(), abilityKey, abilityInfo.cooldownSeconds());

            // Execute ability logic
            ability.execute(player, item);
        });
    }
}
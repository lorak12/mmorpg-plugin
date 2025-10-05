package org.nakii.mmorpg.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility for creating ItemStacks with less boilerplate code.
 *
 * Example Usage:
 * ItemStack coolItem = new ItemBuilder(Material.DIAMOND_SWORD)
 *     .name("&cSuper Sword")
 *     .lore("&7This is a very sharp sword.", "&7Be careful with it.")
 *     .build();
 */
public class ItemBuilder {

    private final ItemStack itemStack;

    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
    }

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
    }

    public ItemBuilder name(String name) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

    public ItemBuilder lore(List<String> lore) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            List<String> coloredLore = lore.stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .collect(Collectors.toList());
            meta.setLore(coloredLore);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder addLore(String line) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder amount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder material(Material material) {
        itemStack.setType(material);
        return this;
    }

    public ItemStack build() {
        return this.itemStack;
    }
}
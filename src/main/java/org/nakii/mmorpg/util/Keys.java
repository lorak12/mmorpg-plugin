package org.nakii.mmorpg.util;

import org.bukkit.NamespacedKey;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.player.Stat;

/**
 * A centralized repository for all PersistentDataContainer keys used throughout the plugin.
 */
public final class Keys {

    private static final MMORPGCore plugin = MMORPGCore.getInstance();

    // --- Item Data ---
    public static final NamespacedKey ITEM_ID = new NamespacedKey(plugin, "item_id");
    public static final NamespacedKey RARITY = new NamespacedKey(plugin, "rarity");
    public static final NamespacedKey BASE_STATS = new NamespacedKey(plugin, "base_stats");
    public static final NamespacedKey REQUIREMENTS = new NamespacedKey(plugin, "requirements");
    public static final NamespacedKey ARMOR_SET_ID = new NamespacedKey(plugin, "armor_set_id");
    public static final NamespacedKey ARMOR_SET_STATS = new NamespacedKey(plugin, "armor_set_stats");
    public static final NamespacedKey REFORGE_ID = new NamespacedKey(plugin, "reforge_id");
    public static final NamespacedKey REFORGE_STATS = new NamespacedKey(plugin, "reforge_stats");
    public static final NamespacedKey PRISTINE_ITEM = new NamespacedKey(plugin, "pristine_item");
    public static final NamespacedKey ABILITY_KEY = new NamespacedKey(plugin, "ability_key");
    public static final NamespacedKey CUSTOM_ENCHANTS = new NamespacedKey(plugin, "custom_enchants");

    // --- Mob Data ---
    public static final NamespacedKey MOB_ID = new NamespacedKey(plugin, "mob_id");
    public static final NamespacedKey MOB_CATEGORY = new NamespacedKey(plugin, "mob_category");

    // --- Misc ---
    public static final NamespacedKey ZONE_WAND = new NamespacedKey(plugin, "zone_wand");
    public static final NamespacedKey LAST_HIT_CRIT = new NamespacedKey(plugin, "mmorpg_last_hit_crit");
    public static final NamespacedKey BYPASS_DEFENSE = new NamespacedKey(plugin, "mmorpg_bypass_defense");


    // Private constructor to prevent instantiation.
    private Keys() {}

    /**
     * Creates a standardized NamespacedKey for a given stat to be stored on a mob.
     * @param stat The stat to create a key for.
     * @return The NamespacedKey, e.g., "mmorpg:stat_health".
     */
    public static NamespacedKey mobStatKey(Stat stat) {
        return new NamespacedKey(plugin, "stat_" + stat.name().toLowerCase());
    }
}
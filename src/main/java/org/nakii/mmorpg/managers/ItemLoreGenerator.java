package org.nakii.mmorpg.managers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.item.CustomItemTemplate;
import org.nakii.mmorpg.item.Rarity;
import org.nakii.mmorpg.player.Stat;
import org.nakii.mmorpg.requirements.Requirement;
import org.nakii.mmorpg.utils.ChatUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ItemLoreGenerator {

    private final MMORPGCore plugin;
    private final Gson gson = new Gson();
    private final Type statMapType = new TypeToken<Map<String, Double>>(){}.getType();

    // Defines the order in which stats should appear in the lore.
    private static final List<Stat> STAT_DISPLAY_ORDER = List.of(
            Stat.DAMAGE, Stat.STRENGTH, Stat.CRIT_CHANCE, Stat.CRIT_DAMAGE, Stat.BONUS_ATTACK_SPEED,
            Stat.HEALTH, Stat.DEFENSE, Stat.SPEED, Stat.INTELLIGENCE, Stat.ABILITY_DAMAGE,
            Stat.MINING_SPEED, Stat.MINING_FORTUNE
    );

    public ItemLoreGenerator(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    public void updateLore(ItemStack item, @Nullable Player viewer) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        var data = meta.getPersistentDataContainer();

        String itemId = data.get(ItemManager.ITEM_ID_KEY, PersistentDataType.STRING);
        if (itemId == null) return;

        CustomItemTemplate template = plugin.getItemManager().getTemplate(itemId);
        if (template == null) return;

        Rarity rarity = template.getRarity();
        List<Component> lore = new ArrayList<>();

        // --- 1. Get All Stat Sources ---
        Map<Stat, Double> baseStats = readStatsFromNBT(data, ItemManager.BASE_STATS_KEY);
        Map<Stat, Double> reforgeStats = readStatsFromNBT(data, ItemManager.REFORGE_STATS_KEY);
        Map<Stat, Double> totalStats = new EnumMap<>(Stat.class);
        baseStats.forEach((stat, value) -> totalStats.merge(stat, value, Double::sum));
        reforgeStats.forEach((stat, value) -> totalStats.merge(stat, value, Double::sum));

        // --- 2. Build Special Lore (e.g., Breaking Power) ---
        if (totalStats.containsKey(Stat.BREAKING_POWER)) {
            lore.add(ChatUtils.format("<gray>Breaking Power " + totalStats.get(Stat.BREAKING_POWER).intValue() + "</gray>"));
        }

        if (!template.getStaticLore().isEmpty()) {
            template.getStaticLore().forEach(line -> lore.add(ChatUtils.format(line)));
        }

        if (lore.size() > 0) lore.add(Component.empty());

        // --- 3. Build the Stat Block ---
        boolean hasStatBlock = false;
        for (Stat stat : STAT_DISPLAY_ORDER) {
            if (totalStats.containsKey(stat)) {
                hasStatBlock = true;
                double value = totalStats.get(stat);
                String sign = value >= 0 ? "+" : "";
                String color = switch (stat) {
                    case DAMAGE, STRENGTH, CRIT_DAMAGE, BONUS_ATTACK_SPEED -> "<red>";
                    default -> "<green>";
                };
                String suffix = (stat == Stat.CRIT_CHANCE || stat == Stat.BONUS_ATTACK_SPEED) ? "%" : "";
                String statLine = String.format("<gray>%s</gray> %s%s%.0f%s",
                        stat.getSimpleName(), color, sign, value, suffix);
                lore.add(ChatUtils.format(statLine));
            }
        }
        if (hasStatBlock) lore.add(Component.empty());

        // --- 4. Enchantment Block ---
        String enchantLine = plugin.getEnchantmentManager().getFormattedEnchantLine(item, viewer);
        if (enchantLine != null && !enchantLine.isEmpty()) {
            lore.add(ChatUtils.format(enchantLine));
            lore.add(Component.empty());
        }

        // --- 5. Armor Set, Ability, and Passive Blocks ---
        template.getArmorSetInfo().ifPresent(info -> {
            String header = String.format("<gold>Full Set Bonus: %s</gold> <gray>(0/4)</gray>", info.name());
            lore.add(ChatUtils.format(header));
            info.bonusDescription().forEach(line -> lore.add(ChatUtils.format(line)));
            lore.add(Component.empty());
        });

        // (Add Ability and Passive blocks here in the same way)

        // --- 6. Unmet Requirements and Reforge Text ---
        boolean meetsAllReqs = true;
        if (viewer != null) {
            List<String> reqStrings = template.getRequirements();
            for (String reqString : reqStrings) {
                Requirement req = Requirement.fromString(reqString);
                if (req != null && !req.meets(viewer)) {
                    meetsAllReqs = false;
                    lore.add(ChatUtils.format("<red>Requires " + reqString.replace(":", " ") + "</red>"));
                }
            }
        }

        String reforgeId = data.get(ItemManager.REFORGE_ID_KEY, PersistentDataType.STRING);
        if (reforgeId == null && template.hasFlag("REFORGABLE")) {
            lore.add(ChatUtils.format("<dark_gray>This item can be reforged.</dark_gray>"));
        }
        if (!meetsAllReqs || (reforgeId == null && template.hasFlag("REFORGABLE"))) {
            lore.add(Component.empty());
        }

        // --- 7. Rarity Footer ---
        String itemType = template.getItemType();
        String footer = rarity.getColorTag() + "<b>" + rarity.name() + (itemType != null ? " " + itemType : "") + "</b>";
        lore.add(ChatUtils.format(footer));

        // --- 8. Final Meta Application ---
        String finalName = (reforgeId != null ? reforgeId + " " : "") + template.getDisplayName();
        meta.displayName(ChatUtils.format(rarity.getColorTag() + finalName));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
    }

    private Map<Stat, Double> readStatsFromNBT(PersistentDataContainer data, NamespacedKey key) {
        Map<Stat, Double> stats = new EnumMap<>(Stat.class);
        if (data.has(key, PersistentDataType.STRING)) {
            String json = data.get(key, PersistentDataType.STRING);
            Map<String, Double> stringMap = gson.fromJson(json, statMapType);
            for (Map.Entry<String, Double> entry : stringMap.entrySet()) {
                try {
                    stats.put(Stat.valueOf(entry.getKey()), entry.getValue());
                } catch (IllegalArgumentException e) { /* Ignore invalid stats */ }
            }
        }
        return stats;
    }
}
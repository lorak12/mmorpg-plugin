package org.nakii.mmorpg.managers;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.skills.PlayerSkillData;
import org.nakii.mmorpg.skills.Skill;
import org.nakii.mmorpg.stats.PlayerStats;
import org.nakii.mmorpg.stats.StatBreakdown;
import org.nakii.mmorpg.utils.ChatUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GUIManager {

    private final MMORPGCore plugin;

    private static final DecimalFormat df = new DecimalFormat("#,###.#");

    public GUIManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    // --- NEW: Main Stats GUI ---
    /**
     * Opens the main stats menu, which acts as a hub for different stat categories.
     * @param player The player to open the GUI for.
     */
    public void openStatsGUI(Player player) {
        String title = plugin.getConfig().getString("guis.stats.main.title", "<dark_gray>Your Stats</dark_gray>");
        int rows = plugin.getConfig().getInt("guis.stats.main.rows", 3);

        Gui gui = Gui.gui().title(ChatUtils.format(title)).rows(rows).disableAllInteractions().create();

        // Combat Stats Category
        GuiItem combatItem = ItemBuilder.from(Material.DIAMOND_SWORD)
                .name(ChatUtils.format("<red>Combat Stats</red>"))
                .lore(ChatUtils.format("<gray>View your core combat statistics.</gray>"))
                .asGuiItem(event -> openCombatStatsGUI(player)); // Click handler
        gui.setItem(11, combatItem);

        // Gathering Stats Category
        GuiItem gatheringItem = ItemBuilder.from(Material.DIAMOND_PICKAXE)
                .name(ChatUtils.format("<gold>Gathering Stats</gold>"))
                .lore(ChatUtils.format("<gray>View your foraging, mining, and</gray>"), ChatUtils.format("<gray>farming statistics.</gray>"))
                .asGuiItem(event -> openGatheringStatsGUI(player)); // Click handler
        gui.setItem(13, gatheringItem);

        // TODO: Add items for Wisdom, Misc, and Fishing stats here

        gui.open(player);
    }

    // --- NEW: Combat Stats Sub-Menu ---
    private void openCombatStatsGUI(Player player) {
        String title = plugin.getConfig().getString("guis.stats.combat.title", "<dark_gray>Combat Stats</dark_gray>");
        int rows = plugin.getConfig().getInt("guis.stats.combat.rows", 4);
        Gui gui = Gui.gui().title(ChatUtils.format(title)).rows(rows).disableAllInteractions().create();

        StatBreakdown breakdown = plugin.getStatsManager().getStatBreakdown(player);

        // Add each stat item
        gui.setItem(10, createStatItem(Material.RED_BED, "❤ Health", breakdown, PlayerStats::getHealth));
        gui.setItem(11, createStatItem(Material.SHIELD, "❈ Defense", breakdown, PlayerStats::getDefense));
        gui.setItem(12, createStatItem(Material.BLAZE_POWDER, "❁ Strength", breakdown, PlayerStats::getStrength));
        gui.setItem(13, createStatItem(Material.LAPIS_LAZULI, "✎ Intelligence", breakdown, PlayerStats::getIntelligence));
        gui.setItem(14, createStatItem(Material.ARROW, "☣ Crit Chance", breakdown, PlayerStats::getCritChance, "%"));
        gui.setItem(15, createStatItem(Material.TNT, "☠ Crit Damage", breakdown, PlayerStats::getCritDamage, "%"));
        gui.setItem(16, createStatItem(Material.SUGAR, "⚔ Attack Speed", breakdown, PlayerStats::getBonusAttackSpeed, "%"));
        gui.setItem(19, createStatItem(Material.IRON_SWORD, "๑ Ability Damage", breakdown, PlayerStats::getAbilityDamage));
        gui.setItem(20, createStatItem(Material.DIAMOND_CHESTPLATE, "❂ True Defense", breakdown, PlayerStats::getTrueDefense));
        gui.setItem(21, createStatItem(Material.GHAST_TEAR, "⫽ Ferocity", breakdown, PlayerStats::getFerocity));

        // Back Button
        gui.setItem(rows * 9 - 5, ItemBuilder.from(Material.ARROW).name(ChatUtils.format("<green>Go Back</green>")).asGuiItem(event -> openStatsGUI(player)));

        gui.open(player);
    }

    // --- NEW: Gathering Stats Sub-Menu ---
    private void openGatheringStatsGUI(Player player) {
        String title = plugin.getConfig().getString("guis.stats.gathering.title", "<dark_gray>Gathering Stats</dark_gray>");
        int rows = plugin.getConfig().getInt("guis.stats.gathering.rows", 3);
        Gui gui = Gui.gui().title(ChatUtils.format(title)).rows(rows).disableAllInteractions().create();

        StatBreakdown breakdown = plugin.getStatsManager().getStatBreakdown(player);

        gui.setItem(11, createStatItem(Material.DIAMOND_PICKAXE, "⸕ Mining Speed", breakdown, PlayerStats::getMiningSpeed));
        gui.setItem(12, createStatItem(Material.NETHERITE_PICKAXE, "Ⓟ Breaking Power", breakdown, PlayerStats::getBreakingPower));
        gui.setItem(13, createStatItem(Material.WHEAT, "☘ Farming Fortune", breakdown, PlayerStats::getFarmingFortune));
        gui.setItem(14, createStatItem(Material.OAK_LOG, "☘ Foraging Fortune", breakdown, PlayerStats::getForagingFortune));
        gui.setItem(15, createStatItem(Material.DIAMOND, "☘ Mining Fortune", breakdown, PlayerStats::getMiningFortune));

        // Back Button
        gui.setItem(rows * 9 - 5, ItemBuilder.from(Material.ARROW).name(ChatUtils.format("<green>Go Back</green>")).asGuiItem(event -> openStatsGUI(player)));

        gui.open(player);
    }

    // --- NEW: Helper method to create a stat item ---
    @FunctionalInterface
    private interface StatGetter { double get(PlayerStats stats); }

    private GuiItem createStatItem(Material material, String name, StatBreakdown breakdown, StatGetter getter, String suffix) {
        double total = getter.get(breakdown.getTotalStats());
        double base = getter.get(breakdown.getBaseStats());
        double item = getter.get(breakdown.getItemStats());
        double skill = getter.get(breakdown.getSkillStats());

        List<String> lore = new ArrayList<>(Arrays.asList(
                "<gray>Your total " + name.substring(2) + " value.",
                "",
                "<white>Base: <green>" + df.format(base) + suffix + "</green></white>",
                "<white>Items: <red>+" + df.format(item) + suffix + "</red></white>",
                "<white>Skills: <red>+" + df.format(skill) + suffix + "</red></white>"
        ));

        return ItemBuilder.from(material)
                .name(ChatUtils.format(name + ": <green>" + df.format(total) + suffix + "</green>"))
                .lore(ChatUtils.formatList(lore))
                .asGuiItem();
    }

    // Overloaded helper for stats without a suffix
    private GuiItem createStatItem(Material material, String name, StatBreakdown breakdown, StatGetter getter) {
        return createStatItem(material, name, breakdown, getter, "");
    }

    /**
     * Creates and opens the main skills GUI for a player.
     * @param player The player to open the GUI for.
     */
    public void openSkillsGUI(Player player) {
        String title = plugin.getConfig().getString("guis.skills.title", "<dark_gray>Your Skills</dark_gray>");
        int rows = plugin.getConfig().getInt("guis.skills.rows", 4);

        // Create the GUI using Triumph-GUI
        Gui gui = Gui.gui()
                .title(ChatUtils.format(title))
                .rows(rows)
                .disableAllInteractions() // This makes it a view-only menu
                .create();

        // Get player's skill data
        PlayerSkillData skillData = plugin.getSkillManager().getSkillData(player);
        if (skillData == null) {
            player.sendMessage(ChatUtils.format("<red>Could not load your skill data."));
            return;
        }

        // Populate the GUI with an item for each skill
        int[] slots = {10, 11, 12, 13, 19, 20, 21, 22}; // Example layout
        int slotIndex = 0;
        for (Skill skill : Skill.values()) {
            if (slotIndex >= slots.length) break;

            int level = skillData.getLevel(skill);
            double currentXp = skillData.getXp(skill);
            // NOTE: You will need a method in SkillManager to get the XP required for the next level.
            // For now, we'll use a placeholder.
            double xpForNextLevel = plugin.getSkillManager().getXpForLevel(level + 1);

            // Build the lore for the item
            List<Component> lore = buildSkillLore(skill, level, currentXp, xpForNextLevel);

            // Build the item and add it to the GUI
            GuiItem skillItem = ItemBuilder.from(getSkillMaterial(skill))
                    .name(ChatUtils.format("<green><b>" + capitalize(skill.name()) + "</b></green>"))
                    .lore(lore)
                    .asGuiItem();

            gui.setItem(slots[slotIndex], skillItem);
            slotIndex++;
        }

        // Open the GUI for the player
        gui.open(player);
    }

    /**
     * Builds the detailed lore for a skill item in the GUI.
     */
    private List<Component> buildSkillLore(Skill skill, int level, double currentXp, double xpForNextLevel) {
        List<String> loreLines = new ArrayList<>();

        loreLines.add("<dark_gray>Level " + level + "</dark_gray>");
        loreLines.add("");

        // Progress Bar
        String progress = String.format("%,.0f / %,.0f", currentXp, xpForNextLevel);
        loreLines.add("<gray>Progress: " + progress + "</gray>");
        loreLines.add(createProgressBar(currentXp, xpForNextLevel));
        loreLines.add("");

        // Rewards (This part will need to be configured in skills.yml later)
        loreLines.add("<white><b>Rewards:</b></white>");
        switch (skill) {
            case COMBAT:
                loreLines.add("<gray>+<red>" + plugin.getConfig().getDouble("skills.combat.rewards.crit_chance_per_level", 0.5) + "% ☣ Crit Chance</red> per level.</gray>");
                break;
            case FARMING:
                loreLines.add("<gray>+<red>" + plugin.getConfig().getInt("skills.farming.rewards.health_per_level", 2) + " ❤ Health</red> per level.</gray>");
                break;
            // Add cases for all other skills...
        }

        return ChatUtils.formatList(loreLines);
    }

    /**
     * Creates a textual progress bar.
     */
    private String createProgressBar(double current, double max) {
        if (max <= 0) max = 1; // Prevent division by zero
        double percent = current / max;
        int barWidth = 20; // The total width of the bar in characters

        int filledWidth = (int) (barWidth * percent);
        int emptyWidth = barWidth - filledWidth;

        return "<green>" + "■".repeat(filledWidth) + "</gray>" + "■".repeat(emptyWidth);
    }

    /**
     * Gets a representative Material for each skill for the GUI icon.
     */
    private Material getSkillMaterial(Skill skill) {
        switch (skill) {
            case COMBAT: return Material.DIAMOND_SWORD;
            case MINING: return Material.DIAMOND_PICKAXE;
            case FARMING: return Material.GOLDEN_HOE;
            case FORAGING: return Material.OAK_SAPLING;
            case FISHING: return Material.FISHING_ROD;
            case ENCHANTING: return Material.ENCHANTING_TABLE;
            case ALCHEMY: return Material.BREWING_STAND;
            case CARPENTRY: return Material.CRAFTING_TABLE;
            default: return Material.STONE;
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
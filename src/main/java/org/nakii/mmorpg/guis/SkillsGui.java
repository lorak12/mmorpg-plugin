package org.nakii.mmorpg.guis;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.skills.PlayerSkillData;
import org.nakii.mmorpg.skills.Skill;
import java.util.ArrayList;
import java.util.List;

public class SkillsGui extends AbstractGui {

    public SkillsGui(MMORPGCore plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public String getTitle() { return "<dark_gray><b>Your Skills</b></dark_gray>"; }

    @Override
    public int getSize() { return 54; } // A 54-slot inventory (6 rows)

    @Override
    public void populateItems() {
        drawBaseLayout(); // This will draw the border and standard controls

        PlayerSkillData skillData = plugin.getSkillManager().getSkillData(player);
        if (skillData == null) return;

        // Define the slots where the skill icons will be placed
        int[] skillSlots = {10, 11, 12, 13, 19, 20, 21, 22};
        Skill[] skills = Skill.values();

        for (int i = 0; i < skillSlots.length; i++) {
            if (i >= skills.length) break; // Stop if we have more slots than skills

            Skill skill = skills[i];
            int level = skillData.getLevel(skill);
            double currentXp = skillData.getXp(skill);
            double xpForNextLevel = plugin.getSkillManager().getXpForLevel(level + 1);

            List<String> lore = buildSkillLore(skill, level, currentXp, xpForNextLevel);

            // Use our corrected createItem helper method
            inventory.setItem(skillSlots[i], createItem(getSkillMaterial(skill), "<green><b>" + capitalize(skill.name()) + "</b></green>", lore));
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        // We just want a view-only menu, so the default behavior in AbstractGui
        // (cancelling the click and handling pagination/close) is perfect.
        super.handleClick(event);
    }

    // --- HELPER METHODS ---

    private List<String> buildSkillLore(Skill skill, int level, double currentXp, double xpForNextLevel) {
        List<String> loreLines = new ArrayList<>();

        loreLines.add("<dark_gray>Level " + level + "</dark_gray>");
        loreLines.add("");

        String progress = String.format("%,.0f / %,.0f", currentXp, xpForNextLevel);
        loreLines.add("<gray>Progress to Level " + (level + 1) + ":</gray>");
        loreLines.add(createProgressBar(currentXp, xpForNextLevel) + " <gray>" + progress + "</gray>");
        loreLines.add("");

        loreLines.add("<white><b>Rewards:</b></white>");
        // This can be expanded to pull from a config
        switch (skill) {
            case COMBAT:
                loreLines.add("<gray>+<red>0.5% ☣ Crit Chance</red> per level.</gray>");
                break;
            case FARMING:
                loreLines.add("<gray>+<red>2 ❤ Health</red> per level.</gray>");
                break;
            case MINING:
                loreLines.add("<gray>+<green>1 ❈ Defense</green> per level.</gray>");
                break;
            case FORAGING:
                loreLines.add("<gray>+<red>1 ❁ Strength</red> per level.</gray>");
                break;
            // Add other skill rewards here...
            default:
                loreLines.add("<gray>Various rewards!</gray>");
        }

        return loreLines;
    }

    private String createProgressBar(double current, double max) {
        if (max <= 0) max = 1;
        double percent = current / max;
        int barWidth = 20;

        int filledWidth = (int) (barWidth * percent);
        int emptyWidth = barWidth - filledWidth;

        return "<green>" + "■".repeat(filledWidth) + "</gray>" + "■".repeat(emptyWidth);
    }

    private Material getSkillMaterial(Skill skill) {
        return switch (skill) {
            case COMBAT -> Material.DIAMOND_SWORD;
            case MINING -> Material.DIAMOND_PICKAXE;
            case FARMING -> Material.GOLDEN_HOE;
            case FORAGING -> Material.OAK_SAPLING;
            case FISHING -> Material.FISHING_ROD;
            case ENCHANTING -> Material.ENCHANTING_TABLE;
            case ALCHEMY -> Material.BREWING_STAND;
            case CARPENTRY -> Material.CRAFTING_TABLE;
        };
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
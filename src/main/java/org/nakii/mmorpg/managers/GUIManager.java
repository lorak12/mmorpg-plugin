package org.nakii.mmorpg.managers;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.skills.PlayerSkillData;
import org.nakii.mmorpg.skills.Skill;
import org.nakii.mmorpg.stats.StatBreakdown;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GUIManager implements InventoryHolder {

    private final MMORPGCore plugin;
    private static final DecimalFormat df = new DecimalFormat("#,###.##");

    public GUIManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    // This method is required by InventoryHolder but we can leave it empty
    // as we handle GUIs dynamically.
    @Override
    public Inventory getInventory() {
        return null;
    }

    // --- GUI Creation ---

    public void openStatsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(this, 36, "Your Stats");
        StatBreakdown breakdown = plugin.getStatsManager().getStatBreakdown(player);

        gui.setItem(10, createStatItem(Material.APPLE, "Health", breakdown.getTotalHealth(), breakdown.getBaseHealth(), breakdown.getItemHealth(), breakdown.getSkillHealth()));
        gui.setItem(11, createStatItem(Material.DIAMOND_SWORD, "Strength", breakdown.getTotalStrength(), breakdown.getBaseStrength(), breakdown.getItemStrength(), breakdown.getSkillStrength()));
        gui.setItem(12, createStatItem(Material.NETHER_STAR, "Crit Chance", breakdown.getTotalCritChance(), breakdown.getBaseCritChance(), breakdown.getItemCritChance(), breakdown.getSkillCritChance(), "%"));
        gui.setItem(13, createStatItem(Material.TNT, "Crit Damage", breakdown.getTotalCritDamage(), breakdown.getBaseCritDamage(), breakdown.getItemCritDamage(), breakdown.getSkillCritDamage(), "%"));
        gui.setItem(14, createStatItem(Material.FEATHER, "Speed", breakdown.getTotalSpeed(), breakdown.getBaseSpeed(), breakdown.getItemSpeed(), breakdown.getSkillSpeed(), "%"));
        gui.setItem(15, createStatItem(Material.GOLD_INGOT, "Luck", breakdown.getTotalLuck(), breakdown.getBaseLuck(), breakdown.getItemLuck(), breakdown.getSkillLuck()));
        gui.setItem(16, createStatItem(Material.IRON_SWORD, "Lethality", breakdown.getTotalLethality(), breakdown.getBaseLethality(), breakdown.getItemLethality(), breakdown.getSkillLethality()));

        gui.setItem(19, createStatItem(Material.SHIELD, "Tenacity", breakdown.getTotalTenacity(), breakdown.getBaseTenacity(), breakdown.getItemTenacity(), breakdown.getSkillTenacity()));
        gui.setItem(20, createStatItem(Material.SPECTRAL_ARROW, "Armor Penetration", breakdown.getTotalArmorPen(), breakdown.getBaseArmorPen(), breakdown.getItemArmorPen(), breakdown.getSkillArmorPen(), "%"));
        gui.setItem(21, createStatItem(Material.IRON_CHESTPLATE, "Armor", breakdown.getTotalArmor(), breakdown.getBaseArmor(), breakdown.getItemArmor(), breakdown.getSkillArmor()));
        gui.setItem(22, createStatItem(Material.LAPIS_LAZULI, "Mana", breakdown.getTotalMana(), breakdown.getBaseMana(), breakdown.getItemMana(), breakdown.getSkillMana()));
        gui.setItem(23, createStatItem(Material.GLOWSTONE_DUST, "Mana Regen", breakdown.getTotalManaRegen(), breakdown.getBaseManaRegen(), breakdown.getItemManaRegen(), breakdown.getSkillManaRegen(), "/s"));
        gui.setItem(24, createStatItem(Material.GHAST_TEAR, "HP Regen", breakdown.getTotalHpRegen(), breakdown.getBaseHpRegen(), breakdown.getItemHpRegen(), breakdown.getSkillHpRegen(), "/s"));

        player.openInventory(gui);
    }

    public void openSkillsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(this, 27, "Skills");
        PlayerSkillData skillData = plugin.getSkillManager().getSkillData(player);

        gui.setItem(10, createSkillItem(player, skillData, Skill.COMBAT, Material.DIAMOND_SWORD));
        gui.setItem(11, createSkillItem(player, skillData, Skill.ARCHERY, Material.BOW));
        gui.setItem(12, createSkillItem(player, skillData, Skill.MINING, Material.DIAMOND_PICKAXE));
        gui.setItem(13, createSkillItem(player, skillData, Skill.FARMING, Material.WHEAT));
        gui.setItem(14, createSkillItem(player, skillData, Skill.FISHING, Material.FISHING_ROD));
        gui.setItem(15, createSkillItem(player, skillData, Skill.FORGING, Material.ANVIL));
        gui.setItem(16, createSkillItem(player, skillData, Skill.CRAFTING, Material.CRAFTING_TABLE));
        // Add other skills similarly...

        player.openInventory(gui);
    }


    // --- Item Creation Helpers ---

    private ItemStack createStatItem(Material material, String name, double total, double base, double fromItems, double fromSkills) {
        return createStatItem(material, name, total, base, fromItems, fromSkills, "");
    }

    private ItemStack createStatItem(Material material, String name, double total, double base, double fromItems, double fromSkills, String suffix) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + name);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "Total: " + ChatColor.GREEN + df.format(total) + suffix);
        lore.add(ChatColor.DARK_GRAY + "--------------------");
        lore.add(ChatColor.GRAY + "Base: " + ChatColor.YELLOW + df.format(base) + suffix);
        lore.add(ChatColor.GRAY + "From Items: " + ChatColor.YELLOW + df.format(fromItems) + suffix);
        lore.add(ChatColor.GRAY + "From Skills: " + ChatColor.YELLOW + df.format(fromSkills) + suffix);

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createSkillItem(Player player, PlayerSkillData data, Skill skill, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        int level = data.getLevel(skill);
        double currentXp = data.getExperience(skill);
        double neededXp = plugin.getSkillManager().getExperienceForLevel(skill, level);

        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + skill.name());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "Level: " + ChatColor.GREEN + level);
        lore.add(ChatColor.GRAY + "XP: " + ChatColor.AQUA + df.format(currentXp) + ChatColor.GRAY + " / " + ChatColor.YELLOW + df.format(neededXp));
        lore.add(createProgressBar(currentXp, neededXp));

        meta.setLore(lore);
        // Add a persistent data tag to identify the skill on click
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "skill_id"), PersistentDataType.STRING, skill.name());
        item.setItemMeta(meta);
        return item;
    }

    private String createProgressBar(double current, double max) {
        int totalBars = 20;
        double percent = current / max;
        int progressBars = (int) (totalBars * percent);

        StringBuilder bar = new StringBuilder();
        bar.append(ChatColor.GREEN);
        for (int i = 0; i < progressBars; i++) {
            bar.append("■");
        }
        bar.append(ChatColor.GRAY);
        for (int i = 0; i < totalBars - progressBars; i++) {
            bar.append("■");
        }
        return bar.toString();
    }
}
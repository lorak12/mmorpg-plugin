package org.nakii.mmorpg.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.crafting.CustomRecipe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecipeManager {

    private final MMORPGCore plugin;
    private final List<CustomRecipe> customRecipes = new ArrayList<>();

    public RecipeManager(MMORPGCore plugin) {
        this.plugin = plugin;
        loadRecipes();
    }

    public void loadRecipes() {
        customRecipes.clear();
        File recipesFile = new File(plugin.getDataFolder(), "recipes.yml");
        if (!recipesFile.exists()) {
            plugin.saveResource("recipes.yml", false);
        }
        FileConfiguration recipeConfig = YamlConfiguration.loadConfiguration(recipesFile);

        ConfigurationSection recipesSection = recipeConfig.getConfigurationSection("recipes");
        if (recipesSection == null) return;

        for (String key : recipesSection.getKeys(false)) {
            ConfigurationSection recipeData = recipesSection.getConfigurationSection(key);
            if (recipeData == null) continue;

            String resultItemId = recipeData.getString("result");
            if (resultItemId == null) continue;

            ItemStack result = plugin.getItemManager().createItem(resultItemId, recipeData.getInt("amount", 1));
            if (result == null) {
                plugin.getLogger().warning("Invalid recipe result: " + resultItemId);
                continue;
            }

            CustomRecipe customRecipe = new CustomRecipe(key, result, recipeData);
            customRecipes.add(customRecipe);
        }
        plugin.getLogger().info("Loaded " + customRecipes.size() + " custom recipes.");
    }

    public ItemStack checkCraftingMatrix(CraftingInventory inventory) {
        for (CustomRecipe recipe : customRecipes) {
            if (recipe.matches(inventory)) {
                return recipe.getResult();
            }
        }
        return null;
    }
}
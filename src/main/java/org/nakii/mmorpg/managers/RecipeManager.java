package org.nakii.mmorpg.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.crafting.CustomRecipe;
import org.nakii.mmorpg.crafting.ShapedRecipe;
import org.nakii.mmorpg.crafting.ShapelessRecipe;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeManager {

    private final MMORPGCore plugin;
    private final List<CustomRecipe> customRecipes = new ArrayList<>();
    private final ItemManager itemManager;
    private final RequirementManager requirementManager;

    public RecipeManager(MMORPGCore plugin, ItemManager itemManager, RequirementManager requirementManager) {
        this.plugin = plugin;
        this.itemManager = itemManager;
        this.requirementManager = requirementManager;
        loadRecipes();
    }

    // A simple record to return the result of a recipe match.
    public record RecipeMatch(CustomRecipe recipe) {}

    public void loadRecipes() {
        customRecipes.clear();
        File recipesFolder = new File(plugin.getDataFolder(), "recipes");
        if (!recipesFolder.exists()) recipesFolder.mkdirs();
        loadRecipesFromDirectory(recipesFolder);
        plugin.getLogger().info("Loaded " + customRecipes.size() + " custom recipes.");
    }

    private void loadRecipesFromDirectory(File directory) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                loadRecipesFromDirectory(file);
            } else if (file.getName().endsWith(".yml")) {
                var config = YamlConfiguration.loadConfiguration(file);
                for (String key : config.getKeys(false)) {
                    ConfigurationSection recipeSection = config.getConfigurationSection(key);
                    if (recipeSection == null) continue;

                    String resultId = recipeSection.getString("result");
                    int amount = recipeSection.getInt("amount", 1);
                    String type = recipeSection.getString("type", "SHAPED").toUpperCase();
                    double xp = recipeSection.getDouble("carpentry-xp", 0);
                    List<String> requirements = recipeSection.getStringList("requirements");

                    CustomRecipe recipe = null;
                    if (type.equals("SHAPED")) {
                        List<String> shape = recipeSection.getStringList("shape");
                        Map<String, String> ingredients = new HashMap<>();
                        ConfigurationSection ingredientsSection = recipeSection.getConfigurationSection("ingredients");
                        if (ingredientsSection != null) {
                            for (String ingKey : ingredientsSection.getKeys(false)) {
                                ingredients.put(ingKey, ingredientsSection.getString(ingKey));
                            }
                        }
                        recipe = new ShapedRecipe(resultId, amount, requirements, xp, shape, ingredients, itemManager, requirementManager);
                    } else if (type.equals("SHAPELESS")) {
                        List<String> ingredients = recipeSection.getStringList("ingredients");
                        recipe = new ShapelessRecipe(resultId, amount, requirements, xp, ingredients, itemManager, requirementManager);
                    }

                    if (recipe != null) {
                        customRecipes.add(recipe);
                    }
                }
            }
        }
    }

    /**
     * Finds a matching custom recipe for the given 3x3 crafting grid.
     * @param grid The 9 items from the crafting grid.
     * @return A RecipeMatch object if a recipe is found, otherwise null.
     */
    public RecipeMatch findMatch(ItemStack[] grid) {
        for (CustomRecipe recipe : customRecipes) {
            if (recipe.matches(grid)) {
                return new RecipeMatch(recipe);
            }
        }
        return null;
    }
}
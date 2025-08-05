package org.nakii.mmorpg.crafting;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.nakii.mmorpg.MMORPGCore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomRecipe {

    private final String id;
    private final ItemStack result;
    private final boolean isShaped;
    private final Map<Character, String> shapedIngredients;
    private final List<String> shapelessIngredients;
    private final String[] shape;

    public CustomRecipe(String id, ItemStack result, ConfigurationSection config) {
        this.id = id;
        this.result = result;
        this.isShaped = config.getString("type", "shaped").equalsIgnoreCase("shaped");
        this.shapedIngredients = new HashMap<>();
        this.shapelessIngredients = config.getStringList("ingredients");
        this.shape = isShaped ? config.getStringList("shape").toArray(new String[0]) : new String[0];

        if (isShaped) {
            ConfigurationSection ingredientsSection = config.getConfigurationSection("ingredients");
            if (ingredientsSection != null) {
                for (String key : ingredientsSection.getKeys(false)) {
                    shapedIngredients.put(key.charAt(0), ingredientsSection.getString(key));
                }
            }
        }
    }

    public ItemStack getResult() {
        return result.clone();
    }

    public boolean matches(CraftingInventory inventory) {
        ItemStack[] matrix = inventory.getMatrix(); // 3x3 grid
        if (isShaped) {
            return matchesShaped(matrix);
        } else {
            return matchesShapeless(matrix);
        }
    }

    private boolean matchesShaped(ItemStack[] matrix) {
        // This is a simplified check. A full implementation would check mirrored recipes etc.
        for (int y = 0; y <= 3 - shape.length; y++) {
            for (int x = 0; x <= 3 - shape[0].length(); x++) {
                if (checkShape(matrix, x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkShape(ItemStack[] matrix, int startX, int startY) {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                int shapeY = y - startY;
                int shapeX = x - startX;

                ItemStack itemInGrid = matrix[y * 3 + x];
                char shapeChar = ' ';

                if (shapeY >= 0 && shapeY < shape.length && shapeX >= 0 && shapeX < shape[shapeY].length()) {
                    shapeChar = shape[shapeY].charAt(shapeX);
                }

                String requiredItem = shapedIngredients.getOrDefault(shapeChar, "AIR");
                if (!itemMatches(itemInGrid, requiredItem)) {
                    return false;
                }
            }
        }
        return true;
    }


    private boolean matchesShapeless(ItemStack[] matrix) {
        List<String> required = new ArrayList<>(this.shapelessIngredients);
        int itemsFound = 0;

        for (ItemStack itemInGrid : matrix) {
            if (itemInGrid == null || itemInGrid.getType() == Material.AIR) {
                continue;
            }

            boolean foundMatch = false;
            for (int i = 0; i < required.size(); i++) {
                if (itemMatches(itemInGrid, required.get(i))) {
                    required.remove(i);
                    foundMatch = true;
                    itemsFound++;
                    break;
                }
            }
            // If an item in the grid is not in the recipe, it fails
            if (!foundMatch) return false;
        }

        // If we found all required items and there are no extra items, it's a match
        return required.isEmpty();
    }


    private boolean itemMatches(ItemStack item, String requiredItem) {
        boolean itemIsEmpty = (item == null || item.getType() == Material.AIR);
        boolean requiredIsEmpty = requiredItem.equalsIgnoreCase("AIR");

        if (itemIsEmpty && requiredIsEmpty) return true;
        if (itemIsEmpty || requiredIsEmpty) return false;

        if (requiredItem.startsWith("custom:")) {
            String customId = requiredItem.substring(7);
            if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(MMORPGCore.getInstance(), "item_id"), PersistentDataType.STRING)) {
                String itemId = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(MMORPGCore.getInstance(), "item_id"), PersistentDataType.STRING);
                return customId.equalsIgnoreCase(itemId);
            }
        } else {
            return item.getType() == Material.matchMaterial(requiredItem);
        }
        return false;
    }
}
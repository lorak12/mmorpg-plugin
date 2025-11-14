package org.nakii.mmorpg.crafting;

import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.ItemManager;
import org.nakii.mmorpg.managers.RequirementManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShapedRecipe extends CustomRecipe {

    private final Map<Character, Ingredient> ingredientMap;
    private final String[] shape;
    private final int recipeWidth;
    private final int recipeHeight;
    private final ItemManager itemManager;


    public ShapedRecipe(String resultItemId, int resultAmount, List<String> requirementStrings, double carpentryXp, List<String> shape, Map<String, String> ingredients, ItemManager itemManager, RequirementManager requirementManager) {
        super(resultItemId, resultAmount, requirementStrings, carpentryXp, requirementManager);
        this.shape = shape.toArray(new String[0]);
        this.recipeHeight = this.shape.length;
        this.recipeWidth = (this.recipeHeight > 0) ? this.shape[0].length() : 0;

        this.ingredientMap = new HashMap<>();
        ingredients.forEach((key, value) -> this.ingredientMap.put(key.charAt(0), Ingredient.fromString(value)));
        this.itemManager = itemManager;
    }

    @Override
    public boolean matches(ItemStack[] grid) {
        // We check every possible top-left starting position for the recipe in the 3x3 grid.
        for (int rowOffset = 0; rowOffset <= 3 - recipeHeight; rowOffset++) {
            for (int colOffset = 0; colOffset <= 3 - recipeWidth; colOffset++) {
                if (checkMatch(grid, rowOffset, colOffset)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkMatch(ItemStack[] grid, int rowOffset, int colOffset) {

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                int gridIndex = r * 3 + c;
                ItemStack gridItem = grid[gridIndex];

                int shapeRow = r - rowOffset;
                int shapeCol = c - colOffset;

                Character shapeChar = null;
                if (shapeRow >= 0 && shapeRow < recipeHeight && shapeCol >= 0 && shapeCol < recipeWidth) {
                    shapeChar = shape[shapeRow].charAt(shapeCol);
                }

                if (shapeChar == null || shapeChar == ' ') {
                    // This part of the grid should be empty for a match
                    if (gridItem != null && !gridItem.getType().isAir()) {
                        return false;
                    }
                } else {
                    // This part of the grid should contain the required ingredient
                    if (gridItem == null || gridItem.getType().isAir()) {
                        return false;
                    }

                    Ingredient required = ingredientMap.get(shapeChar);
                    if (required == null) return false; // Should not happen with valid config

                    String gridItemId = itemManager.getItemId(gridItem);
                    if (gridItemId == null || !gridItemId.equalsIgnoreCase(required.itemId()) || gridItem.getAmount() < required.amount()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void consumeIngredients(ItemStack[] grid) {
        // We need to find the correct offset again
        for (int rOff = 0; rOff <= 3 - recipeHeight; rOff++) {
            for (int cOff = 0; cOff <= 3 - recipeWidth; cOff++) {
                if (checkMatch(grid, rOff, cOff)) {
                    // This is the correct orientation, now consume
                    for (int r = 0; r < recipeHeight; r++) {
                        for (int c = 0; c < recipeWidth; c++) {
                            char shapeChar = shape[r].charAt(c);
                            if (shapeChar != ' ') {
                                Ingredient required = ingredientMap.get(shapeChar);
                                int gridIndex = (r + rOff) * 3 + (c + cOff);
                                ItemStack item = grid[gridIndex];
                                if (item != null) {
                                    item.setAmount(item.getAmount() - required.amount());
                                }
                            }
                        }
                    }
                    return; // Exit after consuming
                }
            }
        }
    }
}
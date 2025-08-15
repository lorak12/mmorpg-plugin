package org.nakii.mmorpg.crafting;

import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.ItemManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShapelessRecipe extends CustomRecipe {


    private final List<Ingredient> ingredients;

    public ShapelessRecipe(String resultItemId, int resultAmount, List<String> requirementStrings, double carpentryXp, List<String> ingredientStrings) {
        super(resultItemId, resultAmount, requirementStrings, carpentryXp);
        this.ingredients = ingredientStrings.stream()
                .map(Ingredient::fromString)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public boolean matches(ItemStack[] grid) {
        ItemManager itemManager = MMORPGCore.getInstance().getItemManager();

        // 1. Create a mutable list of all non-null items in the crafting grid.
        List<ItemStack> gridItems = new ArrayList<>();
        for (ItemStack item : grid) {
            if (item != null && !item.getType().isAir()) {
                gridItems.add(item);
            }
        }

        // 2. Create a map of required ingredients and their total needed amount.
        Map<String, Integer> requiredCounts = new HashMap<>();
        for (Ingredient req : this.ingredients) {
            requiredCounts.put(req.itemId(), requiredCounts.getOrDefault(req.itemId(), 0) + req.amount());
        }

        // 3. Create a map of available ingredients in the grid and their total amount.
        Map<String, Integer> availableCounts = new HashMap<>();
        for (ItemStack item : gridItems) {
            String itemId = itemManager.getItemId(item);
            if (itemId != null) {
                availableCounts.put(itemId, availableCounts.getOrDefault(itemId, 0) + item.getAmount());
            }
        }

        // 4. Check if we have enough of each required ingredient.
        for (Map.Entry<String, Integer> requiredEntry : requiredCounts.entrySet()) {
            String requiredId = requiredEntry.getKey();
            int requiredAmount = requiredEntry.getValue();
            if (availableCounts.getOrDefault(requiredId, 0) < requiredAmount) {
                return false; // Not enough of this item available in the grid
            }
        }

        // 5. Check if there are any extra, un-required items in the grid.
        // If the number of unique item types doesn't match, it's not a match.
        if (availableCounts.size() != requiredCounts.size()) {
            return false;
        }

        return true; // All checks passed
    }

    @Override
    public void consumeIngredients(ItemStack[] grid) {
        ItemManager itemManager = MMORPGCore.getInstance().getItemManager();

        // 1. Create a mutable map of how much of each ingredient we still need to consume.
        Map<String, Integer> requiredToConsume = new HashMap<>();
        for (Ingredient req : this.ingredients) {
            requiredToConsume.put(req.itemId(), requiredToConsume.getOrDefault(req.itemId(), 0) + req.amount());
        }

        // 2. Iterate through each slot in the crafting grid.
        for (ItemStack itemInSlot : grid) {
            if (itemInSlot == null || itemInSlot.getType().isAir()) {
                continue;
            }

            String itemId = itemManager.getItemId(itemInSlot);
            if (itemId == null) {
                continue;
            }

            // 3. Check if this item is one of the ingredients we need to consume.
            if (requiredToConsume.containsKey(itemId)) {
                int needed = requiredToConsume.get(itemId);
                if (needed <= 0) continue; // Already consumed enough of this type

                int amountInStack = itemInSlot.getAmount();

                // 4. Determine how much to take from this specific stack.
                int amountToTake = Math.min(needed, amountInStack);

                // 5. Subtract the amount from the item stack.
                itemInSlot.setAmount(amountInStack - amountToTake);

                // 6. Update the remaining amount we still need to consume.
                requiredToConsume.put(itemId, needed - amountToTake);
            }
        }
    }
}
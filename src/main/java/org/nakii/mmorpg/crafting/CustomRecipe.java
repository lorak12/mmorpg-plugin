package org.nakii.mmorpg.crafting;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.managers.RequirementManager;
import org.nakii.mmorpg.requirements.Requirement;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The base blueprint for all custom recipes in the plugin.
 */
public abstract class CustomRecipe {

    protected final String resultItemId;
    protected final int resultAmount;
    protected final List<String> requirements;
    protected final double carpentryXp;
    protected final RequirementManager requirementManager;

    public CustomRecipe(String resultItemId, int resultAmount, List<String> requirementStrings, double carpentryXp, RequirementManager requirementManager) {
        this.resultItemId = resultItemId;
        this.resultAmount = resultAmount;
        this.carpentryXp = carpentryXp;
        this.requirements = requirementStrings;
        this.requirementManager = requirementManager;
    }

    /**
     * Checks if the given 3x3 crafting grid matches this recipe's pattern.
     * @param grid An array of 9 ItemStacks representing the crafting grid.
     * @return True if the recipe matches, false otherwise.
     */
    public abstract boolean matches(ItemStack[] grid);

    /**
     * Checks if a player meets all requirements to craft this recipe.
     * @param player The player to check.
     * @return True if the player meets all requirements.
     */
    public boolean hasRequirements(Player player) {
        return requirementManager.meetsAll(player, requirements);
    }

    /**
     * Consumes the required ingredients from the grid for a single craft.
     * @param grid The crafting grid to consume items from.
     */
    public abstract void consumeIngredients(ItemStack[] grid);

    // Getters
    public String getResultItemId() { return resultItemId; }
    public int getResultAmount() { return resultAmount; }
    public List<String> getRequirements() { return requirements; }
    public double getCarpentryXp() { return carpentryXp; }

}
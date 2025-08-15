package org.nakii.mmorpg.crafting;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.requirements.Requirement;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The base blueprint for all custom recipes in the plugin.
 */
public abstract class CustomRecipe {

    protected final String resultItemId;
    protected final int resultAmount;
    protected final List<Requirement> requirements;
    protected final double carpentryXp;

    public CustomRecipe(String resultItemId, int resultAmount, List<String> requirementStrings, double carpentryXp) {
        this.resultItemId = resultItemId;
        this.resultAmount = resultAmount;
        this.carpentryXp = carpentryXp;
        this.requirements = requirementStrings.stream()
                .map(Requirement::fromString)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
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
        for (Requirement req : requirements) {
            if (!req.meets(player)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Consumes the required ingredients from the grid for a single craft.
     * @param grid The crafting grid to consume items from.
     */
    public abstract void consumeIngredients(ItemStack[] grid);

    // Getters
    public String getResultItemId() { return resultItemId; }
    public int getResultAmount() { return resultAmount; }
    public List<Requirement> getRequirements() { return requirements; }
    public double getCarpentryXp() { return carpentryXp; }

}
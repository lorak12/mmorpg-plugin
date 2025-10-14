package org.nakii.mmorpg.quest.quest.event.chest;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.bukkit.Location;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Nullable;

/**
 * Clears a specified chest from all items inside.
 */
public class ChestClearEvent extends AbstractChestEvent {

    /**
     * Creates a new chest clear event.
     *
     * @param variableLocation the location of the chest
     */
    public ChestClearEvent(final Variable<Location> variableLocation) {
        super(variableLocation);
    }

    @Override
    public void execute(@Nullable final Profile profile) throws QuestException {
        try {
            final InventoryHolder chest = getChest(profile);
            chest.getInventory().clear();
        } catch (final QuestException e) {
            throw new QuestException("Trying to clear chest. " + e.getMessage(), e);
        }
    }
}

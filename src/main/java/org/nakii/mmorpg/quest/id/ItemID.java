package org.nakii.mmorpg.quest.id;

import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackageManager;
import org.nakii.mmorpg.quest.api.identifier.InstructionIdentifier;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.jetbrains.annotations.Nullable;

/**
 * ID of an Item.
 */
public class ItemID extends InstructionIdentifier {

    /**
     * Create a new Item ID.
     *
     * @param packManager the quest package manager to get quest packages from
     * @param pack        the package of the item
     * @param identifier  the complete identifier of the item
     * @throws QuestException if there is no such item
     */
    public ItemID(final QuestPackageManager packManager, @Nullable final QuestPackage pack, final String identifier) throws QuestException {
        super(packManager, pack, identifier, "items", "Item");
    }
}

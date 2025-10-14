package org.nakii.mmorpg.quest.menu;

import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackageManager;
import org.nakii.mmorpg.quest.api.identifier.SectionIdentifier;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.jetbrains.annotations.Nullable;

/**
 * ID of a Menu item.
 */
public class MenuItemID extends SectionIdentifier {
    /**
     * Create a new Menu Item ID.
     *
     * @param packManager the quest package manager to get quest packages from
     * @param pack        the package of the menu item
     * @param identifier  the complete identifier of the menu item
     * @throws QuestException if there is no such item
     */
    public MenuItemID(final QuestPackageManager packManager, @Nullable final QuestPackage pack, final String identifier) throws QuestException {
        super(packManager, pack, identifier, "menu_items", "Menu Item");
    }
}

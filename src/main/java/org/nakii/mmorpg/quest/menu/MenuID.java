package org.nakii.mmorpg.quest.menu;

import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackageManager;
import org.nakii.mmorpg.quest.api.identifier.SectionIdentifier;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.jetbrains.annotations.Nullable;

/**
 * ID of a menu.
 */
public class MenuID extends SectionIdentifier {

    /**
     * Create a new Menu ID.
     *
     * @param packManager the quest package manager to get quest packages from
     * @param pack        the package of the menu
     * @param identifier  the complete identifier of the menu
     * @throws QuestException if there is no such menu
     */
    public MenuID(final QuestPackageManager packManager, @Nullable final QuestPackage pack, final String identifier) throws QuestException {
        super(packManager, pack, identifier, "menus", "Menu");
    }
}

package org.nakii.mmorpg.quest.menu.betonquest;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.online.OnlineCondition;
import org.nakii.mmorpg.quest.menu.MenuID;
import org.nakii.mmorpg.quest.menu.RPGMenu;
import org.jetbrains.annotations.Nullable;

/**
 * Checks if a player has opened a menu.
 */
public class MenuCondition implements OnlineCondition {

    /**
     * MenuID to check, null if any matches.
     */
    @Nullable
    private final Variable<MenuID> menuID;

    /**
     * Create a new menu condition.
     *
     * @param menuID the menu id to check for or null if matches any
     */
    public MenuCondition(@Nullable final Variable<MenuID> menuID) {
        this.menuID = menuID;
    }

    @Override
    public boolean check(final OnlineProfile profile) throws QuestException {
        return RPGMenu.hasOpenedMenu(profile, menuID == null ? null : menuID.getValue(profile));
    }
}

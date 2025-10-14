package org.nakii.mmorpg.quest.menu.betonquest;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.variable.online.OnlineVariable;
import org.nakii.mmorpg.quest.menu.OpenedMenu;

/**
 * Returns the title of the players currently opened menu.
 */
public class MenuVariable implements OnlineVariable {

    /**
     * Create a new opened menu variable.
     */
    public MenuVariable() {
    }

    @Override
    public String getValue(final OnlineProfile profile) {
        final OpenedMenu menu = OpenedMenu.getMenu(profile);
        if (menu == null) {
            return "";
        }
        return LegacyComponentSerializer.legacySection().serialize(menu.getTitle());
    }
}

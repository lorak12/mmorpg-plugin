package org.nakii.mmorpg.quest.menu.betonquest;

import org.nakii.mmorpg.quest.api.common.function.QuestConsumer;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.online.OnlineEvent;

/**
 * Event to open, close or update menus.
 */
public class MenuEvent implements OnlineEvent {
    /**
     * The action to do with the profile.
     */
    private final QuestConsumer<OnlineProfile> action;

    /**
     * Creates a new MenuQuestEvent.
     *
     * @param action the action to do with the profile
     */
    public MenuEvent(final QuestConsumer<OnlineProfile> action) {
        this.action = action;
    }

    @Override
    public void execute(final OnlineProfile profile) throws QuestException {
        action.accept(profile);
    }
}

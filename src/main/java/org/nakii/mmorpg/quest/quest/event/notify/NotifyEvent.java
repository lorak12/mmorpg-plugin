package org.nakii.mmorpg.quest.quest.event.notify;

import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.nakii.mmorpg.quest.api.quest.event.online.OnlineEvent;
import org.nakii.mmorpg.quest.api.text.Text;
import org.nakii.mmorpg.quest.notify.NotifyIO;

/**
 * {@link PlayerEvent} the implementation of the notify events.
 */
public class NotifyEvent implements OnlineEvent {
    /**
     * The {@link NotifyIO} to use.
     */
    private final NotifyIO notifyIO;

    /**
     * The translations to use.
     */
    private final Text text;

    /**
     * Creates a new {@link NotifyEvent}.
     *
     * @param notifyIO the {@link NotifyIO} to use
     * @param text     the text to use
     */
    public NotifyEvent(final NotifyIO notifyIO, final Text text) {
        this.notifyIO = notifyIO;
        this.text = text;
    }

    @Override
    public void execute(final OnlineProfile profile) throws QuestException {
        notifyIO.sendNotify(text.asComponent(profile), profile);
    }
}

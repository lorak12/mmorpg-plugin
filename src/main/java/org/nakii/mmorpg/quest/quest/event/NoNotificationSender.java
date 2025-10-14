package org.nakii.mmorpg.quest.quest.event;

import org.nakii.mmorpg.quest.api.common.component.VariableReplacement;
import org.nakii.mmorpg.quest.api.profile.Profile;

/**
 * Notification sender that suppresses notifications instead of sending them.
 */
public class NoNotificationSender implements NotificationSender {

    /**
     * Create the no notification sender.
     */
    public NoNotificationSender() {
    }

    @Override
    public void sendNotification(final Profile profile, final VariableReplacement... variables) {
        // null object pattern
    }
}

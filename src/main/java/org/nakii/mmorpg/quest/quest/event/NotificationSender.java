package org.nakii.mmorpg.quest.quest.event;

import org.nakii.mmorpg.quest.api.common.component.VariableReplacement;
import org.nakii.mmorpg.quest.api.profile.Profile;

/**
 * Allows sending notifications to a player.
 */
@FunctionalInterface
public interface NotificationSender {

    /**
     * Send the notification.
     *
     * @param profile   the {@link Profile} of the player to receive the notification
     * @param variables the variables to use in the notification
     */
    void sendNotification(Profile profile, VariableReplacement... variables);
}

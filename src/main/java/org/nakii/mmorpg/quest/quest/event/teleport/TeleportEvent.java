package org.nakii.mmorpg.quest.quest.event.teleport;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.online.OnlineEvent;
import org.nakii.mmorpg.quest.conversation.Conversation;
import org.bukkit.Location;

/**
 * Teleports the player to specified location.
 */
public class TeleportEvent implements OnlineEvent {
    /**
     * Location to teleport to.
     */
    private final Variable<Location> location;

    /**
     * Create a new teleport event that teleports the player to the given location.
     *
     * @param location location to teleport to
     */
    public TeleportEvent(final Variable<Location> location) {
        this.location = location;
    }

    @Override
    public void execute(final OnlineProfile profile) throws QuestException {
        final Conversation conv = Conversation.getConversation(profile);
        if (conv != null) {
            conv.endConversation();
        }
        final Location playerLocation = location.getValue(profile);
        profile.getPlayer().teleport(playerLocation);
    }
}

package org.nakii.mmorpg.quest.quest.event;

import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.online.OnlineEvent;
import org.bukkit.entity.Player;

/**
 * Adapt an event to be run as Op.
 * <p>
 * Gives the player op, executes the nested event and then reverts the operation if necessary.
 */
public class OpPlayerEventAdapter implements OnlineEvent {

    /**
     * The event to execute as Op.
     */
    private final OnlineEvent event;

    /**
     * Creates a new OpPlayerEventAdapter.
     *
     * @param event the event to execute as op.
     */
    public OpPlayerEventAdapter(final OnlineEvent event) {
        this.event = event;
    }

    @Override
    public void execute(final OnlineProfile profile) throws QuestException {
        final Player player = profile.getPlayer();
        final boolean previousOp = player.isOp();
        try {
            player.setOp(true);
            event.execute(profile);
        } finally {
            player.setOp(previousOp);
        }
    }
}

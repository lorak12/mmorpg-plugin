package org.nakii.mmorpg.quest.quest.event;

import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerlessEvent;
import org.bukkit.entity.Player;

import java.util.function.Supplier;

/**
 * Adapt a player event as a playerless event by applying it to a group of online {@link Player}s. The group supplying
 * function will be called every time the event is executed.
 */
public class OnlineProfileGroupPlayerlessEventAdapter implements PlayerlessEvent {
    /**
     * The supplier for generating the group of online players to use.
     */
    private final Supplier<? extends Iterable<? extends OnlineProfile>> profileCollectionSupplier;

    /**
     * The event to execute for every player of the group.
     */
    private final PlayerEvent playerEvent;

    /**
     * Create a playerless event that will execute a normal event for every player provided by the supplying function.
     *
     * @param profileSupplier supplier for the player group
     * @param playerEvent     event to execute
     */
    public OnlineProfileGroupPlayerlessEventAdapter(final Supplier<? extends Iterable<? extends OnlineProfile>> profileSupplier, final PlayerEvent playerEvent) {
        profileCollectionSupplier = profileSupplier;
        this.playerEvent = playerEvent;
    }

    @Override
    public void execute() throws QuestException {
        for (final OnlineProfile onlineProfile : profileCollectionSupplier.get()) {
            playerEvent.execute(onlineProfile);
        }
    }
}

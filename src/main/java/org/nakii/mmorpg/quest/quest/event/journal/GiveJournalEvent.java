package org.nakii.mmorpg.quest.quest.event.journal;

import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.event.online.OnlineEvent;
import org.nakii.mmorpg.quest.database.PlayerData;

import java.util.function.Function;

/**
 * Gives journal to the player.
 */
public class GiveJournalEvent implements OnlineEvent {
    /**
     * Function to get the player data for a given online profile.
     */
    private final Function<OnlineProfile, PlayerData> playerDataSource;

    /**
     * Creates a new GiveJournalEvent.
     *
     * @param playerDataSource source for the player data
     */
    public GiveJournalEvent(final Function<OnlineProfile, PlayerData> playerDataSource) {
        this.playerDataSource = playerDataSource;
    }

    @Override
    public void execute(final OnlineProfile profile) {
        playerDataSource.apply(profile).getJournal().addToInv();
    }
}

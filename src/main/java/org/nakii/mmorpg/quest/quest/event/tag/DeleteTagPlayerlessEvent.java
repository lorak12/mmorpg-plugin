package org.nakii.mmorpg.quest.quest.event.tag;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.profile.ProfileProvider;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerlessEvent;
import org.nakii.mmorpg.quest.data.PlayerDataStorage;
import org.nakii.mmorpg.quest.database.Saver;
import org.nakii.mmorpg.quest.database.UpdateType;

import java.util.List;

/**
 * Event to delete tags from all players.
 */
public class DeleteTagPlayerlessEvent implements PlayerlessEvent {
    /**
     * Storage for player data.
     */
    private final PlayerDataStorage dataStorage;

    /**
     * The saver to inject into database-using events.
     */
    private final Saver saver;

    /**
     * The profile provider instance.
     */
    private final ProfileProvider profileProvider;

    /**
     * The list of tags to delete.
     */
    private final Variable<List<String>> tags;

    /**
     * Create a new delete tag playerless event.
     *
     * @param dataStorage     the storage providing player data
     * @param saver           database saver to use
     * @param profileProvider the profile provider instance
     * @param tags            the list of tags to delete
     */
    public DeleteTagPlayerlessEvent(final PlayerDataStorage dataStorage, final Saver saver, final ProfileProvider profileProvider, final Variable<List<String>> tags) {
        this.dataStorage = dataStorage;
        this.saver = saver;
        this.profileProvider = profileProvider;
        this.tags = tags;
    }

    @Override
    public void execute() throws QuestException {
        for (final String tag : tags.getValue(null)) {
            for (final OnlineProfile onlineProfile : profileProvider.getOnlineProfiles()) {
                dataStorage.get(onlineProfile).removeTag(tag);
            }
            saver.add(new Saver.Record(UpdateType.REMOVE_ALL_TAGS, tag));
        }
    }
}

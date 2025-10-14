package org.nakii.mmorpg.quest.quest.condition.journal;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.online.OnlineCondition;
import org.nakii.mmorpg.quest.data.PlayerDataStorage;
import org.nakii.mmorpg.quest.feature.journal.Pointer;
import org.nakii.mmorpg.quest.id.JournalEntryID;

/**
 * A condition to check if a player has a specified pointer in his journal.
 */
public class JournalCondition implements OnlineCondition {

    /**
     * Storage for player data.
     */
    private final PlayerDataStorage dataStorage;

    /**
     * The target pointer to the journal to check for.
     */
    private final Variable<JournalEntryID> targetPointer;

    /**
     * Create a new journal condition.
     *
     * @param dataStorage   the storage providing player data
     * @param targetPointer the target pointer to the journal to check for
     */
    public JournalCondition(final PlayerDataStorage dataStorage, final Variable<JournalEntryID> targetPointer) {
        this.dataStorage = dataStorage;
        this.targetPointer = targetPointer;
    }

    @Override
    public boolean check(final OnlineProfile profile) throws QuestException {
        final JournalEntryID resolved = targetPointer.getValue(profile);
        for (final Pointer pointer : dataStorage.get(profile).getEntries()) {
            if (pointer.pointer().equals(resolved)) {
                return true;
            }
        }
        return false;
    }
}

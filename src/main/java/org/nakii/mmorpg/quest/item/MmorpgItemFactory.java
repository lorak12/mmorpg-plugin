package org.nakii.mmorpg.quest.item;

import org.jetbrains.annotations.Nullable;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.kernel.TypeFactory;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;

public class MmorpgItemFactory implements TypeFactory<QuestItemWrapper> {

    @Override
    public QuestItemWrapper parseInstruction(Instruction instruction) throws QuestException {
        if (!instruction.hasNext()) {
            throw new QuestException("The 'mmorpg' item type requires an item ID as an argument.");
        }

        final String itemId = instruction.next();
        return new ShallowWrapper(new MmorpgQuestItem(itemId));
    }

    private record ShallowWrapper(QuestItem questItem) implements QuestItemWrapper {
        @Override
        public QuestItem getItem(@Nullable final Profile profile) {
            return questItem;
        }
    }
}
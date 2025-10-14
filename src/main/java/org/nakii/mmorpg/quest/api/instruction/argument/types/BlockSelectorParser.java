package org.nakii.mmorpg.quest.api.instruction.argument.types;

import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.util.BlockSelector;

/**
 * Parses a string to a block selector.
 */
public class BlockSelectorParser implements Argument<BlockSelector> {
    /**
     * Creates a new parser for block selectors.
     */
    public BlockSelectorParser() {
    }

    @Override
    public BlockSelector apply(final String string) throws QuestException {
        return new BlockSelector(string);
    }
}

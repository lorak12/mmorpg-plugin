package org.nakii.mmorpg.quest.quest.condition.chest;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.Item;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.argument.IdentifierArgument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.PrimaryServerThreadData;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerCondition;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerConditionFactory;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerlessCondition;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerlessConditionFactory;
import org.nakii.mmorpg.quest.api.quest.condition.nullable.NullableConditionAdapter;
import org.nakii.mmorpg.quest.api.quest.condition.thread.PrimaryServerThreadPlayerCondition;
import org.nakii.mmorpg.quest.api.quest.condition.thread.PrimaryServerThreadPlayerlessCondition;
import org.bukkit.Location;

import java.util.List;

/**
 * Factory to create chest item conditions from {@link Instruction}s.
 */
public class ChestItemConditionFactory implements PlayerConditionFactory, PlayerlessConditionFactory {

    /**
     * Data used for condition check on the primary server thread.
     */
    private final PrimaryServerThreadData data;

    /**
     * Create the chest item condition factory.
     *
     * @param data the data used for checking the condition on the main thread
     */
    public ChestItemConditionFactory(final PrimaryServerThreadData data) {
        this.data = data;
    }

    @Override
    public PlayerCondition parsePlayer(final Instruction instruction) throws QuestException {
        return new PrimaryServerThreadPlayerCondition(new NullableConditionAdapter(parse(instruction)), data);
    }

    @Override
    public PlayerlessCondition parsePlayerless(final Instruction instruction) throws QuestException {
        return new PrimaryServerThreadPlayerlessCondition(new NullableConditionAdapter(parse(instruction)), data);
    }

    private ChestItemCondition parse(final Instruction instruction) throws QuestException {
        final Variable<Location> loc = instruction.get(Argument.LOCATION);
        final Variable<List<Item>> items = instruction.getList(IdentifierArgument.ITEM);
        return new ChestItemCondition(loc, items);
    }
}

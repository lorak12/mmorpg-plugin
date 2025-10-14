package org.nakii.mmorpg.quest.quest.condition.random;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.nullable.NullableCondition;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * The condition that is met randomly.
 */
public class RandomCondition implements NullableCondition {

    /**
     * The random object to generate random numbers.
     */
    private final Random random;

    /**
     * The range of the random number to be true.
     */
    private final Variable<Number> rangeOfRandom;

    /**
     * The maximum value that the random number can be.
     */
    private final Variable<Number> valueMax;

    /**
     * The constructor of the random condition.
     *
     * @param random        The random object to generate random numbers.
     * @param valueMax      The maximum value that the random number can be.
     * @param rangeOfRandom The range of the random number to be true.
     */
    public RandomCondition(final Random random, final Variable<Number> valueMax, final Variable<Number> rangeOfRandom) {
        this.random = random;
        this.valueMax = valueMax;
        this.rangeOfRandom = rangeOfRandom;
    }

    @Override
    public boolean check(@Nullable final Profile profile) throws QuestException {
        final int randomNumber = random.nextInt(rangeOfRandom.getValue(profile).intValue()) + 1;
        return randomNumber <= valueMax.getValue(profile).intValue();
    }
}

package org.nakii.mmorpg.quest.quest.variable.math;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerVariable;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerVariableFactory;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerlessVariable;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerlessVariableFactory;
import org.nakii.mmorpg.quest.api.quest.variable.nullable.NullableVariableAdapter;
import org.nakii.mmorpg.quest.kernel.processor.quest.VariableProcessor;
import org.nakii.mmorpg.quest.util.math.Tokenizer;
import org.nakii.mmorpg.quest.util.math.tokens.Token;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Factory to create math variables from {@link Instruction}s.
 */
public class MathVariableFactory implements PlayerVariableFactory, PlayerlessVariableFactory {

    /**
     * Regular expression that matches calculation expressions.
     * The regex has a named group 'expression' that contains only the math part without the identifier.
     */
    public static final Pattern CALC_REGEX = Pattern.compile("calc:(?<expression>.+)");

    /**
     * The variable processor to use.
     */
    private final VariableProcessor variableProcessor;

    /**
     * Create a new factory to create Math Variables.
     *
     * @param variableProcessor the variable processor to use
     */
    public MathVariableFactory(final VariableProcessor variableProcessor) {
        this.variableProcessor = variableProcessor;
    }

    @Override
    public PlayerVariable parsePlayer(final Instruction instruction) throws QuestException {
        return parseInstruction(instruction);
    }

    @Override
    public PlayerlessVariable parsePlayerless(final Instruction instruction) throws QuestException {
        return parseInstruction(instruction);
    }

    @SuppressWarnings("deprecation")
    private NullableVariableAdapter parseInstruction(final Instruction instruction) throws QuestException {
        final Matcher expressionMatcher = CALC_REGEX.matcher(String.join(".", instruction.getValueParts()));
        if (!expressionMatcher.matches()) {
            throw new QuestException("invalid format");
        }
        final String expression = expressionMatcher.group("expression");
        final Token token = new Tokenizer(variableProcessor, instruction.getPackage()).tokenize(expression);
        return new NullableVariableAdapter(new MathVariable(token));
    }
}

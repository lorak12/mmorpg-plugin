package org.nakii.mmorpg.quest.quest.condition.eval;

import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackageManager;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.kernel.TypeFactory;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerCondition;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerlessCondition;
import org.nakii.mmorpg.quest.api.quest.condition.nullable.NullableCondition;
import org.nakii.mmorpg.quest.kernel.processor.adapter.ConditionAdapter;
import org.nakii.mmorpg.quest.kernel.registry.QuestTypeRegistry;
import org.nakii.mmorpg.quest.kernel.registry.quest.ConditionTypeRegistry;
import org.jetbrains.annotations.Nullable;

/**
 * A condition which evaluates to another condition.
 */
public class EvalCondition implements NullableCondition {
    /**
     * The quest package manager to get quest packages from.
     */
    private final QuestPackageManager packManager;

    /**
     * The condition type registry providing factories to parse the evaluated instruction.
     */
    private final ConditionTypeRegistry conditionTypeRegistry;

    /**
     * The quest package to relate the condition to.
     */
    private final QuestPackage pack;

    /**
     * The evaluation input.
     */
    private final Variable<String> evaluation;

    /**
     * Creates a new Eval condition.
     *
     * @param packManager           the quest package manager to get quest packages from
     * @param conditionTypeRegistry the condition type registry providing factories to parse the evaluated instruction
     * @param pack                  the quest package to relate the condition to
     * @param evaluation            the evaluation input
     */
    public EvalCondition(final QuestPackageManager packManager, final ConditionTypeRegistry conditionTypeRegistry,
                         final QuestPackage pack, final Variable<String> evaluation) {
        this.packManager = packManager;
        this.conditionTypeRegistry = conditionTypeRegistry;
        this.pack = pack;
        this.evaluation = evaluation;
    }

    /**
     * Constructs a condition with a given instruction and returns it.
     *
     * @param packManager           the quest package manager to get quest packages from
     * @param instruction           the instruction string to parse
     * @param conditionTypeRegistry the condition type registry providing factories to parse the evaluated instruction
     * @param pack                  the quest package to relate the condition to
     * @return the condition
     * @throws QuestException if the condition could not be created
     */
    public static ConditionAdapter createCondition(final QuestPackageManager packManager, final QuestTypeRegistry<PlayerCondition, PlayerlessCondition, ConditionAdapter> conditionTypeRegistry, final QuestPackage pack, final String instruction) throws QuestException {
        final Instruction conditionInstruction = new Instruction(packManager, pack, null, instruction);
        final TypeFactory<ConditionAdapter> conditionFactory = conditionTypeRegistry.getFactory(conditionInstruction.getPart(0));
        return conditionFactory.parseInstruction(conditionInstruction);
    }

    @Override
    public boolean check(@Nullable final Profile profile) throws QuestException {
        return createCondition(packManager, conditionTypeRegistry, pack, evaluation.getValue(profile)).check(profile);
    }
}

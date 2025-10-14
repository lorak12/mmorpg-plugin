package org.nakii.mmorpg.quest.quest.objective.tame;

import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.argument.types.EnumParser;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.objective.ObjectiveFactory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Tameable;

/**
 * Factory for creating {@link TameObjective} instances from {@link Instruction}s.
 */
public class TameObjectiveFactory implements ObjectiveFactory {
    /**
     * Creates a new TameObjectiveFactory instance.
     */
    public TameObjectiveFactory() {
    }

    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        final Variable<EntityType> type = instruction.get(new EntityTypeParser());
        final Variable<Number> targetAmount = instruction.get(Argument.NUMBER_NOT_LESS_THAN_ONE);
        return new TameObjective(instruction, targetAmount, type);
    }

    /**
     * Parser for {@link EntityType} enums.
     */
    private static class EntityTypeParser extends EnumParser<EntityType> {
        /**
         * Creates a new parser for enums.
         */
        public EntityTypeParser() {
            super(EntityType.class);
        }

        @Override
        public EntityType apply(final String string) throws QuestException {
            final EntityType type = super.apply(string);
            if (type.getEntityClass() == null || !Tameable.class.isAssignableFrom(type.getEntityClass())) {
                throw new QuestException("Entity cannot be tamed: " + type);
            }
            return type;
        }
    }
}

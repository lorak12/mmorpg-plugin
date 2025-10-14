package org.nakii.mmorpg.quest.quest.objective.equip;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.Item;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.argument.IdentifierArgument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.objective.ObjectiveFactory;

/**
 * Factory for creating {@link EquipItemObjective} instances from {@link Instruction}s.
 */
public class EquipItemObjectiveFactory implements ObjectiveFactory {

    /**
     * Creates a new instance of the EquipItemObjectiveFactory.
     */
    public EquipItemObjectiveFactory() {
    }

    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        final Variable<PlayerArmorChangeEvent.SlotType> slotType = instruction.get(Argument.ENUM(PlayerArmorChangeEvent.SlotType.class));
        final Variable<Item> item = instruction.get(IdentifierArgument.ITEM);
        return new EquipItemObjective(instruction, item, slotType);
    }
}

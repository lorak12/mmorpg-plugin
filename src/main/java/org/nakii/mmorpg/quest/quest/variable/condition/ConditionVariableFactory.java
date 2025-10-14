package org.nakii.mmorpg.quest.quest.variable.condition;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.QuestTypeApi;
import org.nakii.mmorpg.quest.api.quest.condition.ConditionID;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerVariable;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerVariableFactory;
import org.nakii.mmorpg.quest.config.PluginMessage;

/**
 * Factory to create {@link ConditionVariable}s from {@link Instruction}s.
 */
public class ConditionVariableFactory implements PlayerVariableFactory {

    /**
     * Quest Type API.
     */
    private final QuestTypeApi questTypeApi;

    /**
     * The {@link PluginMessage} instance.
     */
    private final PluginMessage pluginMessage;

    /**
     * Create the Condition Variable Factory.
     *
     * @param questTypeApi  the Quest Type API
     * @param pluginMessage the {@link PluginMessage} instance
     */
    public ConditionVariableFactory(final QuestTypeApi questTypeApi, final PluginMessage pluginMessage) {
        this.questTypeApi = questTypeApi;
        this.pluginMessage = pluginMessage;
    }

    @Override
    public PlayerVariable parsePlayer(final Instruction instruction) throws QuestException {
        final Variable<ConditionID> conditionId = instruction.get(ConditionID::new);
        final boolean papiMode = instruction.hasArgument("papiMode");
        return new ConditionVariable(pluginMessage, conditionId, papiMode, questTypeApi);
    }
}

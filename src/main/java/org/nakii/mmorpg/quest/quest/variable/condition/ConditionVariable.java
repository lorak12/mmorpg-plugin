package org.nakii.mmorpg.quest.quest.variable.condition;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.QuestTypeApi;
import org.nakii.mmorpg.quest.api.quest.condition.ConditionID;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerVariable;
import org.nakii.mmorpg.quest.config.PluginMessage;

/**
 * Get the "fulfillment" status of a quest condition.
 */
public class ConditionVariable implements PlayerVariable {
    /**
     * The {@link PluginMessage} instance.
     */
    private final PluginMessage pluginMessage;

    /**
     * Condition to check.
     */
    private final Variable<ConditionID> conditionId;

    /**
     * If variable should be in PAPI style.
     */
    private final boolean papiMode;

    /**
     * Quest Type API.
     */
    private final QuestTypeApi questTypeApi;

    /**
     * Create a new Condition variable.
     *
     * @param pluginMessage the {@link PluginMessage} instance
     * @param conditionId   the condition to get the "fulfillment" status
     * @param papiMode      if the return value should be in PAPI mode as defined in the documentation
     * @param questTypeApi  the Quest Type API
     */
    public ConditionVariable(final PluginMessage pluginMessage, final Variable<ConditionID> conditionId, final boolean papiMode, final QuestTypeApi questTypeApi) {
        this.pluginMessage = pluginMessage;
        this.conditionId = conditionId;
        this.papiMode = papiMode;
        this.questTypeApi = questTypeApi;
    }

    @Override
    public String getValue(final Profile profile) throws QuestException {
        if (questTypeApi.condition(profile, conditionId.getValue(profile))) {
            return papiMode ? LegacyComponentSerializer.legacySection().serialize(pluginMessage.getMessage(profile, "condition_variable_met")) : "true";
        }
        return papiMode ? LegacyComponentSerializer.legacySection().serialize(pluginMessage.getMessage(profile, "condition_variable_not_met")) : "false";
    }
}

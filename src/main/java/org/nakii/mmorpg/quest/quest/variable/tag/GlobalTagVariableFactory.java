package org.nakii.mmorpg.quest.quest.variable.tag;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerlessVariable;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerlessVariableFactory;
import org.nakii.mmorpg.quest.config.PluginMessage;
import org.nakii.mmorpg.quest.database.GlobalData;

/**
 * A factory for creating GlobalTag variables.
 */
public class GlobalTagVariableFactory extends AbstractTagVariableFactory<GlobalData> implements PlayerlessVariableFactory {

    /**
     * The {@link PluginMessage} instance.
     */
    private final PluginMessage pluginMessage;

    /**
     * Create a new GlobalTagVariableFactory.
     *
     * @param dataHolder    the data holder
     * @param pluginMessage the {@link PluginMessage} instance
     */
    public GlobalTagVariableFactory(final GlobalData dataHolder, final PluginMessage pluginMessage) {
        super(dataHolder);
        this.pluginMessage = pluginMessage;
    }

    @Override
    public PlayerlessVariable parsePlayerless(final Instruction instruction) throws QuestException {
        return new GlobalTagVariable(pluginMessage, dataHolder, instruction.next(), instruction.getPackage(), instruction.hasArgument("papiMode"));
    }
}

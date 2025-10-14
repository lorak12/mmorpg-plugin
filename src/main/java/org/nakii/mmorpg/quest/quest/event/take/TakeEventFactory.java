package org.nakii.mmorpg.quest.quest.event.take;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.Item;
import org.nakii.mmorpg.quest.api.instruction.argument.IdentifierArgument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.nakii.mmorpg.quest.api.quest.event.online.OnlineEventAdapter;
import org.nakii.mmorpg.quest.config.PluginMessage;
import org.nakii.mmorpg.quest.quest.event.NotificationSender;

import java.util.List;

/**
 * Factory for {@link TakeEvent}.
 */
public class TakeEventFactory extends AbstractTakeEventFactory {

    /**
     * Create the take event factory.
     *
     * @param loggerFactory the logger factory to create a logger for the events
     * @param pluginMessage the {@link PluginMessage} instance
     */
    public TakeEventFactory(final BetonQuestLoggerFactory loggerFactory, final PluginMessage pluginMessage) {
        super(loggerFactory, pluginMessage);
    }

    @Override
    public PlayerEvent parsePlayer(final Instruction instruction) throws QuestException {
        final BetonQuestLogger log = loggerFactory.create(TakeEvent.class);
        final List<CheckType> checkOrder = getCheckOrder(instruction);
        final Variable<List<Item>> questItems = instruction.getList(IdentifierArgument.ITEM);
        final NotificationSender notificationSender = getNotificationSender(instruction, log);
        return new OnlineEventAdapter(new TakeEvent(questItems, checkOrder, notificationSender), log, instruction.getPackage());
    }
}

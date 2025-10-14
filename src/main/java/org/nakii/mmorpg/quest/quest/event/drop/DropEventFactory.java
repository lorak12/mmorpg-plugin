package org.nakii.mmorpg.quest.quest.event.drop;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.Item;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.argument.IdentifierArgument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.instruction.variable.VariableList;
import org.nakii.mmorpg.quest.api.profile.ProfileProvider;
import org.nakii.mmorpg.quest.api.quest.PrimaryServerThreadData;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEventFactory;
import org.nakii.mmorpg.quest.api.quest.event.PlayerlessEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerlessEventFactory;
import org.nakii.mmorpg.quest.api.quest.event.nullable.NullableEventAdapter;
import org.nakii.mmorpg.quest.api.quest.event.thread.PrimaryServerThreadEvent;
import org.nakii.mmorpg.quest.api.quest.event.thread.PrimaryServerThreadPlayerlessEvent;
import org.nakii.mmorpg.quest.quest.event.OnlineProfileGroupPlayerlessEventAdapter;
import org.bukkit.Location;

import java.util.List;

/**
 * Factory to create {@link DropEvent}s for items from {@link Instruction}s.
 */
public class DropEventFactory implements PlayerEventFactory, PlayerlessEventFactory {
    /**
     * The profile provider instance.
     */
    private final ProfileProvider profileProvider;

    /**
     * Data for primary server thread access.
     */
    private final PrimaryServerThreadData data;

    /**
     * Creates the drop event factory.
     *
     * @param profileProvider the profile provider instance
     * @param data            the data for primary server thread access
     */
    public DropEventFactory(final ProfileProvider profileProvider, final PrimaryServerThreadData data) {
        this.profileProvider = profileProvider;
        this.data = data;
    }

    @Override
    public PlayerEvent parsePlayer(final Instruction instruction) throws QuestException {
        return new PrimaryServerThreadEvent(createDropEvent(instruction), data);
    }

    @Override
    public PlayerlessEvent parsePlayerless(final Instruction instruction) throws QuestException {
        return new PrimaryServerThreadPlayerlessEvent(createStaticDropEvent(instruction), data);
    }

    private PlayerlessEvent createStaticDropEvent(final Instruction instruction) throws QuestException {
        final NullableEventAdapter dropEvent = createDropEvent(instruction);
        if (!instruction.hasArgument("location")) {
            return new OnlineProfileGroupPlayerlessEventAdapter(profileProvider::getOnlineProfiles, dropEvent);
        }
        return dropEvent;
    }

    private NullableEventAdapter createDropEvent(final Instruction instruction) throws QuestException {
        final Variable<List<Item>> items = instruction.getValueList("items", IdentifierArgument.ITEM, VariableList.notEmptyChecker());
        final Variable<Location> location = instruction.get(instruction.getValue("location", "%location%"), Argument.LOCATION);
        return new NullableEventAdapter(new DropEvent(items, location));
    }
}

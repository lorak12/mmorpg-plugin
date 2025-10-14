package org.nakii.mmorpg.quest.quest.event.tag;

import org.nakii.mmorpg.quest.QuestModule;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.PackageArgument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEventFactory;
import org.nakii.mmorpg.quest.api.quest.event.PlayerlessEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerlessEventFactory;

import java.util.List;
import java.util.Locale;

/**
 * Factory to create global tag events from {@link Instruction}s.
 */
public class TagGlobalEventFactory implements PlayerEventFactory, PlayerlessEventFactory {
    /**
     * BetonQuest instance to provide to events.
     */
    private final QuestModule questModule;

    /**
     * Create the global tag event factory.
     *
     * @param questModule BetonQuest instance to pass on
     */
    public TagGlobalEventFactory(final QuestModule questModule) {
        this.questModule = questModule;
    }

    @Override
    public PlayerEvent parsePlayer(final Instruction instruction) throws QuestException {
        final String action = instruction.next();
        final Variable<List<String>> tags = instruction.getList(PackageArgument.IDENTIFIER);
        return switch (action.toLowerCase(Locale.ROOT)) {
            case "add" -> createAddTagEvent(tags);
            case "delete", "del" -> createDeleteTagEvent(tags);
            default -> throw new QuestException("Unknown tag action: " + action);
        };
    }

    @Override
    public PlayerlessEvent parsePlayerless(final Instruction instruction) throws QuestException {
        final String action = instruction.next();
        final Variable<List<String>> tags = instruction.getList(PackageArgument.IDENTIFIER);
        return switch (action.toLowerCase(Locale.ROOT)) {
            case "add" -> createStaticAddTagEvent(tags);
            case "delete", "del" -> createStaticDeleteTagEvent(tags);
            default -> throw new QuestException("Unknown tag action: " + action);
        };
    }

    private PlayerlessEvent createStaticAddTagEvent(final Variable<List<String>> tags) {
        final TagChanger tagChanger = new AddTagChanger(tags);
        return new PlayerlessTagEvent(questModule.getGlobalData(), tagChanger);
    }

    private PlayerlessEvent createStaticDeleteTagEvent(final Variable<List<String>> tags) {
        final TagChanger tagChanger = new DeleteTagChanger(tags);
        return new PlayerlessTagEvent(questModule.getGlobalData(), tagChanger);
    }

    private PlayerEvent createAddTagEvent(final Variable<List<String>> tags) {
        final TagChanger tagChanger = new AddTagChanger(tags);
        return new TagEvent(profile -> questModule.getGlobalData(), tagChanger);
    }

    private PlayerEvent createDeleteTagEvent(final Variable<List<String>> tags) {
        final TagChanger tagChanger = new DeleteTagChanger(tags);
        return new TagEvent(profile -> questModule.getGlobalData(), tagChanger);
    }
}

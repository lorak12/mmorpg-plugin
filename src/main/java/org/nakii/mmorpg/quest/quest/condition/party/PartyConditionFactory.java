package org.nakii.mmorpg.quest.quest.condition.party;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.ProfileProvider;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.QuestTypeApi;
import org.nakii.mmorpg.quest.api.quest.condition.*;
import org.nakii.mmorpg.quest.api.quest.condition.nullable.NullableConditionAdapter;
import org.nakii.mmorpg.quest.quest.condition.ThrowExceptionPlayerlessCondition;
import org.bukkit.Location;

import java.util.List;

/**
 * Factory to create party conditions from {@link Instruction}s.
 */
public class PartyConditionFactory implements PlayerConditionFactory, PlayerlessConditionFactory {

    /**
     * Quest Type API.
     */
    private final QuestTypeApi questTypeApi;

    /**
     * The profile provider instance.
     */
    private final ProfileProvider profileProvider;

    /**
     * Create the party condition factory.
     *
     * @param questTypeApi    the Quest Type API
     * @param profileProvider the profile provider instance
     */
    public PartyConditionFactory(final QuestTypeApi questTypeApi, final ProfileProvider profileProvider) {
        this.questTypeApi = questTypeApi;
        this.profileProvider = profileProvider;
    }

    @Override
    public PlayerCondition parsePlayer(final Instruction instruction) throws QuestException {
        final Variable<Location> location = instruction.get(instruction.getValue("location", "%location%"),
                Argument.LOCATION);
        return new NullableConditionAdapter(parse(instruction, location));
    }

    @Override
    public PlayerlessCondition parsePlayerless(final Instruction instruction) throws QuestException {
        final Variable<Location> location = instruction.getValue("location", Argument.LOCATION);
        if (location == null) {
            return new ThrowExceptionPlayerlessCondition();
        }
        return new NullableConditionAdapter(parse(instruction, location));
    }

    private PartyCondition parse(final Instruction instruction, final Variable<Location> location) throws QuestException {
        final Variable<Number> range = instruction.get(Argument.NUMBER);
        final Variable<List<ConditionID>> conditions = instruction.getList(ConditionID::new);
        final Variable<List<ConditionID>> everyone = instruction.getValueList("every", ConditionID::new);
        final Variable<List<ConditionID>> anyone = instruction.getValueList("any", ConditionID::new);
        final Variable<Number> count = instruction.getValue("count", Argument.NUMBER);

        return new PartyCondition(location, range, conditions, everyone, anyone, count, questTypeApi, profileProvider);
    }
}

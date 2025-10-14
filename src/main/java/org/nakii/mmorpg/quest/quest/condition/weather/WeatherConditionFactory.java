package org.nakii.mmorpg.quest.quest.condition.weather;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.PrimaryServerThreadData;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerCondition;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerConditionFactory;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerlessCondition;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerlessConditionFactory;
import org.nakii.mmorpg.quest.api.quest.condition.nullable.NullableConditionAdapter;
import org.nakii.mmorpg.quest.api.quest.condition.thread.PrimaryServerThreadPlayerCondition;
import org.nakii.mmorpg.quest.api.quest.condition.thread.PrimaryServerThreadPlayerlessCondition;
import org.nakii.mmorpg.quest.kernel.processor.quest.VariableProcessor;
import org.nakii.mmorpg.quest.quest.condition.ThrowExceptionPlayerlessCondition;
import org.nakii.mmorpg.quest.quest.event.weather.Weather;
import org.bukkit.World;

/**
 * Factory to create weather conditions from {@link Instruction}s.
 */
public class WeatherConditionFactory implements PlayerConditionFactory, PlayerlessConditionFactory {

    /**
     * Data used for condition check on the primary server thread.
     */
    private final PrimaryServerThreadData data;

    /**
     * The variable processor used to process variables.
     */
    private final VariableProcessor variableProcessor;

    /**
     * Create the weather condition factory.
     *
     * @param data              the data used for checking the condition on the main thread
     * @param variableProcessor the variable processor used to process variables
     */
    public WeatherConditionFactory(final PrimaryServerThreadData data, final VariableProcessor variableProcessor) {
        this.data = data;
        this.variableProcessor = variableProcessor;
    }

    @Override
    public PlayerCondition parsePlayer(final Instruction instruction) throws QuestException {
        final Weather weather = Weather.parseWeather(instruction.next());
        final Variable<World> world = instruction.get(instruction.getValue("world", "%location.world%"), Argument.WORLD);
        return new PrimaryServerThreadPlayerCondition(
                new NullableConditionAdapter(new WeatherCondition(weather, world)), data);
    }

    @Override
    public PlayerlessCondition parsePlayerless(final Instruction instruction) throws QuestException {
        final String worldString = instruction.getValue("world");
        if (worldString == null) {
            return new ThrowExceptionPlayerlessCondition();
        }
        final Weather weather = Weather.parseWeather(instruction.next());
        final Variable<World> world = new Variable<>(variableProcessor, instruction.getPackage(), worldString, Argument.WORLD);
        return new PrimaryServerThreadPlayerlessCondition(
                new NullableConditionAdapter(new WeatherCondition(weather, world)), data);
    }
}

package org.nakii.mmorpg.quest.quest.condition.advancement;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.quest.PrimaryServerThreadData;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerCondition;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerConditionFactory;
import org.nakii.mmorpg.quest.api.quest.condition.online.OnlineConditionAdapter;
import org.nakii.mmorpg.quest.api.quest.condition.thread.PrimaryServerThreadPlayerCondition;
import org.nakii.mmorpg.quest.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;

/**
 * Factory to create advancement conditions from {@link Instruction}s.
 */
public class AdvancementConditionFactory implements PlayerConditionFactory {
    /**
     * Amount of parts the advancement string is expected to have.
     */
    private static final int ADVANCEMENT_LENGTH = 2;

    /**
     * Data used for condition check on the primary server thread.
     */
    private final PrimaryServerThreadData data;

    /**
     * Logger factory to create a logger for the conditions.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * Create the Advancement Condition Factory.
     *
     * @param data          the data used for checking the condition on the main thread
     * @param loggerFactory the logger factory to create a logger for the conditions
     */
    public AdvancementConditionFactory(final PrimaryServerThreadData data, final BetonQuestLoggerFactory loggerFactory) {
        this.data = data;
        this.loggerFactory = loggerFactory;
    }

    @Override
    public PlayerCondition parsePlayer(final Instruction instruction) throws QuestException {
        final String advancementString = instruction.next();
        final String[] split = advancementString.split(":");
        if (split.length != ADVANCEMENT_LENGTH) {
            throw new QuestException("The advancement '" + advancementString + "' is missing a namespace!");
        }
        final Advancement advancement = Utils.getNN(Bukkit.getServer().getAdvancement(new NamespacedKey(split[0], split[1])),
                "No such advancement: " + advancementString);
        return new PrimaryServerThreadPlayerCondition(new OnlineConditionAdapter(
                new AdvancementCondition(advancement),
                loggerFactory.create(AdvancementCondition.class),
                instruction.getPackage()
        ), data);
    }
}

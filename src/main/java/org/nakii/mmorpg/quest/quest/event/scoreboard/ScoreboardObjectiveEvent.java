package org.nakii.mmorpg.quest.quest.event.scoreboard;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

/**
 * Adds/removes/multiplies/divides scores on scoreboards.
 */
public class ScoreboardObjectiveEvent implements PlayerEvent {
    /**
     * The name of the objective.
     */
    private final String objective;

    /**
     * The number to modify the score by.
     */
    private final Variable<Number> count;

    /**
     * The modification to apply to the score.
     */
    private final ScoreModification scoreModification;

    /**
     * Creates a new ScoreboardEvent.
     *
     * @param objective         the name of the objective
     * @param count             the number to modify the score by
     * @param scoreModification the modification to apply to the score
     */
    public ScoreboardObjectiveEvent(final String objective, final Variable<Number> count, final ScoreModification scoreModification) {
        this.objective = objective;
        this.count = count;
        this.scoreModification = scoreModification;
    }

    @Override
    public void execute(final Profile profile) throws QuestException {
        final Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        final Objective obj = board.getObjective(objective);
        if (obj == null) {
            throw new QuestException("Scoreboard objective " + objective + " does not exist!");
        }
        final Score score = obj.getScore(profile.getPlayer());
        score.setScore(scoreModification.modify(score.getScore(), count.getValue(profile).doubleValue()));
    }
}

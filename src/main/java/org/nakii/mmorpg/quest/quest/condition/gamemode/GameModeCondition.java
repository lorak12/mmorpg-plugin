package org.nakii.mmorpg.quest.quest.condition.gamemode;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.online.OnlineCondition;
import org.bukkit.GameMode;

/**
 * A condition that checks if the player is in a specific game mode.
 */
public class GameModeCondition implements OnlineCondition {

    /**
     * The game mode to check for.
     */
    private final Variable<GameMode> gameMode;

    /**
     * Creates a new game mode condition.
     *
     * @param gameMode The game mode to check for.
     */
    public GameModeCondition(final Variable<GameMode> gameMode) {
        this.gameMode = gameMode;
    }

    @Override
    public boolean check(final OnlineProfile profile) throws QuestException {
        return profile.getPlayer().getGameMode() == gameMode.getValue(profile);
    }
}

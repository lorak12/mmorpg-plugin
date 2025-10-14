package org.nakii.mmorpg.quest.quest.condition.permission;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.online.OnlineCondition;

/**
 * A condition that checks if a player has a permission.
 */
public class PermissionCondition implements OnlineCondition {

    /**
     * The permission to check for.
     */
    private final Variable<String> permission;

    /**
     * Creates a new permission condition.
     *
     * @param permission The permission to check for.
     */
    public PermissionCondition(final Variable<String> permission) {
        this.permission = permission;
    }

    @Override
    public boolean check(final OnlineProfile profile) throws QuestException {
        return profile.getPlayer().hasPermission(permission.getValue(profile));
    }
}

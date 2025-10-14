package org.nakii.mmorpg.quest.quest.condition.armor;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.online.OnlineCondition;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

/**
 * Requires the player to have specific armor rating.
 */
public class ArmorRatingCondition implements OnlineCondition {

    /**
     * The required armor rating.
     */
    private final Variable<Number> requiredRating;

    /**
     * Creates a new ArmorRatingCondition.
     *
     * @param requiredRating the required armor rating
     */
    public ArmorRatingCondition(final Variable<Number> requiredRating) {
        this.requiredRating = requiredRating;
    }

    @Override
    public boolean check(final OnlineProfile profile) throws QuestException {
        final AttributeInstance genericArmor = profile.getPlayer().getAttribute(Attribute.ARMOR);
        if (genericArmor == null) {
            throw new QuestException("Could not get the generic armor attribute of the player.");
        }
        final int defensePoints = (int) genericArmor.getValue();
        final int rating = requiredRating.getValue(profile).intValue();
        return defensePoints >= rating;
    }
}

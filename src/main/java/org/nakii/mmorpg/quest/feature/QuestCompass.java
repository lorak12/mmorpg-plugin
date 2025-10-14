package org.nakii.mmorpg.quest.feature;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.text.Text;
import org.nakii.mmorpg.quest.id.ItemID;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

/**
 * A Quest Compass targeting a location.
 *
 * @param names    the display names by their language
 * @param location the compass location
 * @param itemID   possible item id, when it should be displayed in the backpack
 */
public record QuestCompass(Text names, Variable<Location> location, @Nullable ItemID itemID) {
}

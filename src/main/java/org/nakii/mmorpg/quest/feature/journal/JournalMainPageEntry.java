package org.nakii.mmorpg.quest.feature.journal;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.condition.ConditionID;
import org.nakii.mmorpg.quest.api.text.Text;

import java.util.List;

/**
 * A journal main page entry.
 *
 * @param priority   the order priority
 * @param conditions the conditions to display the entry
 * @param entry      the text content
 */
public record JournalMainPageEntry(int priority, Variable<List<ConditionID>> conditions, Text entry) {
}

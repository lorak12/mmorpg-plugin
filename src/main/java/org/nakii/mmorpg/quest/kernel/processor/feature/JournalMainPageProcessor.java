package org.nakii.mmorpg.quest.kernel.processor.feature;

import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackageManager;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.instruction.variable.VariableList;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.ConditionID;
import org.nakii.mmorpg.quest.api.text.Text;
import org.nakii.mmorpg.quest.feature.journal.JournalMainPageEntry;
import org.nakii.mmorpg.quest.id.JournalMainPageID;
import org.nakii.mmorpg.quest.kernel.processor.SectionProcessor;
import org.nakii.mmorpg.quest.kernel.processor.quest.VariableProcessor;
import org.nakii.mmorpg.quest.text.ParsedSectionTextCreator;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

/**
 * Loads and stores Journal Main Pages.
 */
public class JournalMainPageProcessor extends SectionProcessor<JournalMainPageID, JournalMainPageEntry> {
    /**
     * Variable to resolve conditions.
     */
    private final VariableProcessor variableProcessor;

    /**
     * Text creator to parse text.
     */
    private final ParsedSectionTextCreator textCreator;

    /**
     * Create a new QuestProcessor to store and execute type logic.
     *
     * @param log               the custom logger for this class
     * @param packManager       the quest package manager to get quest packages from
     * @param variableProcessor the variable resolver to resolve conditions
     * @param textCreator       the text creator to parse text
     */
    public JournalMainPageProcessor(final BetonQuestLogger log, final QuestPackageManager packManager,
                                    final VariableProcessor variableProcessor, final ParsedSectionTextCreator textCreator) {
        super(log, packManager, "Journal Main Page", "journal_main_page");
        this.variableProcessor = variableProcessor;
        this.textCreator = textCreator;
    }

    @Override
    protected JournalMainPageEntry loadSection(final QuestPackage pack, final ConfigurationSection section) throws QuestException {
        final int priority = section.getInt("priority", -1);
        if (priority < 0) {
            throw new QuestException("Priority of journal main page needs to be at least 0!");
        }
        final Variable<List<ConditionID>> conditions = new VariableList<>(variableProcessor, pack,
                section.getString("conditions", ""),
                value -> new ConditionID(packManager, pack, value));
        final Text text = textCreator.parseFromSection(pack, section, "text");
        return new JournalMainPageEntry(priority, conditions, text);
    }

    @Override
    protected JournalMainPageID getIdentifier(final QuestPackage pack, final String identifier) throws QuestException {
        return new JournalMainPageID(packManager, pack, identifier);
    }
}

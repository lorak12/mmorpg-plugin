package org.nakii.mmorpg.quest.kernel.processor.feature;

import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackageManager;
import org.nakii.mmorpg.quest.api.instruction.argument.types.location.LocationParser;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.text.Text;
import org.nakii.mmorpg.quest.feature.QuestCompass;
import org.nakii.mmorpg.quest.id.CompassID;
import org.nakii.mmorpg.quest.id.ItemID;
import org.nakii.mmorpg.quest.kernel.processor.SectionProcessor;
import org.nakii.mmorpg.quest.kernel.processor.quest.VariableProcessor;
import org.nakii.mmorpg.quest.text.ParsedSectionTextCreator;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Loads and stores {@link QuestCompass}es.
 */
public class CompassProcessor extends SectionProcessor<CompassID, QuestCompass> {
    /**
     * Variable processor to create new variables.
     */
    private final VariableProcessor variableProcessor;

    /**
     * Text creator to parse text.
     */
    private final ParsedSectionTextCreator textCreator;

    /**
     * Create a new QuestProcessor to store {@link QuestCompass}es.
     *
     * @param log               the custom logger for this class
     * @param packManager       the quest package manager to get quest packages from
     * @param variableProcessor the variable processor to create new variables
     * @param textCreator       the text creator to parse text
     */
    public CompassProcessor(final BetonQuestLogger log, final QuestPackageManager packManager,
                            final VariableProcessor variableProcessor, final ParsedSectionTextCreator textCreator) {
        super(log, packManager, "Compass", "compass");
        this.variableProcessor = variableProcessor;
        this.textCreator = textCreator;
    }

    @Override
    protected QuestCompass loadSection(final QuestPackage pack, final ConfigurationSection section) throws QuestException {
        final Text names = textCreator.parseFromSection(pack, section, "name");
        final String location = section.getString("location");
        if (location == null) {
            throw new QuestException("Location not defined");
        }
        final Variable<Location> loc = new Variable<>(variableProcessor, pack, location, LocationParser.LOCATION);
        final String itemName = section.getString("item");
        final ItemID itemID = itemName == null ? null : new ItemID(packManager, pack, itemName);
        return new QuestCompass(names, loc, itemID);
    }

    @Override
    protected CompassID getIdentifier(final QuestPackage pack, final String identifier) throws QuestException {
        return new CompassID(packManager, pack, identifier);
    }
}

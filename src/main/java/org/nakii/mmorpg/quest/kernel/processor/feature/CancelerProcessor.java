package org.nakii.mmorpg.quest.kernel.processor.feature;

import org.nakii.mmorpg.quest.api.BetonQuestApi;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.instruction.variable.VariableList;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.QuestTypeApi;
import org.nakii.mmorpg.quest.api.quest.condition.ConditionID;
import org.nakii.mmorpg.quest.api.quest.event.EventID;
import org.nakii.mmorpg.quest.api.quest.objective.ObjectiveID;
import org.nakii.mmorpg.quest.api.text.Text;
import org.nakii.mmorpg.quest.config.PluginMessage;
import org.nakii.mmorpg.quest.data.PlayerDataStorage;
import org.nakii.mmorpg.quest.feature.QuestCanceler;
import org.nakii.mmorpg.quest.id.ItemID;
import org.nakii.mmorpg.quest.id.JournalEntryID;
import org.nakii.mmorpg.quest.id.QuestCancelerID;
import org.nakii.mmorpg.quest.kernel.processor.SectionProcessor;
import org.nakii.mmorpg.quest.kernel.processor.quest.VariableProcessor;
import org.nakii.mmorpg.quest.text.ParsedSectionTextCreator;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Stores Quest Canceler.
 */
public class CancelerProcessor extends SectionProcessor<QuestCancelerID, QuestCanceler> {

    /**
     * Logger factory to create new class specific logger.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * The BetonQuest API instance.
     */
    private final BetonQuestApi api;

    /**
     * The {@link PluginMessage} instance.
     */
    private final PluginMessage pluginMessage;

    /**
     * Variable processor to create new variables.
     */
    private final VariableProcessor variableProcessor;

    /**
     * Text creator to parse text.
     */
    private final ParsedSectionTextCreator textCreator;

    /**
     * Quest Type API.
     */
    private final QuestTypeApi questTypeApi;

    /**
     * Player Data Storage.
     */
    private final PlayerDataStorage playerDataStorage;

    /**
     * Create a new Quest Canceler Processor to store them.
     *
     * @param log               the custom logger for this class
     * @param loggerFactory     the logger factory to create new class specific logger
     * @param api               the BetonQuest API instance
     * @param pluginMessage     the {@link PluginMessage} instance
     * @param variableProcessor the variable processor to create new variables
     * @param textCreator       the text creator to parse text
     * @param questTypeApi      the Quest Type API
     * @param playerDataStorage the storage for player data
     */
    public CancelerProcessor(final BetonQuestLogger log, final BetonQuestLoggerFactory loggerFactory,
                             final BetonQuestApi api, final PluginMessage pluginMessage,
                             final VariableProcessor variableProcessor, final ParsedSectionTextCreator textCreator,
                             final QuestTypeApi questTypeApi, final PlayerDataStorage playerDataStorage) {
        super(log, api.getQuestPackageManager(), "Quest Canceler", "cancel");
        this.loggerFactory = loggerFactory;
        this.api = api;
        this.pluginMessage = pluginMessage;
        this.variableProcessor = variableProcessor;
        this.textCreator = textCreator;
        this.questTypeApi = questTypeApi;
        this.playerDataStorage = playerDataStorage;
    }

    @Override
    protected QuestCanceler loadSection(final QuestPackage pack, final ConfigurationSection section) throws QuestException {
        final Text names = textCreator.parseFromSection(pack, section, "name");
        final String itemString = section.getString("item");
        final String rawItem = itemString == null ? pack.getConfig().getString("item.cancel_button") : itemString;
        final ItemID item = rawItem == null ? null : new ItemID(packManager, pack, rawItem);
        final String rawLoc = section.getString("location");
        final Variable<Location> location = rawLoc == null ? null : new Variable<>(variableProcessor, pack, rawLoc, Argument.LOCATION);
        final QuestCanceler.CancelData cancelData = new QuestCanceler.CancelData(
                new VariableList<>(variableProcessor, pack, section.getString("conditions", ""), value -> new ConditionID(packManager, pack, value)),
                new VariableList<>(variableProcessor, pack, section.getString("events", ""), value -> new EventID(packManager, pack, value)),
                new VariableList<>(variableProcessor, pack, section.getString("objectives", ""), value -> new ObjectiveID(packManager, pack, value)),
                new VariableList<>(variableProcessor, pack, section.getString("tags", ""), Argument.STRING),
                new VariableList<>(variableProcessor, pack, section.getString("points", ""), Argument.STRING),
                new VariableList<>(variableProcessor, pack, section.getString("journal", ""), value -> new JournalEntryID(packManager, pack, value)),
                location);
        final BetonQuestLogger logger = loggerFactory.create(QuestCanceler.class);
        return new QuestCanceler(logger, questTypeApi, playerDataStorage, getIdentifier(pack, section.getName()),
                api.getFeatureApi(), pluginMessage, names, item, pack, cancelData);
    }

    @Override
    protected QuestCancelerID getIdentifier(final QuestPackage pack, final String identifier) throws QuestException {
        return new QuestCancelerID(packManager, pack, identifier);
    }
}

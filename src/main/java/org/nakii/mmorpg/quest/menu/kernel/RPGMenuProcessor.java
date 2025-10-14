package org.nakii.mmorpg.quest.menu.kernel;

import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackageManager;
import org.nakii.mmorpg.quest.api.feature.FeatureApi;
import org.nakii.mmorpg.quest.api.identifier.Identifier;
import org.nakii.mmorpg.quest.api.instruction.argument.IdentifierArgument;
import org.nakii.mmorpg.quest.api.instruction.argument.types.ItemParser;
import org.nakii.mmorpg.quest.api.instruction.variable.VariableList;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.QuestTypeApi;
import org.nakii.mmorpg.quest.kernel.processor.SectionProcessor;
import org.nakii.mmorpg.quest.kernel.processor.quest.VariableProcessor;
import org.nakii.mmorpg.quest.text.ParsedSectionTextCreator;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Does the load logic around {@link T} from a configuration section.
 *
 * @param <I> the {@link Identifier} identifying the type
 * @param <T> the type
 */
public abstract class RPGMenuProcessor<I extends Identifier, T> extends SectionProcessor<I, T> {
    /**
     * Logger Factory to create Menu Item Logger.
     */
    protected final BetonQuestLoggerFactory loggerFactory;

    /**
     * Text creator to parse text.
     */
    protected final ParsedSectionTextCreator textCreator;

    /**
     * The QuestTypeApi.
     */
    protected final QuestTypeApi questTypeApi;

    /**
     * The Variable Processor.
     */
    protected final VariableProcessor variableProcessor;

    /**
     * The Item Parser instance.
     */
    protected final ItemParser itemParser;

    /**
     * Create a new Processor to create and store Menu Items.
     *
     * @param log               the custom logger for this class
     * @param packManager       the quest package manager to get quest packages from
     * @param readable          the type name used for logging, with the first letter in upper case
     * @param internal          the section name and/or bstats topic identifier
     * @param loggerFactory     the logger factory to class specific loggers with
     * @param textCreator       the text creator to parse text
     * @param variableProcessor the variable resolver
     * @param questTypeApi      the QuestTypeApi
     * @param featureApi        the Feature API
     */
    public RPGMenuProcessor(final BetonQuestLogger log, final QuestPackageManager packManager, final String readable,
                            final String internal, final BetonQuestLoggerFactory loggerFactory,
                            final ParsedSectionTextCreator textCreator, final VariableProcessor variableProcessor,
                            final QuestTypeApi questTypeApi, final FeatureApi featureApi) {
        super(log, packManager, readable, internal);
        this.loggerFactory = loggerFactory;
        this.textCreator = textCreator;
        this.questTypeApi = questTypeApi;
        this.variableProcessor = variableProcessor;
        this.itemParser = new ItemParser(featureApi);
    }

    /**
     * Class to bundle objects required for creation.
     */
    protected class CreationHelper {
        /**
         * Source Pack.
         */
        protected final QuestPackage pack;

        /**
         * Source Section.
         */
        protected final ConfigurationSection section;

        /**
         * Creates a new Creation Helper.
         *
         * @param pack    the pack to create from
         * @param section the section to create from
         */
        protected CreationHelper(final QuestPackage pack, final ConfigurationSection section) {
            this.pack = pack;
            this.section = section;
        }

        /**
         * Unresolved string from config file.
         *
         * @param path where to search
         * @return requested String
         * @throws QuestException if string is not given
         */
        protected String getRequired(final String path) throws QuestException {
            final String string = section.getString(path);
            if (string == null) {
                throw new QuestException(path + " is missing!");
            }
            return string;
        }

        /**
         * Parse a list of ids from config file.
         *
         * @param <U>      the id type
         * @param path     where to search
         * @param argument the argument converter
         * @return requested ids or empty list when not present
         * @throws QuestException if one of the ids can't be found
         */
        protected <U extends Identifier> VariableList<U> getID(final String path, final IdentifierArgument<U> argument)
                throws QuestException {
            return new VariableList<>(variableProcessor, pack, section.getString(path, ""), value -> argument.apply(packManager, pack, value));
        }
    }
}

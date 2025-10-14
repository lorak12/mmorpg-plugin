package org.nakii.mmorpg.quest.kernel.processor;

import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackageManager;
import org.nakii.mmorpg.quest.api.feature.FeatureApi;
import org.nakii.mmorpg.quest.api.identifier.InstructionIdentifier;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.profile.ProfileProvider;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.npc.Npc;
import org.nakii.mmorpg.quest.api.quest.npc.NpcID;
import org.nakii.mmorpg.quest.api.quest.npc.feature.NpcHider;
import org.nakii.mmorpg.quest.api.text.Text;
import org.nakii.mmorpg.quest.bstats.InstructionMetricsSupplier;
import org.nakii.mmorpg.quest.config.PluginMessage;
import org.nakii.mmorpg.quest.conversation.ConversationData;
import org.nakii.mmorpg.quest.conversation.ConversationID;
import org.nakii.mmorpg.quest.data.PlayerDataStorage;
import org.nakii.mmorpg.quest.feature.QuestCanceler;
import org.nakii.mmorpg.quest.feature.QuestCompass;
import org.nakii.mmorpg.quest.feature.journal.JournalMainPageEntry;
import org.nakii.mmorpg.quest.id.*;
import org.nakii.mmorpg.quest.item.QuestItem;
import org.nakii.mmorpg.quest.kernel.processor.feature.*;
import org.nakii.mmorpg.quest.kernel.processor.quest.NpcProcessor;
import org.nakii.mmorpg.quest.kernel.processor.quest.VariableProcessor;
import org.nakii.mmorpg.quest.kernel.registry.feature.BaseFeatureRegistries;
import org.nakii.mmorpg.quest.schedule.EventScheduling;
import org.nakii.mmorpg.quest.text.ParsedSectionTextCreator;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Stores the active Processors to store and execute type logic.
 *
 * @param log              The custom {@link BetonQuestLogger} instance for this class.
 * @param core             The core quest type processors.
 * @param eventScheduling  Event scheduling module.
 * @param cancelers        Quest Canceler logic.
 * @param compasses        Compasses.
 * @param conversations    Conversation Data logic.
 * @param items            Quest Item logic.
 * @param journalEntries   Journal Entries.
 * @param journalMainPages Journal Main Pages.
 * @param npcs             Npc getting.
 * @param additional       Additional quest processors.
 */
@SuppressWarnings("PMD.CouplingBetweenObjects")
public record QuestRegistry(
        BetonQuestLogger log,
        CoreQuestRegistry core,
        EventScheduling eventScheduling,
        CancelerProcessor cancelers,
        CompassProcessor compasses,
        ConversationProcessor conversations,
        ItemProcessor items,
        JournalEntryProcessor journalEntries,
        JournalMainPageProcessor journalMainPages,
        NpcProcessor npcs,
        List<QuestProcessor<?, ?>> additional
) implements FeatureApi {

    /**
     * Create a new Registry for storing and using Conditions, Events, Objectives, Variables,
     * Conversations and Quest canceler.
     *
     * @param log               the custom logger for this registry
     * @param loggerFactory     the logger factory used for new custom logger instances
     * @param plugin            the plugin used to create new conversation data
     * @param coreQuestRegistry the core quest type processors
     * @param otherRegistries   the available other types
     * @param pluginMessage     the {@link PluginMessage} instance
     * @param textCreator       the text creator to parse text
     * @param profileProvider   the profile provider instance
     * @param playerDataStorage the storage to get player data
     * @return the newly created QuestRegistry
     */
    public static QuestRegistry create(final BetonQuestLogger log, final BetonQuestLoggerFactory loggerFactory,
                                       final MMORPGCore plugin, final CoreQuestRegistry coreQuestRegistry,
                                       final BaseFeatureRegistries otherRegistries, final PluginMessage pluginMessage,
                                       final ParsedSectionTextCreator textCreator, final ProfileProvider profileProvider,
                                       final PlayerDataStorage playerDataStorage) {
        final VariableProcessor variables = coreQuestRegistry.variables();
        final QuestPackageManager packManager = plugin.getQuestModule().getQuestPackageManager();
        final EventScheduling eventScheduling = new EventScheduling(loggerFactory.create(EventScheduling.class, "Schedules"), packManager, otherRegistries.eventScheduling());
        final CancelerProcessor cancelers = new CancelerProcessor(loggerFactory.create(CancelerProcessor.class), loggerFactory, plugin.getQuestModule(), pluginMessage, variables, textCreator, coreQuestRegistry, playerDataStorage);
        final CompassProcessor compasses = new CompassProcessor(loggerFactory.create(CompassProcessor.class), packManager, variables, textCreator);
        final ConversationProcessor conversations = new ConversationProcessor(loggerFactory.create(ConversationProcessor.class), loggerFactory, plugin.getQuestModule(),
                textCreator, otherRegistries.conversationIO(), otherRegistries.interceptor(), variables);
        final ItemProcessor items = new ItemProcessor(loggerFactory.create(ItemProcessor.class), packManager, otherRegistries.item());
        final JournalEntryProcessor journalEntries = new JournalEntryProcessor(loggerFactory.create(JournalEntryProcessor.class), packManager, textCreator);
        final JournalMainPageProcessor journalMainPages = new JournalMainPageProcessor(loggerFactory.create(JournalMainPageProcessor.class), packManager, variables, textCreator);
        final NpcProcessor npcs = new NpcProcessor(loggerFactory.create(NpcProcessor.class), loggerFactory, packManager, variables, otherRegistries.npc(), pluginMessage, plugin.getQuestModule(), profileProvider, coreQuestRegistry);
        return new QuestRegistry(log, coreQuestRegistry, eventScheduling, cancelers, compasses, conversations, items, journalEntries, journalMainPages, npcs, new ArrayList<>());
    }

    /**
     * Loads the Processors with the QuestPackages.
     * <p>
     * Removes previous data and loads the given QuestPackages.
     *
     * @param packages the quest packages to load
     */
    public void loadData(final Collection<QuestPackage> packages) {
        eventScheduling.clear();
        core.clear();
        cancelers.clear();
        conversations.clear();
        compasses.clear();
        items.clear();
        journalEntries.clear();
        journalMainPages.clear();
        npcs.clear();
        additional.forEach(QuestProcessor::clear);

        for (final QuestPackage pack : packages) {
            final String packName = pack.getQuestPath();
            log.debug(pack, "Loading stuff in package " + packName);
            cancelers.load(pack);
            core.load(pack);
            compasses.load(pack);
            conversations.load(pack);
            items.load(pack);
            journalEntries.load(pack);
            journalMainPages.load(pack);
            npcs.load(pack);
            eventScheduling.load(pack);
            additional.forEach(questProcessor -> questProcessor.load(pack));

            log.debug(pack, "Everything in package " + packName + " loaded");
        }

        conversations.checkExternalPointers();

        log.info("There are " + String.join(", ", core.readableSize(),
                cancelers.readableSize(), compasses.readableSize(), conversations.readableSize(), items.readableSize(),
                journalEntries.readableSize(), journalMainPages.readableSize(), npcs.readableSize())
                + " (Additional: " + additional.stream().map(QuestProcessor::readableSize).collect(Collectors.joining(", ")) + ")"
                + " loaded from " + packages.size() + " packages.");

        eventScheduling.startAll();
        additional.forEach(questProcessor -> {
            if (questProcessor instanceof final StartTask startTask) {
                startTask.startAll();
            }
        });
    }

    /**
     * Gets the bstats metric supplier for registered and active quest types.
     *
     * @return available instruction metrics
     */
    public Map<String, InstructionMetricsSupplier<? extends InstructionIdentifier>> metricsSupplier() {
        final Map<String, InstructionMetricsSupplier<? extends InstructionIdentifier>> map = new HashMap<>(core.metricsSupplier());
        map.putAll(Map.ofEntries(
                items.metricsSupplier(),
                npcs.metricsSupplier())
        );
        return map;
    }

    @Override
    public ConversationData getConversation(final ConversationID conversationID) throws QuestException {
        return conversations().get(conversationID);
    }

    @Override
    public Map<QuestCancelerID, QuestCanceler> getCancelers() {
        return new HashMap<>(cancelers().getValues());
    }

    @Override
    public QuestCanceler getCanceler(final QuestCancelerID cancelerID) throws QuestException {
        return cancelers().get(cancelerID);
    }

    @Override
    public Map<CompassID, QuestCompass> getCompasses() {
        return new HashMap<>(compasses().getValues());
    }

    @Override
    public Text getJournalEntry(final JournalEntryID journalEntryID) throws QuestException {
        return journalEntries().get(journalEntryID);
    }

    @Override
    public void renameJournalEntry(final JournalEntryID name, final JournalEntryID rename) {
        journalEntries().renameJournalEntry(name, rename);
    }

    @Override
    public Map<JournalMainPageID, JournalMainPageEntry> getJournalMainPages() {
        return new HashMap<>(journalMainPages().getValues());
    }

    @Override
    public Npc<?> getNpc(final NpcID npcID, @Nullable final Profile profile) throws QuestException {
        return npcs().get(npcID).getNpc(profile);
    }

    @Override
    public NpcHider getNpcHider() {
        return npcs().getNpcHider();
    }

    @Override
    public QuestItem getItem(final ItemID itemID, @Nullable final Profile profile) throws QuestException {
        return items().get(itemID).getItem(profile);
    }
}

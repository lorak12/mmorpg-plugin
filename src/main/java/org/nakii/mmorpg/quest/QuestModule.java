package org.nakii.mmorpg.quest;

import com.google.common.base.Suppliers;
import io.papermc.lib.PaperLib;
import net.kyori.adventure.key.Key;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.plugin.Plugin;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.api.BetonQuestApi;
import org.nakii.mmorpg.quest.api.LanguageProvider;
import org.nakii.mmorpg.quest.api.bukkit.event.LoadDataEvent;
import org.nakii.mmorpg.quest.api.common.component.font.DefaultFont;
import org.nakii.mmorpg.quest.api.common.component.font.FontRegistry;
import org.nakii.mmorpg.quest.api.config.ConfigAccessor;
import org.nakii.mmorpg.quest.api.config.ConfigAccessorFactory;
import org.nakii.mmorpg.quest.api.config.FileConfigAccessor;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackageManager;
import org.nakii.mmorpg.quest.api.feature.FeatureApi;
import org.nakii.mmorpg.quest.api.feature.FeatureRegistries;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.logger.CachingBetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.profile.ProfileProvider;
import org.nakii.mmorpg.quest.api.quest.PrimaryServerThreadData;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.QuestTypeApi;
import org.nakii.mmorpg.quest.api.quest.QuestTypeRegistries;
import org.nakii.mmorpg.quest.api.text.TextParser;
import org.nakii.mmorpg.quest.bstats.BStatsMetrics;
import org.nakii.mmorpg.quest.command.*;
import org.nakii.mmorpg.quest.compatibility.Compatibility;
import org.nakii.mmorpg.quest.config.DefaultConfigAccessorFactory;
import org.nakii.mmorpg.quest.config.PluginMessage;
import org.nakii.mmorpg.quest.config.QuestManager;
import org.nakii.mmorpg.quest.config.patcher.migration.Migrator;
import org.nakii.mmorpg.quest.config.patcher.migration.QuestMigrator;
import org.nakii.mmorpg.quest.conversation.AnswerFilter;
import org.nakii.mmorpg.quest.conversation.CombatTagger;
import org.nakii.mmorpg.quest.conversation.Conversation;
import org.nakii.mmorpg.quest.conversation.ConversationColors;
import org.nakii.mmorpg.quest.data.PlayerDataStorage;
import org.nakii.mmorpg.quest.database.*;
import org.nakii.mmorpg.quest.feature.CoreFeatureFactories;
import org.nakii.mmorpg.quest.feature.journal.JournalFactory;
import org.nakii.mmorpg.quest.item.QuestItemHandler;
import org.nakii.mmorpg.quest.kernel.processor.CoreQuestRegistry;
import org.nakii.mmorpg.quest.kernel.processor.QuestProcessor;
import org.nakii.mmorpg.quest.kernel.processor.QuestRegistry;
import org.nakii.mmorpg.quest.kernel.processor.quest.VariableProcessor;
import org.nakii.mmorpg.quest.kernel.registry.feature.BaseFeatureRegistries;
import org.nakii.mmorpg.quest.kernel.registry.quest.BaseQuestTypeRegistries;
import org.nakii.mmorpg.quest.listener.CustomDropListener;
import org.nakii.mmorpg.quest.listener.JoinQuitListener;
import org.nakii.mmorpg.quest.listener.MobKillListener;
import org.nakii.mmorpg.quest.logger.DefaultBetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.logger.HandlerFactory;
import org.nakii.mmorpg.quest.logger.PlayerLogWatcher;
import org.nakii.mmorpg.quest.logger.handler.chat.AccumulatingReceiverSelector;
import org.nakii.mmorpg.quest.logger.handler.chat.ChatHandler;
import org.nakii.mmorpg.quest.logger.handler.history.HistoryHandler;
import org.nakii.mmorpg.quest.menu.RPGMenu;
import org.nakii.mmorpg.quest.notify.Notify;
import org.nakii.mmorpg.quest.playerhider.PlayerHider;
import org.nakii.mmorpg.quest.profile.UUIDProfileProvider;
import org.nakii.mmorpg.quest.quest.CoreQuestTypes;
import org.nakii.mmorpg.quest.schedule.LastExecutionCache;
import org.nakii.mmorpg.quest.text.DecidingTextParser;
import org.nakii.mmorpg.quest.text.ParsedSectionTextCreator;
import org.nakii.mmorpg.quest.text.TagTextParserDecider;
import org.nakii.mmorpg.quest.versioning.Version;
import org.nakii.mmorpg.quest.versioning.java.JREVersionPrinter;
import org.nakii.mmorpg.quest.web.DownloadSource;
import org.nakii.mmorpg.quest.web.TempFileDownloadSource;
import org.nakii.mmorpg.quest.web.WebContentSource;
import org.nakii.mmorpg.quest.web.WebDownloadSource;
import org.nakii.mmorpg.quest.web.updater.UpdateDownloader;
import org.nakii.mmorpg.quest.web.updater.UpdateSourceHandler;
import org.nakii.mmorpg.quest.web.updater.Updater;
import org.nakii.mmorpg.quest.web.updater.UpdaterConfig;
import org.nakii.mmorpg.quest.web.updater.source.DevelopmentUpdateSource;
import org.nakii.mmorpg.quest.web.updater.source.ReleaseUpdateSource;
import org.nakii.mmorpg.quest.web.updater.source.implementations.GitHubReleaseSource;
import org.nakii.mmorpg.quest.web.updater.source.implementations.NexusReleaseAndDevelopmentSource;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.InstantSource;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;

@SuppressWarnings({"PMD.CouplingBetweenObjects", "PMD.GodClass", "PMD.TooManyMethods", "PMD.TooManyFields", "NullAway.Init"})
public class QuestModule implements BetonQuestApi, LanguageProvider {

    private final MMORPGCore plugin;

    private static final int BSTATS_METRICS_ID = 551;
    private static final String DEV_INDICATOR = "DEV";
    private static final String CACHE_FILE = ".cache/schedules.yml";

    private PlayerDataStorage playerDataStorage;
    private QuestRegistry questRegistry;
    private BaseQuestTypeRegistries questTypeRegistries;
    private BaseFeatureRegistries featureRegistries;
    private BetonQuestLoggerFactory loggerFactory;
    private ConfigAccessorFactory configAccessorFactory;
    private BetonQuestLogger log;
    private FileConfigAccessor config;
    private String defaultLanguage;
    private TextParser textParser;
    private PluginMessage pluginMessage;
    private Database database;
    private boolean usesMySQL;
    private AsyncSaver saver;
    private Updater updater;
    private GlobalData globalData;
    private PlayerHider playerHider;
    private RPGMenu rpgMenu;
    private LastExecutionCache lastExecutionCache;
    private ProfileProvider profileProvider;
    private QuestManager questManager;
    private FontRegistry fontRegistry;
    private ConversationColors conversationColors;
    private Compatibility compatibility;

    public QuestModule(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    private <T> T registerAndGetService(final Class<T> clazz, final T service) {
        final ServicesManager servicesManager = plugin.getServer().getServicesManager();
        servicesManager.register(clazz, service, plugin, ServicePriority.Lowest);
        return servicesManager.load(clazz);
    }

    @SuppressWarnings({"PMD.NcssCount", "PMD.DoNotUseThreads"})
    public void enable() {
        this.loggerFactory = registerAndGetService(BetonQuestLoggerFactory.class, new CachingBetonQuestLoggerFactory(new DefaultBetonQuestLoggerFactory()));
        this.log = loggerFactory.create(MMORPGCore.getInstance());
        if (!PaperLib.isPaper()) {
            PaperLib.suggestPaper(plugin, Level.WARNING);
            log.warn("Only Paper is supported! Disabling BetonQuest module...");
            return;
        }

        this.configAccessorFactory = registerAndGetService(ConfigAccessorFactory.class, new DefaultConfigAccessorFactory(loggerFactory, loggerFactory.create(ConfigAccessorFactory.class)));
        this.profileProvider = registerAndGetService(ProfileProvider.class, new UUIDProfileProvider(plugin.getServer()));

        final JREVersionPrinter jreVersionPrinter = new JREVersionPrinter();
        final String jreInfo = jreVersionPrinter.getMessage();
        log.info(jreInfo);

        migrate();

        try {
            config = configAccessorFactory.createPatching(new File(plugin.getDataFolder(), "config.yml"), plugin, "config.yml");
        } catch (final InvalidConfigurationException | FileNotFoundException e) {
            log.error("Could not load the config.yml file!", e);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
        defaultLanguage = config.getString("language", "en-US");

        final HistoryHandler debugHistoryHandler = HandlerFactory.createHistoryHandler(loggerFactory, plugin,
                plugin.getServer().getScheduler(), config, new File(plugin.getDataFolder(), "/logs"), InstantSource.system());
        registerLogHandler(plugin.getServer(), debugHistoryHandler);
        final AccumulatingReceiverSelector receiverSelector = new AccumulatingReceiverSelector();
        final ChatHandler chatHandler = HandlerFactory.createChatHandler(plugin, plugin.getServer(), receiverSelector);
        registerLogHandler(plugin.getServer(), chatHandler);

        final String version = plugin.getDescription().getVersion();
        log.debug("BetonQuest " + version + " is starting...");
        log.debug(jreInfo);

        questManager = new QuestManager(loggerFactory, loggerFactory.create(QuestManager.class), configAccessorFactory,
                plugin.getDataFolder(), new QuestMigrator(loggerFactory.create(QuestMigrator.class), plugin.getDescription()));
        Notify.load(config, questManager.getPackages().values());

        setupDatabase();

        saver = new AsyncSaver(loggerFactory.create(AsyncSaver.class, "Database"), config);
        saver.start();
        Backup.loadDatabaseFromBackup(configAccessorFactory);

        globalData = new GlobalData(loggerFactory.create(GlobalData.class), saver);

        final FileConfigAccessor cache;
        try {
            final Path cacheFile = new File(plugin.getDataFolder(), CACHE_FILE).toPath();
            if (!Files.exists(cacheFile)) {
                Files.createDirectories(Optional.ofNullable(cacheFile.getParent()).orElseThrow());
                Files.createFile(cacheFile);
            }
            cache = configAccessorFactory.create(cacheFile.toFile());
        } catch (final IOException | InvalidConfigurationException e) {
            log.error("Error while loading schedule cache: " + e.getMessage(), e);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
        lastExecutionCache = new LastExecutionCache(loggerFactory.create(LastExecutionCache.class, "Cache"), cache);

        questTypeRegistries = BaseQuestTypeRegistries.create(loggerFactory, plugin.getQuestModule());
        final CoreQuestRegistry coreQuestRegistry = new CoreQuestRegistry(loggerFactory, questManager, questTypeRegistries,
                plugin.getServer().getPluginManager(), plugin);

        final PlayerDataFactory playerDataFactory = new PlayerDataFactory(loggerFactory, questManager, saver, plugin.getServer(),
                coreQuestRegistry, Suppliers.memoize(() -> new JournalFactory(pluginMessage, coreQuestRegistry, questRegistry,
                config, textParser, fontRegistry)));
        playerDataStorage = new PlayerDataStorage(loggerFactory, loggerFactory.create(PlayerDataStorage.class), config,
                playerDataFactory, coreQuestRegistry.objectives(), profileProvider);

        featureRegistries = BaseFeatureRegistries.create(loggerFactory);

        final String defaultParser = config.getString("text_parser", "legacyminimessage");
        textParser = new DecidingTextParser(featureRegistries.textParser(), new TagTextParserDecider(defaultParser));
        try {
            pluginMessage = new PluginMessage(plugin, coreQuestRegistry.variables(), playerDataStorage,
                    textParser, configAccessorFactory, this);
            for (final String language : pluginMessage.getLanguages()) {
                log.debug("Loaded " + language + " language");
            }
        } catch (final QuestException e) {
            log.error("Could not load the plugin messages!", e);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        final ParsedSectionTextCreator textCreator = new ParsedSectionTextCreator(textParser, playerDataStorage,
                this, coreQuestRegistry.variables());
        questRegistry = QuestRegistry.create(loggerFactory.create(QuestRegistry.class), loggerFactory, plugin,
                coreQuestRegistry, featureRegistries, pluginMessage, textCreator, profileProvider, playerDataStorage);

        setupUpdater();
        registerListener(coreQuestRegistry);

        new CoreQuestTypes(loggerFactory, plugin.getServer(), plugin.getServer().getScheduler(), plugin,
                coreQuestRegistry, pluginMessage, coreQuestRegistry.variables(), globalData, playerDataStorage,
                profileProvider, this, playerDataFactory)
                .register(questTypeRegistries);

        conversationColors = new ConversationColors(textParser, config);

        final Key defaultkey = Key.key("default");
        fontRegistry = new FontRegistry(defaultkey);
        fontRegistry.registerFont(defaultkey, new DefaultFont());

        new CoreFeatureFactories(loggerFactory, questManager, lastExecutionCache, coreQuestRegistry, config, conversationColors,
                textParser, fontRegistry)
                .register(featureRegistries);

        try {
            conversationColors.load();
        } catch (final QuestException e) {
            log.warn("Could not load conversation colors! " + e.getMessage(), e);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        compatibility = new Compatibility(loggerFactory.create(Compatibility.class), plugin.getQuestModule(), config, version);
        Bukkit.getPluginManager().registerEvents(compatibility, plugin);

        registerCommands(receiverSelector, debugHistoryHandler, playerDataFactory);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            compatibility.postHook();
            loadData();
            playerDataStorage.initProfiles(profileProvider.getOnlineProfiles(), pluginMessage);

            try {
                playerHider = new PlayerHider(plugin, this, getVariableProcessor(), profileProvider);
            } catch (final QuestException e) {
                log.error("Could not start PlayerHider! " + e.getMessage(), e);
            }
        });

        try {
            Class.forName("org.apache.logging.log4j.core.Filter");
            final Logger coreLogger = (Logger) LogManager.getRootLogger();
            coreLogger.addFilter(new AnswerFilter());
        } catch (final ClassNotFoundException | NoClassDefFoundError e) {
            log.warn("Could not disable /betonquestanswer logging", e);
        }

        new BStatsMetrics(plugin, new Metrics(plugin, BSTATS_METRICS_ID), questRegistry.metricsSupplier(), compatibility);

        rpgMenu = new RPGMenu(loggerFactory.create(RPGMenu.class), loggerFactory, questManager, config,
                coreQuestRegistry.variables(), pluginMessage, textCreator, coreQuestRegistry, questRegistry,
                profileProvider);

        log.info("BetonQuest Module successfully enabled!");
    }

    public void disable() {
        if (questRegistry != null) {
            questRegistry.eventScheduling().clear();
        }
        if (profileProvider != null) {
            for (final OnlineProfile onlineProfile : profileProvider.getOnlineProfiles()) {
                final Conversation conv = Conversation.getConversation(onlineProfile);
                if (conv != null) {
                    conv.suspend();
                }
                onlineProfile.getPlayer().closeInventory();
            }
        }
        if (saver != null) {
            saver.end();
        }
        if (compatibility != null) {
            compatibility.disable();
        }
        if (database != null) {
            database.closeConnection();
        }
        if (playerHider != null) {
            playerHider.stop();
        }
        if (rpgMenu != null) {
            rpgMenu.onDisable();
        }
        log.info("BetonQuest Module successfully disabled!");
    }

    private void setupDatabase() {
        final boolean mySQLEnabled = config.getBoolean("mysql.enabled", true);
        if (mySQLEnabled) {
            log.debug("Connecting to MySQL database");
            this.database = new MySQL(loggerFactory.create(MySQL.class, "Database"), plugin,
                    config.getString("mysql.host"),
                    config.getString("mysql.port"),
                    config.getString("mysql.base"),
                    config.getString("mysql.user"),
                    config.getString("mysql.pass"));
            if (database.getConnection() != null) {
                usesMySQL = true;
                log.info("Successfully connected to MySQL database!");
            }
        }
        if (!mySQLEnabled || !usesMySQL) {
            this.database = new SQLite(loggerFactory.create(SQLite.class, "Database"), plugin, "database.db");
            if (mySQLEnabled) {
                log.warn("No connection to the mySQL Database! Using SQLite for storing data as fallback!");
            } else {
                log.info("Using SQLite for storing data!");
            }
        }

        database.createTables();
    }

    private void registerListener(final CoreQuestRegistry coreQuestRegistry) {
        final PluginManager pluginManager = Bukkit.getPluginManager();
        List.of(
                new CombatTagger(profileProvider, config.getInt("conversation.damage.combat_delay")),
                new MobKillListener(profileProvider),
                new CustomDropListener(loggerFactory.create(CustomDropListener.class), questManager, plugin, questRegistry),
                new QuestItemHandler(config, playerDataStorage, profileProvider),
                new JoinQuitListener(loggerFactory, config, coreQuestRegistry.objectives(), playerDataStorage,
                        pluginMessage, profileProvider, updater)
        ).forEach(listener -> pluginManager.registerEvents(listener, plugin));
    }

    private void registerCommands(final AccumulatingReceiverSelector receiverSelector, final HistoryHandler debugHistoryHandler,
                                  final PlayerDataFactory playerDataFactory) {
        final QuestCommand questCommand = new QuestCommand(loggerFactory, loggerFactory.create(QuestCommand.class),
                configAccessorFactory, new PlayerLogWatcher(receiverSelector), debugHistoryHandler,
                MMORPGCore.getInstance().getQuestModule(), playerDataStorage, profileProvider, playerDataFactory, pluginMessage, config, compatibility);
        plugin.getCommand("betonquest").setExecutor(questCommand);
        plugin.getCommand("betonquest").setTabCompleter(questCommand);
        plugin.getCommand("journal").setExecutor(new JournalCommand(playerDataStorage, profileProvider));
        plugin.getCommand("backpack").setExecutor(new BackpackCommand(loggerFactory.create(BackpackCommand.class), config, pluginMessage, profileProvider));
        plugin.getCommand("cancelquest").setExecutor(new CancelQuestCommand(config, pluginMessage, profileProvider));
        plugin.getCommand("compass").setExecutor(new CompassCommand(config, pluginMessage, profileProvider));
        final LangCommand langCommand = new LangCommand(loggerFactory.create(LangCommand.class), playerDataStorage, pluginMessage, profileProvider, this);
        plugin.getCommand("questlang").setExecutor(langCommand);
        plugin.getCommand("questlang").setTabCompleter(langCommand);
        plugin.getCommand("betonquestanswer").setTabCompleter((sender, command, label, args) -> List.of());
    }

    private void migrate() {
        try {
            new Migrator(loggerFactory).migrate();
        } catch (final IOException e) {
            log.error("There was an exception while migrating from a previous version! Reason: " + e.getMessage(), e);
        }
    }

    private void setupUpdater() {
        final File updateFolder = plugin.getServer().getUpdateFolderFile();
        final File file = new File(updateFolder, plugin.getPluginFile().getName());
        final DownloadSource downloadSource = new TempFileDownloadSource(new WebDownloadSource());
        final UpdateDownloader updateDownloader = new UpdateDownloader(downloadSource, file);

        final NexusReleaseAndDevelopmentSource nexusReleaseAndDevelopmentSource = new NexusReleaseAndDevelopmentSource(
                "https://nexus.betonquest.org/", new WebContentSource());
        final GitHubReleaseSource gitHubReleaseSource = new GitHubReleaseSource(
                "https://api.github.com/repos/BetonQuest/BetonQuest",
                new WebContentSource(GitHubReleaseSource.HTTP_CODE_HANDLER));
        final List<ReleaseUpdateSource> releaseHandlers = List.of(nexusReleaseAndDevelopmentSource, gitHubReleaseSource);
        final List<DevelopmentUpdateSource> developmentHandlers = List.of(nexusReleaseAndDevelopmentSource);
        final UpdateSourceHandler updateSourceHandler = new UpdateSourceHandler(loggerFactory.create(UpdateSourceHandler.class),
                releaseHandlers, developmentHandlers);

        final Version pluginVersion = new Version(plugin.getDescription().getVersion());
        final UpdaterConfig updaterConfig = new UpdaterConfig(loggerFactory.create(UpdaterConfig.class), config, pluginVersion, DEV_INDICATOR);
        updater = new Updater(loggerFactory.create(Updater.class), updaterConfig, pluginVersion, updateSourceHandler, updateDownloader,
                plugin.getQuestModule(), plugin.getServer().getScheduler(), InstantSource.system());
    }

    @SuppressWarnings("PMD.DoNotUseThreads")
    private void registerLogHandler(final Server server, final Handler handler) {
        final java.util.logging.Logger serverLogger = server.getLogger().getParent();
        serverLogger.addHandler(handler);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            serverLogger.removeHandler(handler);
            handler.close();
        }));
    }

    public void loadData() {
        new LoadDataEvent(LoadDataEvent.State.PRE_LOAD).callEvent();
        questRegistry.loadData(getQuestPackageManager().getPackages().values());
        new LoadDataEvent(LoadDataEvent.State.POST_LOAD).callEvent();
        playerDataStorage.startObjectives();
        rpgMenu.syncCommands();
    }

    public void reload() {
        log.debug("Reloading configuration");
        try {
            config.reload();
        } catch (final IOException e) {
            log.warn("Could not reload config! " + e.getMessage(), e);
        }
        defaultLanguage = config.getString("language", "en-US");
        questManager.reload();
        try {
            pluginMessage.reload();
        } catch (final IOException | QuestException e) {
            log.error("Could not reload the plugin messages!", e);
        }
        Notify.load(config, getQuestPackageManager().getPackages().values());
        lastExecutionCache.reload();

        getUpdater().search();

        log.debug("Restarting global locations");
        try {
            conversationColors.load();
        } catch (final QuestException e) {
            log.warn("Could not reload conversation colors! " + e.getMessage(), e);
        }
        compatibility.reload();
        loadData();
        playerDataStorage.reloadProfiles(profileProvider.getOnlineProfiles());

        if (playerHider != null) {
            playerHider.stop();
        }
        try {
            playerHider = new PlayerHider(plugin, this, getVariableProcessor(), profileProvider);
        } catch (final QuestException e) {
            log.error("Could not start PlayerHider! " + e.getMessage(), e);
        }
    }

    // --- API and Getter methods ---

    public void addProcessor(final QuestProcessor<?, ?> processor) {
        questRegistry.additional().add(processor);
    }

    @Override
    public BetonQuestLoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    public ConfigAccessorFactory getConfigAccessorFactory() {
        return configAccessorFactory;
    }

    public RPGMenu getRpgMenu() {
        return rpgMenu;
    }

    public ConfigAccessor getPluginConfig() {
        return config;
    }

    @Override
    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public TextParser getTextParser() {
        return textParser;
    }

    public PluginMessage getPluginMessage() {
        return pluginMessage;
    }

    @Override
    public ProfileProvider getProfileProvider() {
        return profileProvider;
    }

    @Override
    public QuestPackageManager getQuestPackageManager() {
        return questManager;
    }

    @Override
    public QuestTypeApi getQuestTypeApi() {
        return questRegistry.core();
    }

    @Override
    public FeatureApi getFeatureApi() {
        return questRegistry;
    }

    public Database getDB() {
        return database;
    }

    public Updater getUpdater() {
        return updater;
    }

    public boolean isMySQLUsed() {
        return usesMySQL;
    }

    public GlobalData getGlobalData() {
        return globalData;
    }

    public Saver getSaver() {
        return saver;
    }

    public PlayerDataStorage getPlayerDataStorage() {
        return playerDataStorage;
    }

    @Override
    public QuestTypeRegistries getQuestRegistries() {
        return questTypeRegistries;
    }

    @Override
    public FeatureRegistries getFeatureRegistries() {
        return featureRegistries;
    }

    @Override
    public PrimaryServerThreadData getPrimaryServerThreadData() {
        return new PrimaryServerThreadData(plugin.getServer(), plugin.getServer().getScheduler(), plugin);
    }

    public VariableProcessor getVariableProcessor() {
        return questRegistry.core().variables();
    }

    public ConversationColors getConversationColors() {
        return conversationColors;
    }

    public FontRegistry getFontRegistry() {
        return fontRegistry;
    }
}
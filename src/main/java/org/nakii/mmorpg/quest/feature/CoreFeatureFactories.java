package org.nakii.mmorpg.quest.feature;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.QuestModule;
import org.nakii.mmorpg.quest.api.common.component.BookPageWrapper;
import org.nakii.mmorpg.quest.api.common.component.font.FontRegistry;
import org.nakii.mmorpg.quest.api.config.ConfigAccessor;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackageManager;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.quest.QuestTypeApi;
import org.nakii.mmorpg.quest.api.text.TextParser;
import org.nakii.mmorpg.quest.api.text.TextParserRegistry;
import org.nakii.mmorpg.quest.conversation.ConversationColors;
import org.nakii.mmorpg.quest.conversation.interceptor.NonInterceptingInterceptorFactory;
import org.nakii.mmorpg.quest.conversation.interceptor.SimpleInterceptorFactory;
import org.nakii.mmorpg.quest.conversation.io.InventoryConvIOFactory;
import org.nakii.mmorpg.quest.conversation.io.SimpleConvIOFactory;
import org.nakii.mmorpg.quest.conversation.io.SlowTellrawConvIOFactory;
import org.nakii.mmorpg.quest.conversation.io.TellrawConvIOFactory;
import org.nakii.mmorpg.quest.item.SimpleQuestItemFactory;
import org.nakii.mmorpg.quest.item.SimpleQuestItemSerializer;
import org.nakii.mmorpg.quest.kernel.registry.feature.*;
import org.nakii.mmorpg.quest.notify.SuppressNotifyIOFactory;
import org.nakii.mmorpg.quest.notify.io.*;
import org.nakii.mmorpg.quest.schedule.LastExecutionCache;
import org.nakii.mmorpg.quest.schedule.impl.realtime.cron.RealtimeCronSchedule;
import org.nakii.mmorpg.quest.schedule.impl.realtime.cron.RealtimeCronScheduler;
import org.nakii.mmorpg.quest.schedule.impl.realtime.daily.RealtimeDailySchedule;
import org.nakii.mmorpg.quest.schedule.impl.realtime.daily.RealtimeDailyScheduler;
import org.nakii.mmorpg.quest.text.parser.LegacyParser;
import org.nakii.mmorpg.quest.text.parser.MineDownParser;
import org.nakii.mmorpg.quest.text.parser.MiniMessageParser;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

/**
 * Registers the stuff that is not built from Instructions.
 */
public class CoreFeatureFactories {
    /**
     * Factory to create new class specific loggers.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * The quest package manager to get quest packages from.
     */
    private final QuestPackageManager packManager;

    /**
     * Cache to catch up missed schedulers.
     */
    private final LastExecutionCache lastExecutionCache;

    /**
     * Quest Type API.
     */
    private final QuestTypeApi questTypeApi;

    /**
     * The Config.
     */
    private final ConfigAccessor config;

    /**
     * The colors to use for the conversation.
     */
    private final ConversationColors colors;

    /**
     * The message parser to use for parsing messages.
     */
    private final TextParser textParser;

    /**
     * The font registry to use in APIs that work with {@link net.kyori.adventure.text.Component}.
     */
    private final FontRegistry fontRegistry;

    /**
     * Create a new Core Other Factories class for registering.
     *
     * @param loggerFactory      the factory to create new class specific loggers
     * @param packManager        the quest package manager to get quest packages from
     * @param lastExecutionCache the cache to catch up missed schedulers
     * @param questTypeApi       the class for executing events
     * @param config             the config
     * @param colors             the colors to use for the conversation
     * @param textParser         the text parser to use for parsing text
     * @param fontRegistry       the font registry to use for the conversation
     */
    public CoreFeatureFactories(final BetonQuestLoggerFactory loggerFactory, final QuestPackageManager packManager,
                                final LastExecutionCache lastExecutionCache, final QuestTypeApi questTypeApi,
                                final ConfigAccessor config, final ConversationColors colors,
                                final TextParser textParser, final FontRegistry fontRegistry) {
        this.loggerFactory = loggerFactory;
        this.packManager = packManager;
        this.lastExecutionCache = lastExecutionCache;
        this.questTypeApi = questTypeApi;
        this.config = config;
        this.colors = colors;
        this.textParser = textParser;
        this.fontRegistry = fontRegistry;
    }

    /**
     * Registers the Factories.
     *
     * @param registries containing the registry to register in
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public void register(final BaseFeatureRegistries registries) {
        final ConversationIORegistry conversationIOTypes = registries.conversationIO();
        conversationIOTypes.register("simple", new SimpleConvIOFactory(colors));
        conversationIOTypes.register("tellraw", new TellrawConvIOFactory(colors));
        conversationIOTypes.register("chest", new InventoryConvIOFactory(loggerFactory, packManager, config, fontRegistry, colors, false));
        conversationIOTypes.register("combined", new InventoryConvIOFactory(loggerFactory, packManager, config, fontRegistry, colors, true));
        conversationIOTypes.register("slowtellraw", new SlowTellrawConvIOFactory(fontRegistry, colors));

        final InterceptorRegistry interceptorTypes = registries.interceptor();
        interceptorTypes.register("simple", new SimpleInterceptorFactory());
        interceptorTypes.register("none", new NonInterceptingInterceptorFactory());

        final ItemTypeRegistry itemTypes = registries.item();
        final BookPageWrapper bookPageWrapper = new BookPageWrapper(fontRegistry, 114, 14);
        itemTypes.register("simple", new SimpleQuestItemFactory(packManager, textParser, bookPageWrapper));
        itemTypes.registerSerializer("simple", new SimpleQuestItemSerializer(textParser, bookPageWrapper));

        final Plugin plugin = MMORPGCore.getInstance();
        final NotifyIORegistry notifyIOTypes = registries.notifyIO();
        notifyIOTypes.register("suppress", new SuppressNotifyIOFactory());
        notifyIOTypes.register("chat", new ChatNotifyIOFactory());
        notifyIOTypes.register("advancement", new AdvancementNotifyIOFactory(plugin));
        notifyIOTypes.register("actionbar", new ActionBarNotifyIOFactory());
        notifyIOTypes.register("bossbar", new BossBarNotifyIOFactory(plugin));
        notifyIOTypes.register("title", new TitleNotifyIOFactory());
        notifyIOTypes.register("totem", new TotemNotifyIOFactory(plugin));
        notifyIOTypes.register("subtitle", new SubTitleNotifyIOFactory());
        notifyIOTypes.register("sound", new SoundIOFactory());

        final ScheduleRegistry eventSchedulingTypes = registries.eventScheduling();
        eventSchedulingTypes.register("realtime-daily", RealtimeDailySchedule::new, new RealtimeDailyScheduler(
                loggerFactory.create(RealtimeDailyScheduler.class, "Schedules"), questTypeApi, lastExecutionCache));
        eventSchedulingTypes.register("realtime-cron", RealtimeCronSchedule::new, new RealtimeCronScheduler(
                loggerFactory.create(RealtimeCronScheduler.class, "Schedules"), questTypeApi, lastExecutionCache));

        final TextParserRegistry textParserRegistry = registries.textParser();
        registerTextParsers(textParserRegistry);
    }

    private void registerTextParsers(final TextParserRegistry textParserRegistry) {
        final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .extractUrls()
                .build();
        textParserRegistry.register("legacy", new LegacyParser(legacySerializer));
        final MiniMessage miniMessage = MiniMessage.miniMessage();
        textParserRegistry.register("minimessage", new MiniMessageParser(miniMessage));
        final MiniMessage legacyMiniMessage = MiniMessage.builder()
                .preProcessor(input -> {
                    final TextComponent deserialize = legacySerializer.deserialize(ChatColor.translateAlternateColorCodes('&', input.replaceAll("(?<!\\\\)\\\\n", "\n")));
                    final String serialize = miniMessage.serialize(deserialize);
                    return serialize.replaceAll("\\\\<", "<");
                })
                .build();
        textParserRegistry.register("legacyminimessage", new MiniMessageParser(legacyMiniMessage));
        textParserRegistry.register("minedown", new MineDownParser());
    }
}

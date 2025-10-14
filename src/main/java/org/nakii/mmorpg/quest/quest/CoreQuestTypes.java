package org.nakii.mmorpg.quest.quest;

import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.QuestModule;
import org.nakii.mmorpg.quest.api.LanguageProvider;
import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.kernel.FeatureTypeRegistry;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.profile.ProfileProvider;
import org.nakii.mmorpg.quest.api.quest.PrimaryServerThreadData;
import org.nakii.mmorpg.quest.api.quest.QuestTypeApi;
import org.nakii.mmorpg.quest.config.PluginMessage;
import org.nakii.mmorpg.quest.data.PlayerDataStorage;
import org.nakii.mmorpg.quest.database.GlobalData;
import org.nakii.mmorpg.quest.database.PlayerDataFactory;
import org.nakii.mmorpg.quest.kernel.processor.quest.VariableProcessor;
import org.nakii.mmorpg.quest.kernel.registry.quest.BaseQuestTypeRegistries;
import org.nakii.mmorpg.quest.kernel.registry.quest.ConditionTypeRegistry;
import org.nakii.mmorpg.quest.kernel.registry.quest.EventTypeRegistry;
import org.nakii.mmorpg.quest.kernel.registry.quest.VariableTypeRegistry;
import org.nakii.mmorpg.quest.quest.condition.advancement.AdvancementConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.armor.ArmorConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.armor.ArmorRatingConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.biome.BiomeConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.block.BlockConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.burning.BurningConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.check.CheckConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.chest.ChestItemConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.conversation.ConversationConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.conversation.InConversationConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.effect.EffectConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.entity.EntityConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.eval.EvalConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.experience.ExperienceConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.facing.FacingConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.flying.FlyingConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.gamemode.GameModeConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.hand.HandConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.health.HealthConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.height.HeightConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.hunger.HungerConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.item.ItemConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.item.ItemDurabilityConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.journal.JournalConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.language.LanguageConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.location.LocationConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.logik.AlternativeConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.logik.ConjunctionConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.looking.LookingAtConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.moon.MoonPhaseConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.npc.NpcDistanceConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.npc.NpcLocationConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.number.NumberCompareConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.objective.ObjectiveConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.party.PartyConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.permission.PermissionConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.point.GlobalPointConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.point.PointConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.random.RandomConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.ride.RideConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.scoreboard.ScoreboardObjectiveConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.scoreboard.ScoreboardTagConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.slots.EmptySlotsConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.sneak.SneakConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.stage.StageConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.tag.GlobalTagConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.tag.TagConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.time.ingame.TimeConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.time.real.DayOfWeekConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.time.real.PartialDateConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.time.real.RealTimeConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.variable.VariableConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.weather.WeatherConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.world.WorldConditionFactory;
import org.nakii.mmorpg.quest.quest.event.burn.BurnEventFactory;
import org.nakii.mmorpg.quest.quest.event.cancel.CancelEventFactory;
import org.nakii.mmorpg.quest.quest.event.chat.ChatEventFactory;
import org.nakii.mmorpg.quest.quest.event.chest.ChestClearEventFactory;
import org.nakii.mmorpg.quest.quest.event.chest.ChestGiveEventFactory;
import org.nakii.mmorpg.quest.quest.event.chest.ChestTakeEventFactory;
import org.nakii.mmorpg.quest.quest.event.command.CommandEventFactory;
import org.nakii.mmorpg.quest.quest.event.command.OpSudoEventFactory;
import org.nakii.mmorpg.quest.quest.event.command.SudoEventFactory;
import org.nakii.mmorpg.quest.quest.event.compass.CompassEventFactory;
import org.nakii.mmorpg.quest.quest.event.conversation.CancelConversationEventFactory;
import org.nakii.mmorpg.quest.quest.event.conversation.ConversationEventFactory;
import org.nakii.mmorpg.quest.quest.event.damage.DamageEventFactory;
import org.nakii.mmorpg.quest.quest.event.door.DoorEventFactory;
import org.nakii.mmorpg.quest.quest.event.drop.DropEventFactory;
import org.nakii.mmorpg.quest.quest.event.effect.DeleteEffectEventFactory;
import org.nakii.mmorpg.quest.quest.event.effect.EffectEventFactory;
import org.nakii.mmorpg.quest.quest.event.entity.RemoveEntityEventFactory;
import org.nakii.mmorpg.quest.quest.event.eval.EvalEventFactory;
import org.nakii.mmorpg.quest.quest.event.experience.ExperienceEventFactory;
import org.nakii.mmorpg.quest.quest.event.explosion.ExplosionEventFactory;
import org.nakii.mmorpg.quest.quest.event.folder.FolderEventFactory;
import org.nakii.mmorpg.quest.quest.event.give.GiveEventFactory;
import org.nakii.mmorpg.quest.quest.event.hunger.HungerEventFactory;
import org.nakii.mmorpg.quest.quest.event.item.ItemDurabilityEventFactory;
import org.nakii.mmorpg.quest.quest.event.journal.GiveJournalEventFactory;
import org.nakii.mmorpg.quest.quest.event.journal.JournalEventFactory;
import org.nakii.mmorpg.quest.quest.event.kill.KillEventFactory;
import org.nakii.mmorpg.quest.quest.event.language.LanguageEventFactory;
import org.nakii.mmorpg.quest.quest.event.lever.LeverEventFactory;
import org.nakii.mmorpg.quest.quest.event.lightning.LightningEventFactory;
import org.nakii.mmorpg.quest.quest.event.log.LogEventFactory;
import org.nakii.mmorpg.quest.quest.event.logic.FirstEventFactory;
import org.nakii.mmorpg.quest.quest.event.logic.IfElseEventFactory;
import org.nakii.mmorpg.quest.quest.event.notify.NotifyAllEventFactory;
import org.nakii.mmorpg.quest.quest.event.notify.NotifyEventFactory;
import org.nakii.mmorpg.quest.quest.event.npc.NpcTeleportEventFactory;
import org.nakii.mmorpg.quest.quest.event.npc.UpdateVisibilityNowEventFactory;
import org.nakii.mmorpg.quest.quest.event.objective.ObjectiveEventFactory;
import org.nakii.mmorpg.quest.quest.event.party.PartyEventFactory;
import org.nakii.mmorpg.quest.quest.event.point.DeleteGlobalPointEventFactory;
import org.nakii.mmorpg.quest.quest.event.point.DeletePointEventFactory;
import org.nakii.mmorpg.quest.quest.event.point.GlobalPointEventFactory;
import org.nakii.mmorpg.quest.quest.event.point.PointEventFactory;
import org.nakii.mmorpg.quest.quest.event.random.PickRandomEventFactory;
import org.nakii.mmorpg.quest.quest.event.run.RunEventFactory;
import org.nakii.mmorpg.quest.quest.event.run.RunForAllEventFactory;
import org.nakii.mmorpg.quest.quest.event.run.RunIndependentEventFactory;
import org.nakii.mmorpg.quest.quest.event.scoreboard.ScoreboardObjectiveEventFactory;
import org.nakii.mmorpg.quest.quest.event.scoreboard.ScoreboardTagEventFactory;
import org.nakii.mmorpg.quest.quest.event.setblock.SetBlockEventFactory;
import org.nakii.mmorpg.quest.quest.event.spawn.SpawnMobEventFactory;
import org.nakii.mmorpg.quest.quest.event.stage.StageEventFactory;
import org.nakii.mmorpg.quest.quest.event.tag.TagGlobalEventFactory;
import org.nakii.mmorpg.quest.quest.event.tag.TagPlayerEventFactory;
import org.nakii.mmorpg.quest.quest.event.take.TakeEventFactory;
import org.nakii.mmorpg.quest.quest.event.teleport.TeleportEventFactory;
import org.nakii.mmorpg.quest.quest.event.time.TimeEventFactory;
import org.nakii.mmorpg.quest.quest.event.variable.VariableEventFactory;
import org.nakii.mmorpg.quest.quest.event.velocity.VelocityEventFactory;
import org.nakii.mmorpg.quest.quest.event.weather.WeatherEventFactory;
import org.nakii.mmorpg.quest.quest.objective.action.ActionObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.arrow.ArrowShootObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.block.BlockObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.breed.BreedObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.brew.BrewObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.chestput.ChestPutObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.command.CommandObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.consume.ConsumeObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.crafting.CraftingObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.delay.DelayObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.die.DieObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.enchant.EnchantObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.equip.EquipItemObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.experience.ExperienceObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.fish.FishObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.interact.EntityInteractObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.jump.JumpObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.kill.KillPlayerObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.kill.MobKillObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.location.LocationObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.login.LoginObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.logout.LogoutObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.npc.NpcInteractObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.npc.NpcRangeObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.password.PasswordObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.pickup.PickupObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.resourcepack.ResourcepackObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.ride.RideObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.shear.ShearObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.smelt.SmeltingObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.stage.StageObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.step.StepObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.tame.TameObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.timer.TimerObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.variable.VariableObjectiveFactory;
import org.nakii.mmorpg.quest.quest.variable.condition.ConditionVariableFactory;
import org.nakii.mmorpg.quest.quest.variable.constant.ConstantVariableFactory;
import org.nakii.mmorpg.quest.quest.variable.eval.EvalVariableFactory;
import org.nakii.mmorpg.quest.quest.variable.item.ItemDurabilityVariableFactory;
import org.nakii.mmorpg.quest.quest.variable.item.ItemVariableFactory;
import org.nakii.mmorpg.quest.quest.variable.location.LocationVariableFactory;
import org.nakii.mmorpg.quest.quest.variable.math.MathVariableFactory;
import org.nakii.mmorpg.quest.quest.variable.name.PlayerNameVariableFactory;
import org.nakii.mmorpg.quest.quest.variable.name.QuesterVariableFactory;
import org.nakii.mmorpg.quest.quest.variable.npc.NpcVariableFactory;
import org.nakii.mmorpg.quest.quest.variable.objective.ObjectivePropertyVariableFactory;
import org.nakii.mmorpg.quest.quest.variable.point.GlobalPointVariableFactory;
import org.nakii.mmorpg.quest.quest.variable.point.PointVariableFactory;
import org.nakii.mmorpg.quest.quest.variable.random.RandomNumberVariableFactory;
import org.nakii.mmorpg.quest.quest.variable.tag.GlobalTagVariableFactory;
import org.nakii.mmorpg.quest.quest.variable.tag.TagVariableFactory;
import org.nakii.mmorpg.quest.quest.variable.version.VersionVariableFactory;
import org.bukkit.Server;
import org.bukkit.scheduler.BukkitScheduler;

import java.time.InstantSource;

/**
 * Registers the Conditions, Events, Objectives and Variables that come with BetonQuest.
 */
@SuppressWarnings("PMD.NcssCount")
public class CoreQuestTypes {
    /**
     * Logger Factory to create new custom Logger from.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * Server used for primary server thread access.
     */
    private final Server server;

    /**
     * Plugin used for primary server thread access, type registration and general usage.
     */
    private final QuestModule questModule;

    /**
     * Server, Scheduler and Plugin used for primary server thread access.
     */
    private final PrimaryServerThreadData data;

    /**
     * Quest Type API.
     */
    private final QuestTypeApi questTypeApi;

    /**
     * The {@link PluginMessage} instance.
     */
    private final PluginMessage pluginMessage;

    /**
     * Variable processor to create new variables.
     */
    private final VariableProcessor variableProcessor;

    /**
     * Storage for global data.
     */
    private final GlobalData globalData;

    /**
     * Storage for player data.
     */
    private final PlayerDataStorage dataStorage;

    /**
     * The profile provider instance.
     */
    private final ProfileProvider profileProvider;

    /**
     * The language provider to get the default language.
     */
    private final LanguageProvider languageProvider;

    /**
     * Factory to create new Player Data.
     */
    private final PlayerDataFactory playerDataFactory;

    /**
     * Create a new Core Quest Types class for registering.
     *
     * @param loggerFactory     used in factories
     * @param server            the server used for primary server thread access.
     * @param scheduler         the scheduler used for primary server thread access
     * @param questModule        the plugin used for primary server access and type registration
     * @param questTypeApi      the Quest Type API
     * @param pluginMessage     the plugin message instance
     * @param variableProcessor the variable processor to create new variables
     * @param globalData        the storage providing global data
     * @param dataStorage       the storage providing player data
     * @param profileProvider   the profile provider instance
     * @param languageProvider  the language provider to get the default language
     * @param playerDataFactory the factory to create player data
     */
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public CoreQuestTypes(final BetonQuestLoggerFactory loggerFactory,
                          final Server server, final BukkitScheduler scheduler, final MMORPGCore questModule,
                          final QuestTypeApi questTypeApi, final PluginMessage pluginMessage,
                          final VariableProcessor variableProcessor, final GlobalData globalData,
                          final PlayerDataStorage dataStorage, final ProfileProvider profileProvider,
                          final LanguageProvider languageProvider, final PlayerDataFactory playerDataFactory) {
        this.loggerFactory = loggerFactory;
        this.server = server;
        this.questModule = questModule.getQuestModule();
        this.questTypeApi = questTypeApi;
        this.pluginMessage = pluginMessage;
        this.variableProcessor = variableProcessor;
        this.globalData = globalData;
        this.dataStorage = dataStorage;
        this.profileProvider = profileProvider;
        this.languageProvider = languageProvider;
        this.playerDataFactory = playerDataFactory;
        this.data = new PrimaryServerThreadData(server, scheduler, questModule);
    }

    /**
     * Registers the Quest Types.
     *
     * @param questTypeRegistries the registry to register the types in
     */
    public void register(final BaseQuestTypeRegistries questTypeRegistries) {
        // When adding new types they need to be ordered by name in the corresponding method!
        registerConditions(questTypeRegistries.condition());
        registerEvents(questTypeRegistries.event());
        registerObjectives(questTypeRegistries.objective());
        registerVariables(questTypeRegistries.variable());
    }

    private void registerConditions(final ConditionTypeRegistry conditionTypes) {
        conditionTypes.register("advancement", new AdvancementConditionFactory(data, loggerFactory));
        conditionTypes.registerCombined("and", new ConjunctionConditionFactory(questTypeApi));
        conditionTypes.register("armor", new ArmorConditionFactory(loggerFactory, data));
        conditionTypes.register("biome", new BiomeConditionFactory(loggerFactory, data));
        conditionTypes.register("burning", new BurningConditionFactory(loggerFactory, data));
        conditionTypes.registerCombined("check", new CheckConditionFactory(questModule.getQuestPackageManager(), conditionTypes));
        conditionTypes.registerCombined("chestitem", new ChestItemConditionFactory(data));
        conditionTypes.register("conversation", new ConversationConditionFactory(questModule.getFeatureApi()));
        conditionTypes.register("dayofweek", new DayOfWeekConditionFactory(loggerFactory.create(DayOfWeekConditionFactory.class)));
        conditionTypes.register("effect", new EffectConditionFactory(loggerFactory, data));
        conditionTypes.register("empty", new EmptySlotsConditionFactory(loggerFactory, data));
        conditionTypes.registerCombined("entities", new EntityConditionFactory(data));
        conditionTypes.registerCombined("eval", new EvalConditionFactory(questModule.getQuestPackageManager(), conditionTypes));
        conditionTypes.register("experience", new ExperienceConditionFactory(loggerFactory, data));
        conditionTypes.register("facing", new FacingConditionFactory(loggerFactory, data));
        conditionTypes.register("fly", new FlyingConditionFactory(loggerFactory, data));
        conditionTypes.register("gamemode", new GameModeConditionFactory(loggerFactory, data));
        conditionTypes.registerCombined("globalpoint", new GlobalPointConditionFactory(questModule.getGlobalData()));
        conditionTypes.register("globaltag", new GlobalTagConditionFactory(questModule.getGlobalData()));
        conditionTypes.register("hand", new HandConditionFactory(loggerFactory, data));
        conditionTypes.register("health", new HealthConditionFactory(loggerFactory, data));
        conditionTypes.register("height", new HeightConditionFactory(loggerFactory, data));
        conditionTypes.register("hunger", new HungerConditionFactory(loggerFactory, data));
        conditionTypes.register("inconversation", new InConversationConditionFactory());
        conditionTypes.register("item", new ItemConditionFactory(loggerFactory, data, dataStorage));
        conditionTypes.register("itemdurability", new ItemDurabilityConditionFactory(loggerFactory, data));
        conditionTypes.register("journal", new JournalConditionFactory(dataStorage, loggerFactory));
        conditionTypes.register("language", new LanguageConditionFactory(dataStorage, languageProvider, pluginMessage));
        conditionTypes.register("location", new LocationConditionFactory(data, loggerFactory));
        conditionTypes.register("looking", new LookingAtConditionFactory(loggerFactory, data));
        conditionTypes.registerCombined("moonphase", new MoonPhaseConditionFactory(data));
        conditionTypes.register("npcdistance", new NpcDistanceConditionFactory(questModule.getFeatureApi(), data, loggerFactory));
        conditionTypes.registerCombined("npclocation", new NpcLocationConditionFactory(questModule.getFeatureApi(), data));
        conditionTypes.registerCombined("numbercompare", new NumberCompareConditionFactory());
        conditionTypes.register("objective", new ObjectiveConditionFactory(questTypeApi));
        conditionTypes.registerCombined("or", new AlternativeConditionFactory(loggerFactory, questTypeApi));
        conditionTypes.register("partialdate", new PartialDateConditionFactory());
        conditionTypes.registerCombined("party", new PartyConditionFactory(questTypeApi, profileProvider));
        conditionTypes.register("permission", new PermissionConditionFactory(loggerFactory, data));
        conditionTypes.register("point", new PointConditionFactory(dataStorage));
        conditionTypes.registerCombined("random", new RandomConditionFactory());
        conditionTypes.register("rating", new ArmorRatingConditionFactory(loggerFactory, data));
        conditionTypes.register("realtime", new RealTimeConditionFactory());
        conditionTypes.register("ride", new RideConditionFactory(loggerFactory, data));
        conditionTypes.register("score", new ScoreboardObjectiveConditionFactory(data));
        conditionTypes.register("scoretag", new ScoreboardTagConditionFactory(data, loggerFactory));
        conditionTypes.register("sneak", new SneakConditionFactory(loggerFactory, data));
        conditionTypes.register("stage", new StageConditionFactory(questTypeApi));
        conditionTypes.register("tag", new TagConditionFactory(dataStorage));
        conditionTypes.registerCombined("testforblock", new BlockConditionFactory(data));
        conditionTypes.registerCombined("time", new TimeConditionFactory(data, variableProcessor));
        conditionTypes.registerCombined("variable", new VariableConditionFactory(loggerFactory, data));
        conditionTypes.registerCombined("weather", new WeatherConditionFactory(data, variableProcessor));
        conditionTypes.register("world", new WorldConditionFactory(loggerFactory, data, variableProcessor));
    }

    private void registerEvents(final EventTypeRegistry eventTypes) {
        eventTypes.register("burn", new BurnEventFactory(loggerFactory, data));
        eventTypes.register("cancel", new CancelEventFactory(loggerFactory, questModule.getFeatureApi()));
        eventTypes.register("cancelconversation", new CancelConversationEventFactory(loggerFactory));
        eventTypes.register("chat", new ChatEventFactory(loggerFactory, data));
        eventTypes.registerCombined("chestclear", new ChestClearEventFactory(data));
        eventTypes.registerCombined("chestgive", new ChestGiveEventFactory(data));
        eventTypes.registerCombined("chesttake", new ChestTakeEventFactory(data));
        eventTypes.register("compass", new CompassEventFactory(questModule.getFeatureApi(), dataStorage, data));
        eventTypes.registerCombined("command", new CommandEventFactory(loggerFactory, data));
        eventTypes.register("conversation", new ConversationEventFactory(loggerFactory, questModule.getQuestPackageManager(), data, pluginMessage));
        eventTypes.register("damage", new DamageEventFactory(loggerFactory, data));
        eventTypes.register("deleffect", new DeleteEffectEventFactory(loggerFactory, data));
        eventTypes.registerCombined("deleteglobalpoint", new DeleteGlobalPointEventFactory(globalData));
        eventTypes.registerCombined("deletepoint", new DeletePointEventFactory(dataStorage, questModule.getSaver(), profileProvider));
        eventTypes.registerCombined("door", new DoorEventFactory(data));
        eventTypes.registerCombined("drop", new DropEventFactory(profileProvider, data));
        eventTypes.register("effect", new EffectEventFactory(loggerFactory, data));
        eventTypes.registerCombined("eval", new EvalEventFactory(questModule.getQuestPackageManager(), eventTypes));
        eventTypes.register("experience", new ExperienceEventFactory(loggerFactory, data));
        eventTypes.registerCombined("explosion", new ExplosionEventFactory(data));
        eventTypes.registerCombined("folder", new FolderEventFactory(questModule, loggerFactory, server.getPluginManager(), questTypeApi));
        eventTypes.registerCombined("first", new FirstEventFactory(questTypeApi));
        eventTypes.register("give", new GiveEventFactory(loggerFactory, data, dataStorage, pluginMessage));
        eventTypes.register("givejournal", new GiveJournalEventFactory(loggerFactory, dataStorage, data));
        eventTypes.registerCombined("globaltag", new TagGlobalEventFactory(questModule));
        eventTypes.registerCombined("globalpoint", new GlobalPointEventFactory(globalData));
        eventTypes.register("hunger", new HungerEventFactory(loggerFactory, data));
        eventTypes.registerCombined("if", new IfElseEventFactory(questTypeApi));
        eventTypes.register("itemdurability", new ItemDurabilityEventFactory(loggerFactory, data));
        eventTypes.registerCombined("journal", new JournalEventFactory(loggerFactory, pluginMessage, dataStorage,
                InstantSource.system(), questModule.getSaver(), profileProvider));
        eventTypes.register("kill", new KillEventFactory(loggerFactory, data));
        eventTypes.register("language", new LanguageEventFactory(dataStorage));
        eventTypes.registerCombined("lever", new LeverEventFactory(data));
        eventTypes.registerCombined("lightning", new LightningEventFactory(data));
        eventTypes.registerCombined("log", new LogEventFactory(loggerFactory));
        eventTypes.register("notify", new NotifyEventFactory(loggerFactory, data, questModule.getTextParser(), dataStorage, languageProvider));
        eventTypes.registerCombined("notifyall", new NotifyAllEventFactory(loggerFactory, data, questModule.getTextParser(), dataStorage, profileProvider, languageProvider));
        eventTypes.registerCombined("npcteleport", new NpcTeleportEventFactory(questModule.getFeatureApi(), data));
        eventTypes.registerCombined("objective", new ObjectiveEventFactory(questModule, loggerFactory, questTypeApi, playerDataFactory));
        eventTypes.register("opsudo", new OpSudoEventFactory(loggerFactory, data));
        eventTypes.register("party", new PartyEventFactory(loggerFactory, questTypeApi, profileProvider));
        eventTypes.registerCombined("pickrandom", new PickRandomEventFactory(questModule.getQuestPackageManager(), questTypeApi));
        eventTypes.register("point", new PointEventFactory(loggerFactory, dataStorage,
                pluginMessage));
        eventTypes.registerCombined("removeentity", new RemoveEntityEventFactory(data));
        eventTypes.registerCombined("run", new RunEventFactory(questModule.getQuestPackageManager(), eventTypes));
        eventTypes.register("runForAll", new RunForAllEventFactory(questTypeApi, profileProvider));
        eventTypes.register("runIndependent", new RunIndependentEventFactory(questTypeApi));
        eventTypes.registerCombined("setblock", new SetBlockEventFactory(data));
        eventTypes.register("score", new ScoreboardObjectiveEventFactory(data));
        eventTypes.register("scoretag", new ScoreboardTagEventFactory(loggerFactory, data));
        eventTypes.registerCombined("spawn", new SpawnMobEventFactory(data));
        eventTypes.register("stage", new StageEventFactory(questTypeApi));
        eventTypes.register("sudo", new SudoEventFactory(loggerFactory, data));
        eventTypes.registerCombined("tag", new TagPlayerEventFactory(dataStorage, questModule.getSaver(), profileProvider));
        eventTypes.register("take", new TakeEventFactory(loggerFactory, pluginMessage));
        eventTypes.register("teleport", new TeleportEventFactory(loggerFactory, data));
        eventTypes.registerCombined("time", new TimeEventFactory(server, data));
        eventTypes.register("updatevisibility", new UpdateVisibilityNowEventFactory(questModule.getFeatureApi().getNpcHider(), loggerFactory, data));
        eventTypes.register("variable", new VariableEventFactory(questTypeApi));
        eventTypes.register("velocity", new VelocityEventFactory(loggerFactory, data));
        eventTypes.registerCombined("weather", new WeatherEventFactory(loggerFactory, data));
    }

    private void registerObjectives(final FeatureTypeRegistry<Objective> objectiveTypes) {
        objectiveTypes.register("action", new ActionObjectiveFactory());
        objectiveTypes.register("arrow", new ArrowShootObjectiveFactory());
        objectiveTypes.register("block", new BlockObjectiveFactory(loggerFactory, pluginMessage));
        objectiveTypes.register("breed", new BreedObjectiveFactory());
        objectiveTypes.register("brew", new BrewObjectiveFactory(profileProvider));
        objectiveTypes.register("chestput", new ChestPutObjectiveFactory(loggerFactory, pluginMessage));
        objectiveTypes.register("command", new CommandObjectiveFactory());
        objectiveTypes.register("consume", new ConsumeObjectiveFactory());
        objectiveTypes.register("craft", new CraftingObjectiveFactory());
        objectiveTypes.register("delay", new DelayObjectiveFactory());
        objectiveTypes.register("die", new DieObjectiveFactory());
        objectiveTypes.register("enchant", new EnchantObjectiveFactory());
        objectiveTypes.register("experience", new ExperienceObjectiveFactory(loggerFactory, pluginMessage));
        objectiveTypes.register("fish", new FishObjectiveFactory());
        objectiveTypes.register("interact", new EntityInteractObjectiveFactory());
        objectiveTypes.register("kill", new KillPlayerObjectiveFactory());
        objectiveTypes.register("location", new LocationObjectiveFactory());
        objectiveTypes.register("login", new LoginObjectiveFactory());
        objectiveTypes.register("logout", new LogoutObjectiveFactory());
        objectiveTypes.register("mobkill", new MobKillObjectiveFactory());
        objectiveTypes.register("npcinteract", new NpcInteractObjectiveFactory());
        objectiveTypes.register("npcrange", new NpcRangeObjectiveFactory());
        objectiveTypes.register("password", new PasswordObjectiveFactory());
        objectiveTypes.register("pickup", new PickupObjectiveFactory());
        objectiveTypes.register("ride", new RideObjectiveFactory());
        objectiveTypes.register("shear", new ShearObjectiveFactory());
        objectiveTypes.register("smelt", new SmeltingObjectiveFactory());
        objectiveTypes.register("stage", new StageObjectiveFactory());
        objectiveTypes.register("step", new StepObjectiveFactory());
        objectiveTypes.register("tame", new TameObjectiveFactory());
        objectiveTypes.register("timer", new TimerObjectiveFactory(questTypeApi));
        objectiveTypes.register("variable", new VariableObjectiveFactory());
        objectiveTypes.register("equip", new EquipItemObjectiveFactory());
        objectiveTypes.register("jump", new JumpObjectiveFactory());
        objectiveTypes.register("resourcepack", new ResourcepackObjectiveFactory());
    }

    private void registerVariables(final VariableTypeRegistry variables) {
        variables.register("condition", new ConditionVariableFactory(questTypeApi, pluginMessage));
        variables.registerCombined("constant", new ConstantVariableFactory());
        variables.registerCombined("eval", new EvalVariableFactory());
        variables.register("globalpoint", new GlobalPointVariableFactory(globalData, loggerFactory.create(GlobalPointVariableFactory.class)));
        variables.register("globaltag", new GlobalTagVariableFactory(globalData, pluginMessage));
        variables.registerCombined("item", new ItemVariableFactory(questModule.getPlayerDataStorage()));
        variables.register("itemdurability", new ItemDurabilityVariableFactory());
        variables.register("location", new LocationVariableFactory());
        variables.registerCombined("math", new MathVariableFactory(variableProcessor));
        variables.registerCombined("npc", new NpcVariableFactory(questModule.getFeatureApi()));
        variables.register("objective", new ObjectivePropertyVariableFactory(questTypeApi));
        variables.register("point", new PointVariableFactory(dataStorage, loggerFactory.create(PointVariableFactory.class)));
        variables.register("player", new PlayerNameVariableFactory());
        variables.register("quester", new QuesterVariableFactory());
        variables.registerCombined("randomnumber", new RandomNumberVariableFactory());
        variables.register("tag", new TagVariableFactory(dataStorage, pluginMessage));
        variables.register("version", new VersionVariableFactory(MMORPGCore.getInstance()));
    }
}

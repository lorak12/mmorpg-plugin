package org.nakii.mmorpg.quest.quest.event.objective;

import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.QuestModule;
import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.profile.ProfileProvider;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.QuestTypeApi;
import org.nakii.mmorpg.quest.api.quest.event.nullable.NullableEvent;
import org.nakii.mmorpg.quest.api.quest.objective.ObjectiveID;
import org.nakii.mmorpg.quest.database.PlayerData;
import org.nakii.mmorpg.quest.database.PlayerDataFactory;
import org.nakii.mmorpg.quest.database.Saver;
import org.nakii.mmorpg.quest.database.UpdateType;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * The objective event, that adds, removes oder completes objectives.
 */
public class ObjectiveEvent implements NullableEvent {

    /**
     * Custom {@link BetonQuestLogger} instance for this class.
     */
    private final BetonQuestLogger log;

    /**
     * The quest package.
     */
    private final QuestPackage questPackage;

    /**
     * The BetonQuest instance.
     */
    private final QuestModule questModule;

    /**
     * All objectives affected by this event.
     */
    private final Variable<List<ObjectiveID>> objectives;

    /**
     * API for starting objectives.
     */
    private final QuestTypeApi questTypeApi;

    /**
     * Factory to create new Player Data.
     */
    private final PlayerDataFactory playerDataFactory;

    /**
     * The action to do with the objectives.
     */
    private final String action;

    /**
     * Creates a new ObjectiveEvent.
     *
     * @param questModule        the BetonQuest instance
     * @param questTypeApi      the class for starting objectives
     * @param log               the logger
     * @param questPackage      the quest package of the instruction
     * @param objectives        the objectives to affect
     * @param playerDataFactory the factory to create player data
     * @param action            the action to do with the objectives
     * @throws QuestException if the action is invalid
     */
    public ObjectiveEvent(final QuestModule questModule, final BetonQuestLogger log, final QuestTypeApi questTypeApi,
                          final QuestPackage questPackage, final Variable<List<ObjectiveID>> objectives, final PlayerDataFactory playerDataFactory, final String action) throws QuestException {
        this.log = log;
        this.questPackage = questPackage;
        this.questModule = questModule;
        this.objectives = objectives;
        this.questTypeApi = questTypeApi;
        this.playerDataFactory = playerDataFactory;
        if (!Arrays.asList("start", "add", "delete", "remove", "complete", "finish").contains(action)) {
            throw new QuestException("Invalid action: " + action);
        }
        this.action = action.toLowerCase(Locale.ROOT);
    }

    @Override
    public void execute(@Nullable final Profile profile) throws QuestException {
        for (final ObjectiveID objectiveID : objectives.getValue(profile)) {
            final Objective objective = questTypeApi.getObjective(objectiveID);
            if (profile == null) {
                handleStatic(objectiveID, objective);
            } else if (profile.getOnlineProfile().isEmpty()) {
                handleForOfflinePlayer(profile, objectiveID);
            } else {
                handleForOnlinePlayer(profile, objectiveID, objective);
            }
        }
    }

    private void handleStatic(final ObjectiveID objectiveID, final Objective objective) {
        if ("delete".equals(action) || "remove".equals(action)) {
            final ProfileProvider profileProvider = MMORPGCore.getInstance().getQuestModule().getProfileProvider();
            profileProvider.getOnlineProfiles().forEach(onlineProfile -> cancelObjectiveForOnlinePlayer(onlineProfile, objectiveID, objective));
            questModule.getSaver().add(new Saver.Record(UpdateType.REMOVE_ALL_OBJECTIVES, objectiveID.toString()));
        } else {
            log.warn(questPackage, "You tried to call an objective add / finish event in a static context! Only objective delete works here.");
        }
    }

    private void handleForOnlinePlayer(final Profile profile, final ObjectiveID objectiveID, final Objective objective) {
        switch (action.toLowerCase(Locale.ROOT)) {
            case "start", "add" -> questTypeApi.newObjective(profile, objectiveID);
            case "complete", "finish" -> objective.completeObjective(profile);
            default -> cancelObjectiveForOnlinePlayer(profile, objectiveID, objective);
        }
    }

    private void handleForOfflinePlayer(final Profile profile, final ObjectiveID objectiveID) {
        Bukkit.getScheduler().runTaskAsynchronously(MMORPGCore.getInstance(), () -> {
            final PlayerData playerData = playerDataFactory.createPlayerData(profile);
            switch (action.toLowerCase(Locale.ROOT)) {
                case "start", "add" -> playerData.addNewRawObjective(objectiveID);
                case "complete", "finish" ->
                        log.warn(questPackage, "Cannot complete objective for " + profile + ", because he is offline!");
                default -> playerData.removeRawObjective(objectiveID);
            }
        });
    }

    private void cancelObjectiveForOnlinePlayer(final Profile profile, final ObjectiveID objectiveID, final Objective objective) {
        objective.cancelObjectiveForPlayer(profile);
        questModule.getPlayerDataStorage().get(profile).removeRawObjective(objectiveID);
    }
}

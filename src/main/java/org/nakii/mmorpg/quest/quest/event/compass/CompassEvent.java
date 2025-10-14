package org.nakii.mmorpg.quest.quest.event.compass;

import org.nakii.mmorpg.quest.api.bukkit.event.QuestCompassTargetChangeEvent;
import org.nakii.mmorpg.quest.api.feature.FeatureApi;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.instruction.variable.VariableList;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.nakii.mmorpg.quest.data.PlayerDataStorage;
import org.nakii.mmorpg.quest.feature.QuestCompass;
import org.nakii.mmorpg.quest.id.CompassID;
import org.nakii.mmorpg.quest.quest.event.tag.AddTagChanger;
import org.nakii.mmorpg.quest.quest.event.tag.DeleteTagChanger;
import org.nakii.mmorpg.quest.quest.event.tag.TagChanger;
import org.nakii.mmorpg.quest.quest.event.tag.TagEvent;
import org.bukkit.Location;

/**
 * Event to set a compass target and manage compass points.
 */
public class CompassEvent implements PlayerEvent {
    /**
     * Feature API.
     */
    private final FeatureApi featureApi;

    /**
     * Storage to get the offline player data.
     */
    private final PlayerDataStorage dataStorage;

    /**
     * The action to perform on the compass.
     */
    private final Variable<CompassTargetAction> action;

    /**
     * The compass point to set.
     */
    private final Variable<CompassID> compassId;

    /**
     * Create the compass event.
     *
     * @param featureApi the Feature API
     * @param storage    the storage to get the offline player data
     * @param action     the action to perform
     * @param compassId  the compass point
     */
    public CompassEvent(final FeatureApi featureApi, final PlayerDataStorage storage,
                        final Variable<CompassTargetAction> action, final Variable<CompassID> compassId) {
        this.featureApi = featureApi;
        this.dataStorage = storage;
        this.action = action;
        this.compassId = compassId;
    }

    @Override
    public void execute(final Profile profile) throws QuestException {
        final CompassID compassId = this.compassId.getValue(profile);
        switch (action.getValue(profile)) {
            case ADD -> changeTag(new AddTagChanger(new VariableList<>(compassId.getTag())), profile);
            case DEL -> changeTag(new DeleteTagChanger(new VariableList<>(compassId.getTag())), profile);
            case SET -> {
                final QuestCompass compass = featureApi.getCompasses().get(compassId);
                if (compass == null) {
                    throw new QuestException("No compass found for id '" + compassId + "' found.");
                }
                final Location location = compass.location().getValue(profile);
                if (profile.getOnlineProfile().isPresent() && new QuestCompassTargetChangeEvent(profile, location).callEvent()) {
                    profile.getOnlineProfile().get().getPlayer().setCompassTarget(location);
                }
            }
        }
    }

    private void changeTag(final TagChanger tagChanger, final Profile profile) throws QuestException {
        new TagEvent(dataStorage::getOffline, tagChanger).execute(profile);
    }
}

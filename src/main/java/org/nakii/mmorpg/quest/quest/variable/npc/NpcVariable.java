package org.nakii.mmorpg.quest.quest.variable.npc;

import org.nakii.mmorpg.quest.api.feature.FeatureApi;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.npc.Npc;
import org.nakii.mmorpg.quest.api.quest.npc.NpcID;
import org.nakii.mmorpg.quest.api.quest.variable.nullable.NullableVariable;
import org.nakii.mmorpg.quest.quest.variable.location.LocationFormationMode;
import org.jetbrains.annotations.Nullable;

import static org.nakii.mmorpg.quest.quest.variable.npc.Argument.LOCATION;

/**
 * Provides information about a npc.
 */
public class NpcVariable implements NullableVariable {

    /**
     * Feature API.
     */
    private final FeatureApi featureApi;

    /**
     * Id of the npc.
     */
    private final Variable<NpcID> npcID;

    /**
     * The type of information to retrieve for the NPC: name, full_name, or location.
     */
    private final Argument key;

    /**
     * The location formation mode to use for location resolution.
     */
    @Nullable
    private final LocationFormationMode formationMode;

    /**
     * The number of decimal places to use for location resolution.
     */
    private final int decimalPlaces;

    /**
     * Construct a new NPCVariable that allows for resolution of information about a NPC.
     *
     * @param featureApi    the Feature API
     * @param npcID         the npc id
     * @param key           the argument defining the value
     * @param formationMode the location formation mode to use for location resolution
     * @param decimalPlaces the number of decimal places to use for location resolution
     * @throws IllegalArgumentException when location argument is given without location variable
     */
    public NpcVariable(final FeatureApi featureApi, final Variable<NpcID> npcID, final Argument key,
                       @Nullable final LocationFormationMode formationMode, final int decimalPlaces) {
        this.featureApi = featureApi;
        this.npcID = npcID;
        this.key = key;
        this.formationMode = formationMode;
        if (key == LOCATION && formationMode == null) {
            throw new IllegalArgumentException("The location argument requires a location variable!");
        }
        this.decimalPlaces = decimalPlaces;
    }

    @Override
    public String getValue(@Nullable final Profile profile) throws QuestException {
        final Npc<?> npc = featureApi.getNpc(npcID.getValue(profile), profile);
        return key.resolve(npc, formationMode, decimalPlaces);
    }
}

package org.nakii.mmorpg.quest.compatibility.worldguard.npc;

import org.nakii.mmorpg.quest.api.feature.FeatureApi;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.nullable.NullableCondition;
import org.nakii.mmorpg.quest.api.quest.npc.Npc;
import org.nakii.mmorpg.quest.api.quest.npc.NpcID;
import org.nakii.mmorpg.quest.compatibility.worldguard.WorldGuardIntegrator;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Checks if a Npc is inside a WorldGuard region.
 */
public class NpcRegionCondition implements NullableCondition {

    /**
     * Quest Type API.
     */
    private final FeatureApi featureApi;

    /**
     * The Npc id.
     */
    private final Variable<NpcID> npcId;

    /**
     * The region name where the Npc should be.
     */
    private final Variable<String> region;

    /**
     * Create a new NpcRegionCondition.
     *
     * @param featureApi the Feature API
     * @param npcId      the npc id, null or positive
     * @param region     the name of the region where the NPC should be
     */
    public NpcRegionCondition(final FeatureApi featureApi, final Variable<NpcID> npcId, final Variable<String> region) {
        this.featureApi = featureApi;
        this.npcId = npcId;
        this.region = region;
    }

    @Override
    public boolean check(@Nullable final Profile profile) throws QuestException {
        final Npc<?> npc = featureApi.getNpc(npcId.getValue(profile), profile);
        if (!npc.isSpawned()) {
            return false;
        }
        final Optional<Location> location = npc.getLocation();
        return location.isPresent() && WorldGuardIntegrator.isInsideRegion(location.get(), region.getValue(profile));
    }
}

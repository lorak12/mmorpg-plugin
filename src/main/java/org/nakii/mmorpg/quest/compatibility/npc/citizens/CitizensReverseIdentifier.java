package org.nakii.mmorpg.quest.compatibility.npc.citizens;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.npc.Npc;
import org.nakii.mmorpg.quest.api.quest.npc.NpcID;
import org.nakii.mmorpg.quest.compatibility.npc.GenericReverseIdentifier;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Allows to get NpcIds for a Citizens NPC.
 */
public class CitizensReverseIdentifier extends GenericReverseIdentifier<NPC> {

    /**
     * Registry of NPC to identify.
     */
    private final NPCRegistry registry;

    /**
     * Create a new Identifier.
     *
     * @param registry the source registry for identifiable NPC
     */
    public CitizensReverseIdentifier(final NPCRegistry registry) {
        super("citizens", NPC.class, original -> String.valueOf(original.getId()),
                original -> original.getName() + " byName");
        this.registry = registry;
    }

    @Override
    public Set<NpcID> getIdsFromNpc(final Npc<?> npc, @Nullable final OnlineProfile profile) {
        if (npc.getOriginal() instanceof final NPC citizen && citizen.getOwningRegistry().equals(registry)) {
            return super.getIdsFromNpc(npc, profile);
        }
        return Set.of();
    }
}

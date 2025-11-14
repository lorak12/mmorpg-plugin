package org.nakii.mmorpg.quest.quest.objective.kill;

import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.api.CountingObjective;
import org.nakii.mmorpg.quest.api.MobKillNotifier.MobKilledEvent;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.nakii.mmorpg.quest.util.QuestMobType; // ---> ADD THIS IMPORT
import org.bukkit.entity.LivingEntity; // ---> ADD THIS IMPORT

import java.util.List;

/**
 * Player has to kill specified amount of specified mobs. It can also require
 * the player to kill specifically named mobs and notify them about the required
 * amount.
 */
public class MobKillObjective extends CountingObjective implements Listener {

    /**
     * The entity types that should be killed.
     */
    private final Variable<List<QuestMobType>> entities;

    /**
     * The optional name of the mob.
     */
    @Nullable
    protected Variable<String> name;

    /**
     * The optional marker for the mobs to identify them.
     */
    @Nullable
    protected Variable<String> marked;

    /**
     * Constructor for the MobKillObjective.
     *
     * @param instruction  the instruction that created this objective
     * @param targetAmount the amount of mobs to kill
     * @param entities     the entity types that should be killed
     * @param name         the optional name of the mob
     * @param marked       the optional marker for the mobs to identify them
     * @throws QuestException if there is an error in the instruction
     */
    public MobKillObjective(final Instruction instruction, final Variable<Number> targetAmount,
                            final Variable<List<QuestMobType>> entities, @Nullable final Variable<String> name,
                            @Nullable final Variable<String> marked) throws QuestException {
        super(instruction, targetAmount, "mobs_to_kill");
        this.entities = entities;
        this.name = name;
        this.marked = marked;
    }

    /**
     * Check if the player has killed the specified mob.
     *
     * @param event the event containing the mob kill information
     */
    @EventHandler(ignoreCancelled = true)
    public void onMobKill(final MobKilledEvent event) {
        final OnlineProfile onlineProfile = event.getProfile().getOnlineProfile().get();
        qeHandler.handle(() -> {

            // --- START OF FIX ---

            // The event gives a generic Entity, but we need a LivingEntity to check its type.
            if (!(event.getEntity() instanceof LivingEntity livingVictim)) {
                return; // If the killed entity wasn't a living creature, ignore it.
            }

            boolean typeMatch = entities.getValue(onlineProfile).stream()
                    .anyMatch(questMob -> questMob.matches(livingVictim)); // Use the casted variable

            if (!containsPlayer(onlineProfile)
                    || !typeMatch
                    || name != null && (livingVictim.getCustomName() == null
                    || !livingVictim.getCustomName().equals(name.getValue(onlineProfile)))) {
                return;
            }

            // --- END OF FIX ---

            if (marked != null) {
                final String value = marked.getValue(onlineProfile);
                final NamespacedKey key = new NamespacedKey(MMORPGCore.getInstance(), "betonquest-marked");
                final String dataContainerValue = livingVictim.getPersistentDataContainer().get(key, PersistentDataType.STRING); // Use livingVictim here too
                if (dataContainerValue == null || !dataContainerValue.equals(value)) {
                    return;
                }
            }

            if (checkConditions(onlineProfile)) {
                getCountingData(onlineProfile).progress();
                completeIfDoneOrNotify(onlineProfile);
            }
        });
    }
}

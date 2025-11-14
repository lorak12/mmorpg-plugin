package org.nakii.mmorpg.quest.quest.event.spawn;

import net.kyori.adventure.text.Component;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.MobManager;
import org.nakii.mmorpg.quest.QuestAPIBridge;
import org.nakii.mmorpg.quest.QuestModule;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.nullable.NullableEvent;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.nakii.mmorpg.quest.util.QuestMobType;

/**
 * Spawns mobs at given location, with given equipment and drops.
 */
public class SpawnMobEvent implements NullableEvent {

    /**
     * The location to spawn the mob at.
     */
    private final Variable<Location> variableLocation;

    /**
     * The type of mob to spawn.
     */
    private final Variable<QuestMobType> type;

    /**
     * The equipment and drops of the mob.
     */
    private final Equipment equipment;

    /**
     * The amount of mobs to spawn.
     */
    private final Variable<Number> amount;

    /**
     * The name of the mob.
     */
    @Nullable
    private final Variable<Component> name;

    /**
     * The marked variable.
     */
    @Nullable
    private final Variable<String> marked;

    /**
     * Creates a new spawn mob event.
     *
     * @param variableLocation the location to spawn the mob at
     * @param type             the type of mob to spawn
     * @param equipment        the equipment and drops of the mob
     * @param amount           the amount of entities to spawn
     * @param name             the name of the mob
     * @param marked           the marked variable
     */
    public SpawnMobEvent(final Variable<Location> variableLocation, final Variable<QuestMobType> type, final Equipment equipment,
                         final Variable<Number> amount, @Nullable final Variable<Component> name, @Nullable final Variable<String> marked) {
        this.variableLocation = variableLocation;
        this.type = type;
        this.equipment = equipment;
        this.amount = amount;
        this.name = name;
        this.marked = marked;
    }

    @Override
    public void execute(@Nullable final Profile profile) throws QuestException {
        final Location location = variableLocation.getValue(profile);
        final int numberOfMob = amount.getValue(profile).intValue();

        // ---> MODIFIED LOGIC: Handle both vanilla and custom spawning <---
        final QuestMobType mobType = type.getValue(profile);

        for (int i = 0; i < numberOfMob; i++) {
            Mob mob;
            if (mobType.isCustom()) {
                MobManager mobManager = QuestAPIBridge.getMobManager();
                mob = (Mob) mobManager.spawnMob(mobType.getCustomId(), location, null);
                if (mob == null) {
                    throw new QuestException("Failed to spawn custom mob with ID: " + mobType.getCustomId());
                }
            } else {
                mob = (Mob) location.getWorld().spawnEntity(location, mobType.getVanillaType());
            }

            // The rest of the logic remains the same
            this.equipment.addEquipment(profile, mob);
            this.equipment.addDrops(mob, profile);
            if (this.name != null) {
                mob.customName(this.name.getValue(profile));
            }
            if (this.marked != null) {
                final NamespacedKey key = new NamespacedKey(MMORPGCore.getInstance(), "betonquest-marked");
                mob.getPersistentDataContainer().set(key, PersistentDataType.STRING, this.marked.getValue(profile));
            }
        }
    }
}

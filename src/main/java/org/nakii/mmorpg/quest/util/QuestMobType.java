package org.nakii.mmorpg.quest.util;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.MobManager;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.quest.QuestException;

import java.util.Objects;

/**
 * A wrapper that represents either a vanilla EntityType or a custom MMORPGCore mob ID.
 */
public final class QuestMobType {

    @Nullable private final EntityType vanillaType;
    @Nullable private final String customId;

    private QuestMobType(@Nullable EntityType vanillaType, @Nullable String customId) {
        this.vanillaType = vanillaType;
        this.customId = customId;
    }

    public static QuestMobType fromVanilla(EntityType type) {
        return new QuestMobType(type, null);
    }

    public static QuestMobType fromCustom(String customId) {
        return new QuestMobType(null, customId.toUpperCase());
    }

    public boolean isCustom() {
        return customId != null;
    }

    @Nullable
    public EntityType getVanillaType() {
        return vanillaType;
    }

    @Nullable
    public String getCustomId() {
        return customId;
    }

    /**
     * Checks if a given LivingEntity matches this QuestMobType.
     * @param entity The entity to check.
     * @return True if it's a match, false otherwise.
     */
    public boolean matches(LivingEntity entity) {
        if (isCustom()) {
            String entityId = MMORPGCore.getInstance().getMobManager().getMobId(entity);
            return customId.equalsIgnoreCase(entityId);
        } else {
            return entity.getType() == vanillaType;
        }
    }

    @Override
    public String toString() {
        return isCustom() ? customId : (vanillaType != null ? vanillaType.name() : "INVALID");
    }

    // --- Argument Parser for BetonQuest ---

    public static class MobTypeArgument implements Argument<QuestMobType> {
        public static final MobTypeArgument MOB_TYPE = new MobTypeArgument();

        private final MobManager mobManager = MMORPGCore.getInstance().getMobManager();

        @Override
        public QuestMobType apply(String input) throws QuestException {
            String uppercaseInput = input.toUpperCase();

            // 1. Prioritize custom mobs. Check if an ID exists in the MobManager.
            if (mobManager.getTemplate(uppercaseInput) != null) {
                return QuestMobType.fromCustom(uppercaseInput);
            }

            // 2. If not a custom mob, try to parse it as a vanilla EntityType.
            try {
                EntityType vanillaType = EntityType.valueOf(uppercaseInput);
                return QuestMobType.fromVanilla(vanillaType);
            } catch (IllegalArgumentException e) {
                // 3. If both checks fail, the mob type is invalid.
                throw new QuestException("Unknown mob type or custom mob ID: '" + input + "'");
            }
        }
    }
}
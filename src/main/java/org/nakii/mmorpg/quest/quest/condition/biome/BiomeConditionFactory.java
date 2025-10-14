package org.nakii.mmorpg.quest.quest.condition.biome;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.quest.PrimaryServerThreadData;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerCondition;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerConditionFactory;
import org.nakii.mmorpg.quest.api.quest.condition.online.OnlineConditionAdapter;
import org.nakii.mmorpg.quest.api.quest.condition.thread.PrimaryServerThreadPlayerCondition;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class BiomeConditionFactory implements PlayerConditionFactory {

    private final BetonQuestLoggerFactory loggerFactory;
    private final PrimaryServerThreadData data;

    public BiomeConditionFactory(final BetonQuestLoggerFactory loggerFactory, final PrimaryServerThreadData data) {
        this.loggerFactory = loggerFactory;
        this.data = data;
    }

    @Override
    public PlayerCondition parsePlayer(final Instruction instruction) throws QuestException {
        final String biomeName = instruction.next();


        @Nullable
        final NamespacedKey key = NamespacedKey.fromString(biomeName.toLowerCase(Locale.ROOT));

        // <-- FIX: Use RegistryAccess to get the biome registry, then get the biome.
        final Registry<Biome> biomeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);
        final Biome biome = key != null ? biomeRegistry.get(key) : null;

        if (biome == null) {
            throw new QuestException("Invalid or unknown biome key: " + biomeName);
        }


        final BetonQuestLogger log = loggerFactory.create(BiomeCondition.class);
        return new PrimaryServerThreadPlayerCondition(
                new OnlineConditionAdapter(new BiomeCondition(biome), log, instruction.getPackage()), data
        );
    }
}
package org.nakii.mmorpg.quest.kernel.registry.feature;

import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.item.ItemRegistry;
import org.nakii.mmorpg.quest.item.QuestItem;
import org.nakii.mmorpg.quest.item.QuestItemSerializer;
import org.nakii.mmorpg.quest.item.QuestItemWrapper;
import org.nakii.mmorpg.quest.kernel.registry.FactoryTypeRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry for {@link QuestItem} types.
 */
public class ItemTypeRegistry extends FactoryTypeRegistry<QuestItemWrapper> implements ItemRegistry {
    /**
     * Identifies registered serializer by string.
     */
    private final Map<String, QuestItemSerializer> serializers;

    /**
     * Create a new Item registry.
     *
     * @param log the logger that will be used for logging
     */
    public ItemTypeRegistry(final BetonQuestLogger log) {
        super(log, "items");
        serializers = new HashMap<>();
    }

    @Override
    public void registerSerializer(final String name, final QuestItemSerializer serializer) {
        log.debug("Registering item serializer for '" + name + "' type");
        serializers.put(name, serializer);
    }

    @Override
    public QuestItemSerializer getSerializer(final String name) throws QuestException {
        final QuestItemSerializer serializer = serializers.get(name);
        if (serializer == null) {
            throw new QuestException("No serializer for '" + name + "' type");
        }
        return serializer;
    }

    @Override
    public Set<String> serializerKeySet() {
        return serializers.keySet();
    }
}

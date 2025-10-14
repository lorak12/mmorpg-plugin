package org.nakii.mmorpg.quest.kernel.processor.feature;

import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackageManager;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.id.ItemID;
import org.nakii.mmorpg.quest.item.QuestItem;
import org.nakii.mmorpg.quest.item.QuestItemWrapper;
import org.nakii.mmorpg.quest.kernel.processor.TypedQuestProcessor;
import org.nakii.mmorpg.quest.kernel.registry.feature.ItemTypeRegistry;

/**
 * Stores QuestItems and generates new.
 */
public class ItemProcessor extends TypedQuestProcessor<ItemID, QuestItemWrapper> {
    /**
     * Create a new ItemProcessor to store and get {@link QuestItem}s.
     *
     * @param log         the custom logger for this class
     * @param packManager the quest package manager to get quest packages from
     * @param types       the available types
     */
    public ItemProcessor(final BetonQuestLogger log, final QuestPackageManager packManager, final ItemTypeRegistry types) {
        super(log, packManager, types, "Item", "items");
    }

    @Override
    protected ItemID getIdentifier(final QuestPackage pack, final String identifier) throws QuestException {
        return new ItemID(packManager, pack, identifier);
    }
}

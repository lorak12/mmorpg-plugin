package org.nakii.mmorpg.quest.item;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.item.CustomItemTemplate;
import org.nakii.mmorpg.managers.ItemLoreGenerator;
import org.nakii.mmorpg.managers.ItemManager;
import org.nakii.mmorpg.quest.api.profile.Profile;

import java.util.List;
import java.util.Objects;

public class MmorpgQuestItem implements QuestItem {

    private final String mmorpgItemId;
    private final ItemManager itemManager;
    private final ItemLoreGenerator itemLoreGenerator;

    public MmorpgQuestItem(String mmorpgItemId, ItemManager itemManager, ItemLoreGenerator itemLoreGenerator) {
        this.mmorpgItemId = mmorpgItemId.toUpperCase();
        this.itemManager = itemManager;
        this.itemLoreGenerator = itemLoreGenerator;
    }

    @Override
    public ItemStack generate(int stackSize, @Nullable Profile profile) {

        // 1. Create the base item using your existing system
        ItemStack item = itemManager.createItemStack(mmorpgItemId);
        if (item == null) {
            return null; // Item ID was invalid
        }

        // 2. Generate the visual lore
        itemLoreGenerator.updateLore(item, null);

        // 3. Set the requested amount
        item.setAmount(stackSize);

        // 4. IMPORTANT: We DO NOT mark it as a quest item.
        // This ensures it behaves like a normal MMORPG item (can be dropped, etc.)
        // No call to Utils.setQuestItem(item);

        return item;
    }

    @Override
    public boolean matches(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        String itemIdFromStack = itemManager.getItemId(item);
        return mmorpgItemId.equalsIgnoreCase(itemIdFromStack);
    }

    @Override
    public Component getName() {
        CustomItemTemplate template = itemManager.getTemplate(mmorpgItemId);
        if (template != null) {
            return MMORPGCore.getInstance().getMiniMessage().deserialize(template.getDisplayName());
        }
        return Component.text(mmorpgItemId);
    }

    @Override
    public List<Component> getLore() {
        return List.of();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MmorpgQuestItem that = (MmorpgQuestItem) o;
        return mmorpgItemId.equals(that.mmorpgItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mmorpgItemId);
    }
}
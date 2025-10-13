package org.nakii.mmorpg.quest.hider;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import org.jetbrains.annotations.Nullable;

public final class CitizensHider {

    @Nullable
    private static CitizensHider instance;

    private final EntityHider hider;

    private CitizensHider(Plugin plugin) {
        hider = new EntityHider(plugin);
    }

    public static void start(Plugin plugin) {
        if (instance != null) instance.stop();
        instance = new CitizensHider(plugin);
    }

    @Nullable
    public static CitizensHider getInstance() {
        return instance;
    }

    public void stop() {
        if (instance != null) hider.close();
    }

    public void hide(Player player, NPC npc) {
        hider.hideNpc(player, npc);
    }

    public void show(Player player, NPC npc) {
        hider.showNpc(player, npc);
    }

    public boolean isInvisible(Player player, NPC npc) {
        // We must check using the entity's actual ID, not the Citizens ID.
        // We also check if the entity itself is valid.
        if (npc == null || npc.getEntity() == null) {
            return false;
        }
        return hider.isHidden(player, npc.getEntity().getEntityId());
    }
}

package org.nakii.mmorpg.quest.hider;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.HologramTrait;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class EntityHider implements Listener {

    private final Plugin plugin;
    private final ProtocolManager protocolManager;
    private final Map<UUID, Set<Integer>> hiddenEntities = new HashMap<>();

    private static final PacketType[] ENTITY_PACKETS = new PacketType[]{
            PacketType.Play.Server.SPAWN_ENTITY,
            PacketType.Play.Server.SPAWN_ENTITY_LIVING,
            PacketType.Play.Server.ENTITY_EQUIPMENT,
            PacketType.Play.Server.ANIMATION,
            PacketType.Play.Server.ENTITY_VELOCITY,
            PacketType.Play.Server.REL_ENTITY_MOVE,
            PacketType.Play.Server.ENTITY_LOOK,
            PacketType.Play.Server.REL_ENTITY_MOVE_LOOK,
            PacketType.Play.Server.ENTITY_TELEPORT,
            PacketType.Play.Server.ENTITY_HEAD_ROTATION,
            PacketType.Play.Server.ENTITY_STATUS,
            PacketType.Play.Server.ENTITY_METADATA,
            PacketType.Play.Server.ENTITY_EFFECT,
            PacketType.Play.Server.REMOVE_ENTITY_EFFECT,
            PacketType.Play.Server.ATTACH_ENTITY,
            PacketType.Play.Server.COLLECT,
            PacketType.Play.Server.PLAYER_COMBAT_KILL,
            PacketType.Play.Server.BLOCK_BREAK_ANIMATION
    };

    public EntityHider(Plugin plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        protocolManager.addPacketListener(new PacketAdapter(plugin, ENTITY_PACKETS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player player = event.getPlayer();
                Integer entityId = getEntityIdFromPacket(event.getPacket());
                if (entityId != null && isHidden(player, entityId)) {
                    event.setCancelled(true);
                }
            }
        });
    }

    private Integer getEntityIdFromPacket(PacketContainer packet) {
        try {
            if (packet.getType().equals(PacketType.Play.Server.PLAYER_COMBAT_KILL)) {
                return packet.getIntegers().read(1);
            }
            return packet.getIntegers().read(0);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isHidden(Player player, int entityId) {
        return hiddenEntities.getOrDefault(player.getUniqueId(), Collections.emptySet()).contains(entityId);
    }

    public void hideNpc(Player player, NPC npc) {
        for (Entity entity : getNpcEntities(npc)) {
            hiddenEntities.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(entity.getEntityId());
            PacketContainer destroy = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            destroy.getIntLists().write(0, Collections.singletonList(entity.getEntityId()));
            try {
                protocolManager.sendServerPacket(player, destroy);
            } catch (Exception ignored) {}
        }
    }

    public void showNpc(Player player, NPC npc) {
        for (Entity entity : getNpcEntities(npc)) {
            Set<Integer> set = hiddenEntities.get(player.getUniqueId());
            if (set != null) set.remove(entity.getEntityId());
            try {
                protocolManager.updateEntity(entity, Collections.singletonList(player));
            } catch (Exception ignored) {}
        }
    }

    private List<Entity> getNpcEntities(NPC npc) {
        List<Entity> entities = new ArrayList<>();
        entities.add(npc.getEntity());

        HologramTrait trait = npc.getTraitNullable(HologramTrait.class);
        if (trait != null) {
            Entity nameEntity = trait.getNameEntity();
            if (nameEntity != null) entities.add(nameEntity);
            entities.addAll(trait.getHologramEntities());
        }

        return entities;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        hiddenEntities.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        removeEntity(event.getEntity().getEntityId());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity e : event.getChunk().getEntities()) removeEntity(e.getEntityId());
    }

    private void removeEntity(int entityId) {
        for (Set<Integer> set : hiddenEntities.values()) set.remove(entityId);
    }

    public void close() {
        HandlerList.unregisterAll(this);
    }
}
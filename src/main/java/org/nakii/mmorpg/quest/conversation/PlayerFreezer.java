package org.nakii.mmorpg.quest.conversation;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages freezing a player in place for a conversation by mounting them on an invisible ArmorStand.
 */
public class PlayerFreezer {

    private final Map<UUID, ArmorStand> freezerEntities = new HashMap<>();
    private final Plugin plugin; // Keep a reference to the plugin for logging

    public PlayerFreezer() {
        this.plugin = org.nakii.mmorpg.MMORPGCore.getInstance();
    }

    /**
     * Freezes a player by sending a packet to mount them on an invisible ArmorStand.
     * @param player The player to freeze.
     */
    public void freeze(Player player) {
        Location freezerLoc = player.getLocation().clone().subtract(0, 0.5, 0);

        ArmorStand freezer = player.getWorld().spawn(freezerLoc, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setSmall(true);
            stand.setMarker(false);
            stand.setInvulnerable(true);
        });

        freezerEntities.put(player.getUniqueId(), freezer);

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer mountPacket = protocolManager.createPacket(PacketType.Play.Server.MOUNT);

        // The first integer in the packet is the entity being ridden (the vehicle)
        mountPacket.getIntegers().write(0, freezer.getEntityId());

        // The integer array is the list of passengers
        mountPacket.getIntegerArrays().write(0, new int[]{player.getEntityId()});

        try {
            protocolManager.sendServerPacket(player, mountPacket);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to send mount packet for conversation!");
            e.printStackTrace();
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, PotionEffect.INFINITE_DURATION, 0, false, false));
    }

    /**
     * Unfreezes a player by removing the ArmorStand they are mounted on.
     * @param player The player to unfreeze.
     */
    public void unfreeze(Player player) {
        ArmorStand freezer = freezerEntities.remove(player.getUniqueId());
        if (freezer != null) {
            player.setVelocity(new Vector(0, 0.2, 0));
            freezer.remove();
        }
        player.removePotionEffect(PotionEffectType.BLINDNESS);
    }
}
package org.nakii.mmorpg.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.conversation.ConversationInput;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ConversationInputListener implements Listener {

    private final MMORPGCore plugin;

    // A map to prevent players from scrolling through options too quickly.
    private final Map<UUID, Long> lastInputTime = new ConcurrentHashMap<>();
    private static final long INPUT_COOLDOWN_MS = 200; // 200ms cooldown between up/down inputs.
    private static final float MOVE_THRESHOLD = 0.1f; // Minimum value to register as movement.

    public ConversationInputListener(MMORPGCore plugin) {
        this.plugin = plugin;
        registerPacketListener();
    }

    private void registerPacketListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, PacketType.Play.Client.STEER_VEHICLE) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        MMORPGCore core = MMORPGCore.getInstance();
                        UUID playerUUID = event.getPlayer().getUniqueId();

                        if (!core.getConversationManager().isInConversation(event.getPlayer())) {
                            return;
                        }

                        // --- Read all relevant data from the packet ---
                        boolean isJumping = event.getPacket().getBooleans().read(0);
                        float forward = event.getPacket().getFloat().read(1);

                        // --- Handle SELECT (Spacebar) Input ---
                        if (isJumping) {
                            // Run on the next server tick to avoid threading issues
                            core.getServer().getScheduler().runTask(core, () ->
                                    core.getConversationManager().handleInput(event.getPlayer(), ConversationInput.SELECT));
                            return; // Selection takes priority
                        }

                        // --- Handle UP/DOWN (W/S) Input with Cooldown ---
                        long now = System.currentTimeMillis();
                        if (now - lastInputTime.getOrDefault(playerUUID, 0L) < INPUT_COOLDOWN_MS) {
                            return; // Input is on cooldown
                        }

                        ConversationInput direction = null;
                        if (forward > MOVE_THRESHOLD) {        // Player pressed 'W' (Forward)
                            direction = ConversationInput.UP;
                        } else if (forward < -MOVE_THRESHOLD) { // Player pressed 'S' (Backward)
                            direction = ConversationInput.DOWN;
                        }

                        if (direction != null) {
                            lastInputTime.put(playerUUID, now); // Update cooldown timer
                            final ConversationInput finalDirection = direction;
                            core.getServer().getScheduler().runTask(core, () ->
                                    core.getConversationManager().handleInput(event.getPlayer(), finalDirection));
                        }
                    }
                }
        );
    }

    // This event handler is now obsolete and has been removed.

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up data to prevent memory leaks
        lastInputTime.remove(event.getPlayer().getUniqueId());

        // Forcefully end conversation if player logs out
        if (plugin.getConversationManager().isInConversation(event.getPlayer())) {
            plugin.getConversationManager().endConversation(event.getPlayer());
        }
    }
}
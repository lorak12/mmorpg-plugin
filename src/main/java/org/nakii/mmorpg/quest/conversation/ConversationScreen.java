package org.nakii.mmorpg.quest.conversation;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;

import java.util.ArrayList;
import java.util.List;

public class ConversationScreen {

    private static final String PASS_TAG = "§M§M§O§R§P§G";
    private final Player player;
    private final MMORPGCore plugin;
    private final List<PacketEvent> bufferedPackets = new ArrayList<>();
    private final PacketAdapter packetAdapter;

    public ConversationScreen(MMORPGCore plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        packetAdapter = new PacketAdapter(plugin, PacketType.Play.Server.SYSTEM_CHAT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if(!event.getPlayer().equals(player)) return;

                String json = event.getPacket().getChatComponents().read(0).getJson();
                if(json.contains(PASS_TAG)) {
                    String finalJson = json.replace(PASS_TAG, "");
                    event.getPacket().getChatComponents().write(0, WrappedChatComponent.fromJson(finalJson));
                    return;
                }
                event.setCancelled(true);
                bufferedPackets.add(event);
            }
        };
        ProtocolLibrary.getProtocolManager().addPacketListener(packetAdapter);
    }

    public void display(LineView view) {
        // "Clear" the screen by sending many blank lines that bypass our interceptor
        for(int i = 0; i < 30; i++) {
            player.sendMessage(PASS_TAG);
        }

        // Display the actual conversation content
        for(LineView.Line line : view.getLines()) {
            player.sendMessage(Component.text(PASS_TAG).append(line.getComponent()));
        }
    }

    public void end() {
        ProtocolLibrary.getProtocolManager().removePacketListener(packetAdapter);
        for(PacketEvent e : bufferedPackets)
            ProtocolLibrary.getProtocolManager().sendServerPacket(e.getPlayer(), e.getPacket());
    }
}

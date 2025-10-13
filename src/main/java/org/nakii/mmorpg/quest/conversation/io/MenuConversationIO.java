package org.nakii.mmorpg.quest.conversation.io;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.conversation.*;
import org.nakii.mmorpg.util.ChatUtils;

import java.util.ArrayList;
import java.util.List;

public class MenuConversationIO implements Listener {

    private final MMORPGCore plugin;
    private final Conversation conversation; // The engine controlling this UI
    private final Player player;

    private final ConversationScreen screen;
    private final PlayerFreezer freezer;
    private PacketAdapter packetAdapter;

    private long lastInputTime = 0;
    private static final long INPUT_COOLDOWN_MS = 150;

    // We now store the current NPC text to redraw the screen on scroll
    private String currentNpcText = "";

    public MenuConversationIO(MMORPGCore plugin, Conversation conversation) {
        this.plugin = plugin;
        this.conversation = conversation;
        this.player = conversation.getPlayer();
        this.screen = new ConversationScreen(plugin, player);
        this.freezer = new PlayerFreezer();
    }

    public void start() {
        freezer.freeze(player);
        registerListeners();
    }

    public void end() {
        unregisterListeners();
        screen.end();
        freezer.unfreeze(player);
    }

    public void redrawScreen(String npcName, String npcText, List<PlayerOption> options, int selection) {
        this.currentNpcText = npcText; // Store current text
        Component displayComponent = buildDisplay(npcName, npcText, options, selection);
        screen.display(new LineView.Holder(new LineView.Line(displayComponent)));
    }

    public void displayAndEnd(String npcName, String npcText) {
        Component displayComponent = buildDisplay(npcName, npcText, new ArrayList<>(), 0);
        screen.display(new LineView.Holder(new LineView.Line(displayComponent)));
        Bukkit.getScheduler().runTaskLater(plugin, () -> conversation.end(false), 40L);
    }

    private Component buildDisplay(String npcName, String npcText, List<PlayerOption> options, int selection) {
        List<Component> lines = new ArrayList<>();
        lines.add(ChatUtils.format("<gold>" + npcName + ":</gold> " + npcText));
        lines.add(Component.empty());

        for (int i = 0; i < options.size(); i++) {
            PlayerOption option = options.get(i);
            String prefix = (i == selection) ? "<yellow><b>> </b>" : "<gray>  ";
            lines.add(ChatUtils.format(prefix + option.text()));
        }
        lines.add(Component.empty());
        return Component.join(JoinConfiguration.newlines(), lines);
    }

    private void handleInput(Scroll scroll) {
        if (isOffCooldown()) {
            conversation.handlePlayerInput(scroll); // Report to engine
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.5f);
        }
    }

    private void handleSelect() {
        if (isOffCooldown()) {
            conversation.passPlayerAnswer(); // Report to engine
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);
        }
    }

    // --- Unchanged Listeners and Helper Methods from here down ---

    private boolean isOffCooldown() {
        long now = System.currentTimeMillis();
        if (now - lastInputTime < INPUT_COOLDOWN_MS) {
            return false;
        }
        lastInputTime = now;
        return true;
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        packetAdapter = new PacketAdapter(plugin, PacketType.Play.Client.STEER_VEHICLE) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (!event.getPlayer().equals(player)) return;

                var booleans = event.getPacket().getStructures().read(0).getBooleans();
                boolean key_w = booleans.read(0);
                boolean key_s = booleans.read(1);
                boolean key_jump = booleans.read(4);
                boolean key_unmount = booleans.read(5);

                if (key_unmount) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> conversation.end(true));
                    return;
                }
                if (key_jump) {
                    plugin.getServer().getScheduler().runTask(plugin, MenuConversationIO.this::handleSelect);
                    return;
                }
                if (key_w) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> handleInput(Scroll.UP));
                } else if (key_s) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> handleInput(Scroll.DOWN));
                }
            }
        };
        ProtocolLibrary.getProtocolManager().addPacketListener(packetAdapter);
    }

    private void unregisterListeners() {
        PlayerInteractEvent.getHandlerList().unregister(this);
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
        PlayerItemHeldEvent.getHandlerList().unregister(this);
        if (packetAdapter != null) {
            ProtocolLibrary.getProtocolManager().removePacketListener(packetAdapter);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getPlayer().equals(player)) {
            event.setCancelled(true);
            Action action = event.getAction();
            if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                handleSelect();
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager().equals(player)) {
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                event.setCancelled(true);
                handleSelect();
            }
        }
    }

    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent event) {
        if (event.getPlayer().equals(player)) {
            event.setCancelled(true);
            int oldSlot = event.getPreviousSlot();
            int newSlot = event.getNewSlot();
            if ((oldSlot + 1) % 9 == newSlot) {
                handleInput(Scroll.DOWN);
            } else if ((oldSlot - 1 + 9) % 9 == newSlot) {
                handleInput(Scroll.UP);
            }
        }
    }
}
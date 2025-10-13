package org.nakii.mmorpg.quest.hologram;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.nakii.mmorpg.util.ChatUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * An implementation of IMmoHologram using the DecentHolograms API.
 */
public class DecentMmoHologram implements IMmoHologram {

    private Hologram hologram;
    private final String name;

    public DecentMmoHologram() {
        this.name = "mmorpg-" + UUID.randomUUID();
    }

    @Override
    public void spawn(Location location, List<String> lines) {
        if (isSpawned()) return;

        List<String> formattedLines = lines.stream()
                .map(line -> {
                    Component component = ChatUtils.format(line);
                    return LegacyComponentSerializer.legacySection().serialize(component);
                })
                .collect(Collectors.toList());

        this.hologram = DHAPI.createHologram(name, location, formattedLines);
        // --- NEW --- Set the default state to NOT visible. We will control it per-player.
        hologram.setDefaultVisibleState(false);
    }

    @Override
    public void show(Player player) {
        if (isSpawned() && !hologram.isVisible(player)) {
            // --- UPDATED LOGIC ---
            // Explicitly manage the per-player visibility lists for robustness.
            hologram.removeHidePlayer(player);
            hologram.setShowPlayer(player);
        }
    }

    @Override
    public void hide(Player player) {
        if (isSpawned() && hologram.isVisible(player)) {
            // --- UPDATED LOGIC ---
            hologram.removeShowPlayer(player);
            hologram.setHidePlayer(player);
        }
    }

    @Override
    public void move(Location location) {
        if (isSpawned()) {
            DHAPI.moveHologram(hologram, location);
        }
    }

    @Override
    public void delete() {
        if (isSpawned()) {
            hologram.delete();
            hologram = null;
        }
    }

    @Override
    public boolean isVisibleTo(Player player) {
        // We now check our own state, which is managed by setShowPlayer/setHidePlayer
        return isSpawned() && hologram.getShowPlayers().contains(player);
    }

    @Override
    public boolean isSpawned() {
        return this.hologram != null;
    }
}
package org.nakii.mmorpg.quest.engine.profile;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class MmoProfile implements Profile {
    private final OfflinePlayer player;

    public MmoProfile(OfflinePlayer player) {
        this.player = player;
    }

    @Override
    public OfflinePlayer getPlayer() {
        return player;
    }

    @Override
    public UUID getPlayerUUID() {
        return player.getUniqueId();
    }

    @Override
    public String getProfileName() {
        return player.getName();
    }

    @Override
    public Optional<OnlineProfile> getOnlineProfile() {
        if (player.isOnline()) {
            return Optional.of(new MmoOnlineProfile(player.getPlayer()));
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return getProfileName();
    }
}
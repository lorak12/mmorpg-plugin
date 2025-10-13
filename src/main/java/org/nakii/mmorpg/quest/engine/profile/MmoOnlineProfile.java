package org.nakii.mmorpg.quest.engine.profile;

import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class MmoOnlineProfile implements OnlineProfile {
    private final Player player;

    public MmoOnlineProfile(Player player) {
        this.player = player;
    }

    @Override
    public Player getPlayer() {
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
        return Optional.of(this);
    }

    @Override
    public String toString() {
        return getProfileName();
    }
}
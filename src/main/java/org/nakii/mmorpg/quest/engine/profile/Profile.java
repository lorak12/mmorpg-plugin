package org.nakii.mmorpg.quest.engine.profile;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public interface Profile {
    OfflinePlayer getPlayer();
    UUID getPlayerUUID();
    String getProfileName();
    Optional<OnlineProfile> getOnlineProfile();
}
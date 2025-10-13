package org.nakii.mmorpg.quest.engine.profile;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface ProfileProvider {
    Profile getProfile(OfflinePlayer offlinePlayer);
    OnlineProfile getProfile(Player player);
}
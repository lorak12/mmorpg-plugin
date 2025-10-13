package org.nakii.mmorpg.quest.engine.profile;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class ProfileManager implements ProfileProvider {
    @Override
    public Profile getProfile(OfflinePlayer offlinePlayer) {
        return new MmoProfile(offlinePlayer);
    }

    @Override
    public OnlineProfile getProfile(Player player) {
        return new MmoOnlineProfile(player);
    }
}
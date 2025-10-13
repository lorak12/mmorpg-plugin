package org.nakii.mmorpg.quest.engine.profile;

import org.bukkit.entity.Player;

public interface OnlineProfile extends Profile {
    @Override
    Player getPlayer();
}
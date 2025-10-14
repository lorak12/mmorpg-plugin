package org.nakii.mmorpg.quest.notify.io;

import net.kyori.adventure.text.Component;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.notify.NotifyIO;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Plays a sound.
 */
public class SoundIO extends NotifyIO {

    /**
     * Create a new Sound IO.
     *
     * @param pack the source pack to resolve variables
     * @param data the customization data for sound
     * @throws QuestException when data could not be parsed
     */
    public SoundIO(@Nullable final QuestPackage pack, final Map<String, String> data) throws QuestException {
        super(pack, data);
    }

    @Override
    protected void notifyPlayer(final Component message, final OnlineProfile onlineProfile) {
        // Empty
    }
}

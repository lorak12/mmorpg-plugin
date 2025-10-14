package org.nakii.mmorpg.quest.notify.io;

import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.notify.NotifyIO;
import org.nakii.mmorpg.quest.notify.NotifyIOFactory;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Factory to create {@link SoundIO}s.
 */
public class SoundIOFactory implements NotifyIOFactory {

    /**
     * Empty default constructor.
     */
    public SoundIOFactory() {
    }

    @Override
    public NotifyIO create(@Nullable final QuestPackage pack, final Map<String, String> categoryData) throws QuestException {
        return new SoundIO(pack, categoryData);
    }
}

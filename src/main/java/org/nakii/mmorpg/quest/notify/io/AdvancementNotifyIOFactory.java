package org.nakii.mmorpg.quest.notify.io;

import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.notify.NotifyIO;
import org.nakii.mmorpg.quest.notify.NotifyIOFactory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Factory to create {@link AdvancementNotifyIO}s.
 */
public class AdvancementNotifyIOFactory implements NotifyIOFactory {
    /**
     * Plugin to start tasks.
     */
    private final Plugin plugin;

    /**
     * Create a new factory.
     *
     * @param plugin the plugin to start tasks
     */
    public AdvancementNotifyIOFactory(final Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public NotifyIO create(@Nullable final QuestPackage pack, final Map<String, String> categoryData) throws QuestException {
        return new AdvancementNotifyIO(pack, categoryData, plugin);
    }
}

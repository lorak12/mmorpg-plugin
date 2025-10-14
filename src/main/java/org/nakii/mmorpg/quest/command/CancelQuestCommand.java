package org.nakii.mmorpg.quest.command;

import org.nakii.mmorpg.quest.api.config.ConfigAccessor;
import org.nakii.mmorpg.quest.api.profile.ProfileProvider;
import org.nakii.mmorpg.quest.config.PluginMessage;
import org.nakii.mmorpg.quest.feature.Backpack;
import org.nakii.mmorpg.quest.feature.Backpack.DisplayType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * The /cancelquest command. It opens the list of quests.
 */
@SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
public class CancelQuestCommand implements CommandExecutor {

    /**
     * The plugin configuration file.
     */
    private final ConfigAccessor config;

    /**
     * The {@link PluginMessage} instance.
     */
    private final PluginMessage pluginMessage;

    /**
     * The profile provider instance.
     */
    private final ProfileProvider profileProvider;

    /**
     * Creates a new executor for the /cancelquest command.
     *
     * @param config          the plugin configuration file
     * @param pluginMessage   the {@link PluginMessage} instance
     * @param profileProvider the profile provider instance
     */
    public CancelQuestCommand(final ConfigAccessor config, final PluginMessage pluginMessage, final ProfileProvider profileProvider) {
        this.config = config;
        this.pluginMessage = pluginMessage;
        this.profileProvider = profileProvider;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if ("cancelquest".equalsIgnoreCase(cmd.getName())) {
            if (sender instanceof Player) {
                new Backpack(config, pluginMessage, profileProvider.getProfile((Player) sender), DisplayType.CANCEL);
            }
            return true;
        }
        return false;
    }
}

package org.nakii.mmorpg.quest.notify.io;

import net.kyori.adventure.text.Component;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.conversation.Conversation;
import org.nakii.mmorpg.quest.notify.NotifyIO;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Displays the message in the chat.
 */
public class ChatNotifyIO extends NotifyIO {

    /**
     * Create a new Chat Notify IO.
     *
     * @param pack the source pack to resolve variables
     * @param data the customization data for notifications
     * @throws QuestException when data could not be parsed
     */
    public ChatNotifyIO(@Nullable final QuestPackage pack, final Map<String, String> data) throws QuestException {
        super(pack, data);
    }

    @Override
    protected void notifyPlayer(final Component message, final OnlineProfile onlineProfile) {
        final Conversation conversation = Conversation.getConversation(onlineProfile);
        if (conversation == null || conversation.getInterceptor() == null) {
            onlineProfile.getPlayer().sendMessage(message);
        } else {
            conversation.getInterceptor().sendMessage(message);
        }
    }
}

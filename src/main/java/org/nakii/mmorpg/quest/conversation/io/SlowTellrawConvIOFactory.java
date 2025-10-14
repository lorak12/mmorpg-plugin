package org.nakii.mmorpg.quest.conversation.io;

import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.QuestModule;
import org.nakii.mmorpg.quest.api.common.component.FixedComponentLineWrapper;
import org.nakii.mmorpg.quest.api.common.component.font.FontRegistry;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.conversation.Conversation;
import org.nakii.mmorpg.quest.conversation.ConversationColors;
import org.nakii.mmorpg.quest.conversation.ConversationIO;
import org.nakii.mmorpg.quest.conversation.ConversationIOFactory;

/**
 * SlowTellraw conversation output.
 */
public class SlowTellrawConvIOFactory implements ConversationIOFactory {
    /**
     * The font registry to use in APIs that work with {@link net.kyori.adventure.text.Component}.
     */
    private final FontRegistry fontRegistry;

    /**
     * The colors used for the conversation.
     */
    private final ConversationColors colors;

    /**
     * Create a new SlowTellraw conversation IO factory.
     *
     * @param fontRegistry The font registry used for the conversation.
     * @param colors       The colors used for the conversation.
     */
    public SlowTellrawConvIOFactory(final FontRegistry fontRegistry, final ConversationColors colors) {
        this.fontRegistry = fontRegistry;
        this.colors = colors;
    }

    @Override
    public ConversationIO parse(final Conversation conversation, final OnlineProfile onlineProfile) {
        final FixedComponentLineWrapper componentLineWrapper = new FixedComponentLineWrapper(fontRegistry, 320);
        int messageDelay = MMORPGCore.getInstance().getQuestModule().getPluginConfig().getInt("conversation.io.slowtellraw.message_delay", 10);
        if (messageDelay <= 0) {
            MMORPGCore.getInstance().getLogger().warning("Invalid message delay of " + messageDelay + " for SlowTellraw Conversation IO, using default value of 10 ticks");
            messageDelay = 10;
        }
        return new SlowTellrawConvIO(conversation, onlineProfile, messageDelay, componentLineWrapper, colors);
    }
}

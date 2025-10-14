package org.nakii.mmorpg.quest.api.feature;

import org.nakii.mmorpg.quest.api.kernel.FeatureRegistry;
import org.nakii.mmorpg.quest.api.quest.npc.NpcRegistry;
import org.nakii.mmorpg.quest.api.text.TextParserRegistry;
import org.nakii.mmorpg.quest.conversation.ConversationIOFactory;
import org.nakii.mmorpg.quest.conversation.interceptor.InterceptorFactory;
import org.nakii.mmorpg.quest.item.ItemRegistry;
import org.nakii.mmorpg.quest.notify.NotifyIOFactory;
import org.nakii.mmorpg.quest.schedule.EventScheduling;

/**
 * Provides the BetonQuest Feature Registries.
 * <p>
 * They are used to add new implementations and access them.
 */
public interface FeatureRegistries {
    /**
     * Gets the registry for conversation IOs.
     *
     * @return the conversation io registry
     */
    FeatureRegistry<ConversationIOFactory> conversationIO();

    /**
     * Gets the registry for quest items.
     *
     * @return the quest item registry
     */
    ItemRegistry item();

    /**
     * Gets the registry for chat interceptor.
     *
     * @return the interceptor registry
     */
    FeatureRegistry<InterceptorFactory> interceptor();

    /**
     * Gets the registry for text parser.
     *
     * @return the text parser registry
     */
    TextParserRegistry textParser();

    /**
     * Gets the registry for npc types.
     *
     * @return the npc registry
     */
    NpcRegistry npc();

    /**
     * Gets the registry for notify IOs.
     *
     * @return the notify io registry
     */
    FeatureRegistry<NotifyIOFactory> notifyIO();

    /**
     * Gets the registry for event scheduling types.
     *
     * @return the scheduling registry
     */
    FeatureRegistry<EventScheduling.ScheduleType<?, ?>> eventScheduling();
}

package org.nakii.mmorpg.quest.compatibility.protocollib.conversation;

import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.conversation.interceptor.Interceptor;
import org.nakii.mmorpg.quest.conversation.interceptor.InterceptorFactory;

/**
 * Factory to create a new {@link PacketInterceptor}.
 */
public class PacketInterceptorFactory implements InterceptorFactory {

    /**
     * The empty default constructor.
     */
    public PacketInterceptorFactory() {
    }

    @Override
    public Interceptor create(final OnlineProfile profile) {
        return new PacketInterceptor(profile);
    }
}

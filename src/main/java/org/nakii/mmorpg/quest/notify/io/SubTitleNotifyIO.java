package org.nakii.mmorpg.quest.notify.io;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.notify.NotifyIO;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Map;

/**
 * Displays the message as subtitle.
 */
public class SubTitleNotifyIO extends NotifyIO {

    /**
     * Time in ticks the title fades in.
     */
    private final Variable<Number> variableFadeIn;

    /**
     * Time in ticks the title stays.
     */
    private final Variable<Number> variableStay;

    /**
     * Time in ticks the title fades out.
     */
    private final Variable<Number> variableFadeOut;

    /**
     * Create a new Sub Title Notify IO.
     *
     * @param pack the source pack to resolve variables
     * @param data the customization data for notifications
     * @throws QuestException when the data could not be parsed
     */
    public SubTitleNotifyIO(@Nullable final QuestPackage pack, final Map<String, String> data) throws QuestException {
        super(pack, data);

        variableFadeIn = getNumberData("fadein", 10);
        variableStay = getNumberData("stay", 70);
        variableFadeOut = getNumberData("fadeout", 20);
    }

    @Override
    protected void notifyPlayer(final Component message, final OnlineProfile onlineProfile) throws QuestException {
        final Duration fadeIn = Duration.ofMillis(variableFadeIn.getValue(onlineProfile).longValue() * 50L);
        final Duration stay = Duration.ofMillis(variableStay.getValue(onlineProfile).longValue() * 50L);
        final Duration fadeOut = Duration.ofMillis(variableFadeOut.getValue(onlineProfile).longValue() * 50L);

        final Title title = Title.title(Component.empty(), message, Title.Times.times(fadeIn, stay, fadeOut));
        onlineProfile.getPlayer().showTitle(title);
    }
}

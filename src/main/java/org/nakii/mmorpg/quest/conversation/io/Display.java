package org.nakii.mmorpg.quest.conversation.io;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.nakii.mmorpg.util.ChatUtils;
import org.nakii.mmorpg.quest.conversation.LineView;
import org.nakii.mmorpg.quest.conversation.PlayerOption;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Display {

    private final String npcName;
    private final String npcText;
    private final List<PlayerOption> options;

    private final Cursor selectionCursor;
    private final int viewHeight = 6; // Number of options to show at once

    public Display(String npcName, String npcText, List<PlayerOption> options, int currentSelection) {
        this.npcName = npcName;
        this.npcText = npcText;
        this.options = options;
        this.selectionCursor = new Cursor(0, options.size() - 1, currentSelection);
    }

    public int getSelection() {
        return selectionCursor.get();
    }

    public void scroll(Scroll direction) {
        selectionCursor.modify(direction.getModification());
    }

    public Component getDisplay() {
        List<Component> lines = new ArrayList<>();

        // 1. Add NPC text
        lines.add(ChatUtils.format("<gold>" + npcName + ":</gold> " + npcText));
        lines.add(Component.empty());

        // 2. Determine visible options
        int totalOptions = options.size();
        if (totalOptions == 0) {
            return Component.join(JoinConfiguration.newlines(), lines);
        }

        int start = 0;
        if (totalOptions > viewHeight) {
            start = Math.max(0, selectionCursor.get() - viewHeight / 2);
            start = Math.min(start, totalOptions - viewHeight);
        }
        int end = Math.min(totalOptions, start + viewHeight);

        // 3. Add "Scroll Up" indicator
        if (start > 0) {
            lines.add(ChatUtils.format("<gray><i>(Scroll Up)</i>"));
        }

        // 4. Build and add player option lines
        for (int i = start; i < end; i++) {
            PlayerOption option = options.get(i);
            String prefix = (i == selectionCursor.get()) ? "<yellow><b>> </b>" : "<gray>  ";
            lines.add(ChatUtils.format(prefix + option.text()));
        }

        // 5. Add "Scroll Down" indicator
        if (end < totalOptions) {
            lines.add(ChatUtils.format("<gray><i>(Scroll Down)</i>"));
        }

        lines.add(Component.empty());

        return Component.join(JoinConfiguration.newlines(), lines);
    }
}
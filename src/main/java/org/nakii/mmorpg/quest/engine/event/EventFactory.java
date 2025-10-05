package org.nakii.mmorpg.quest.engine;

import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.engine.event.*;
import org.nakii.mmorpg.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EventFactory {

    private final MMORPGCore plugin;

    public EventFactory(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    public Optional<QuestEvent> createEvent(String eventString) {
        if (eventString == null || eventString.isEmpty()) {
            return Optional.empty();
        }

        String[] parts = eventString.split(" ");
        String type = parts[0].toLowerCase();

        switch (type) {
            case "tag":
                if (parts.length >= 3) {
                    boolean add = parts[1].equalsIgnoreCase("add");
                    return Optional.of(new TagEvent(parts[2], add));
                }
                break;

            case "message":
                if (parts.length >= 2) {
                    String message = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
                    return Optional.of(new MessageEvent(message));
                }
                break;

            case "give":
                if (parts.length >= 2) {
                    String[] itemParts = parts[1].split(":", 2);
                    String itemId = itemParts[0];
                    int amount = (itemParts.length > 1) ? Integer.parseInt(itemParts[1]) : 1;
                    return Optional.of(new GiveItemEvent(itemId, amount));
                }
                break;

            case "reputation":
                if (parts.length >= 3) {
                    String faction = parts[1];
                    double amount = Double.parseDouble(parts[2]);
                    return Optional.of(new ReputationEvent(faction, amount));
                }
            case "folder":
                if (parts.length >= 2) {
                    // The second part is always the comma-separated list of child events
                    List<String> childEvents = Arrays.asList(parts[1].split(","));

                    // The rest of the parts are the folder's own parameters
                    Map<String, String> parameters = StringUtils.parseArguments(parts, 2);

                    return Optional.of(new FolderEvent(childEvents, parameters));
                }
            case "objective":
                if (parts.length >= 3) {
                    ObjectiveEvent.Action action = ObjectiveEvent.Action.valueOf(parts[1].toUpperCase());
                    String objectiveId = parts[2];
                    return Optional.of(new ObjectiveEvent(objectiveId, action));
                }
                break;
        }

        plugin.getLogger().warning("Unknown or malformed event string: " + eventString);
        return Optional.empty();
    }
}
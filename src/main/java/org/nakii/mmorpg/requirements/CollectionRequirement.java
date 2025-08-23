package org.nakii.mmorpg.requirements;

import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.CollectionManager;

/**
 * A requirement that checks if a player has reached a certain tier in a collection.
 */
public class CollectionRequirement implements Requirement {

    private final String requiredCollectionId;
    private final int requiredTier;

    public CollectionRequirement(String collectionId, int tier) {
        this.requiredCollectionId = collectionId.toUpperCase();
        this.requiredTier = tier;
    }

    @Override
    public boolean meets(Player player) {
        CollectionManager collectionManager = MMORPGCore.getInstance().getCollectionManager();

        // Get the player's current tier for this collection.
        int playerTier = collectionManager.getTierForPlayer(player, requiredCollectionId);

        // Check if their tier is high enough.
        return playerTier >= requiredTier;
    }
}
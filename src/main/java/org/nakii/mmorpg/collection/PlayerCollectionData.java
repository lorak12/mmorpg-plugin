package org.nakii.mmorpg.collection;

import java.util.HashMap;
import java.util.Map;

/**
 * A data object holding a player's progress for all collection types.
 */
public class PlayerCollectionData {

    // Maps Collection ID (e.g., "CACTUS") to the amount collected.
    private final Map<String, Integer> collectionProgress;

    public PlayerCollectionData() {
        this.collectionProgress = new HashMap<>();
    }

    public int getProgress(String collectionId) {
        return collectionProgress.getOrDefault(collectionId.toUpperCase(), 0);
    }

    public void setProgress(String collectionId, int amount) {
        collectionProgress.put(collectionId.toUpperCase(), amount);
    }

    public void addProgress(String collectionId, int amount) {
        setProgress(collectionId, getProgress(collectionId) + amount);
    }

    public Map<String, Integer> getCollectionProgressMap() {
        return collectionProgress;
    }
}
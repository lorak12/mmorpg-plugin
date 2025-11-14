package org.nakii.mmorpg.requirements;

public class CollectionRequirement implements Requirement {

    private final String requiredCollectionId;
    private final int requiredTier;

    public CollectionRequirement(String collectionId, int tier) {
        this.requiredCollectionId = collectionId.toUpperCase();
        this.requiredTier = tier;
    }

    public String getRequiredCollectionId() {
        return requiredCollectionId;
    }

    public int getRequiredTier() {
        return requiredTier;
    }
}
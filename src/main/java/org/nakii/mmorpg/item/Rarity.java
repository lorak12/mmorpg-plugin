package org.nakii.mmorpg.item;

public enum Rarity {
    COMMON("<white>"),
    UNCOMMON("<green>"),
    RARE("<blue>"),
    EPIC("<dark_purple>"),
    LEGENDARY("<gold>"),
    MYTHIC("<light_purple>"),
    SPECIAL("<red>");

    private final String colorTag;

    Rarity(String colorTag) {
        this.colorTag = colorTag;
    }

    public String getColorTag() {
        return colorTag;
    }

    public static Rarity fromString(String name) {
        try {
            return Rarity.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return COMMON; // Default to COMMON if the rarity is invalid or missing
        }
    }
}
package org.nakii.mmorpg.player;

public enum Stat {
    // --- Combat Stats ---
    DAMAGE("⚔ Damage", "⚔", "<red>", false),
    HEALTH("❤ Health", "❤", "<green>", false),
    DEFENSE("❈ Defense", "❈", "<green>", false),
    STRENGTH("❁ Strength", "❁", "<red>", false),
    INTELLIGENCE("✎ Intelligence", "✎", "<aqua>", false),
    CRIT_CHANCE("☣ Crit Chance", "☣", "<blue>", true),
    CRIT_DAMAGE("☠ Crit Damage", "☠", "<blue>", true),
    BONUS_ATTACK_SPEED("⚔ Bonus Attack Speed", "⚔", "<yellow>", true),
    ABILITY_DAMAGE("๑ Ability Damage", "๑", "<aqua>", true),
    TRUE_DEFENSE("❂ True Defense", "❂", "<white>", false),
    FEROCITY("⫽ Ferocity", "⫽", "<red>", false),
    HEALTH_REGEN("❣ Health Regen", "❣", "<green>", false),
    VITALITY("♨ Vitality", "♨", "<green>", false),
    SWING_RANGE("Ⓢ Swing Range", "Ⓢ", "<white>", false),

    // --- Gathering Stats ---
    MINING_SPEED("⸕ Mining Speed", "⸕", "<gold>", false),
    BREAKING_POWER("Ⓟ Breaking Power", "Ⓟ", "<dark_gray>", false),
    FORAGING_FORTUNE("☘ Foraging Fortune", "☘", "<gold>", false),
    FARMING_FORTUNE("☘ Farming Fortune", "☘", "<gold>", false),
    MINING_FORTUNE("☘ Mining Fortune", "☘", "<gold>", false),
    ORE_FORTUNE("☘ Ore Fortune", "☘", "<gold>", false),
    BLOCK_FORTUNE("☘ Block Fortune", "☘", "<gold>", false),

    // --- Wisdom Stats ---
    COMBAT_WISDOM("☯ Combat Wisdom", "☯", "<dark_purple>", false),
    MINING_WISDOM("☯ Mining Wisdom", "☯", "<dark_purple>", false),
    FARMING_WISDOM("☯ Farming Wisdom", "☯", "<dark_purple>", false),
    FORAGING_WISDOM("☯ Farming Wisdom", "☯", "<dark_purple>", false),
    FISHING_WISDOM("☯ Fishing Wisdom", "☯", "<dark_purple>", false),
    ENCHANTING_WISDOM("☯ Enchanting Wisdom", "☯", "<dark_purple>", false),
    ALCHEMY_WISDOM("☯ Alchemy Wisdom", "☯", "<dark_purple>", false),
    CARPENTRY_WISDOM("☯ Carpentry Wisdom", "☯", "<dark_purple>", false),

    // --- Misc Stats ---
    SPEED("✦ Speed", "✦", "<white>", false),
    MAGIC_FIND("✯ Magic Find", "✯", "<yellow>", false),
    COLD_RESISTANCE("❄ Cold Resistance", "❄", "<aqua>", false),
    HEAT_RESISTANCE("♨ Heat Resistance", "♨", "<gold>", false),

    // --- Fishing Stats ---
    SEA_CREATURE_CHANCE("α Sea Creature Chance", "α", "<dark_aqua>", true),
    FISHING_SPEED("☂ Fishing Speed", "☂", "<dark_aqua>", false),
    TREASURE_CHANCE("⛃ Treasure Chance", "⛃", "<gold>", true);

    private final String displayName;
    private final String symbol;
    private final String colorTag;
    private final boolean isPercentage;

    Stat(String displayName, String symbol, String colorTag, boolean isPercentage) {
        this.displayName = displayName;
        this.symbol = symbol;
        this.colorTag = colorTag;
        this.isPercentage = isPercentage;
    }

    public String getDisplayName() { return displayName; }
    public String getSymbol() { return symbol; }

    public String getSimpleName() {
        return this.displayName.replaceAll("^[❤❈❁✎☣☠⚔๑❂⫽❣♨Ⓢ⸕Ⓟ☘☯✦✯❄α☂⛃] ", "")
                .replace("Bonus ", "");
    }

    /**
     * --- NEW: THE CENTRAL STAT FORMATTER ---
     * Formats a given value according to this stat's rules (color, percentage, etc.).
     *
     * @param value The numeric value of the stat.
     * @return A complete, colored MiniMessage string (e.g., "<red>+50 ❁ Strength</red>").
     */
    public String format(double value) {
        String sign = value > 0 ? "+" : "";
        String formattedValue;

        // Use long for formatting if the double is a whole number, otherwise show one decimal place.
        if (value == (long) value) {
            formattedValue = String.format("%,d", (long) value);
        } else {
            formattedValue = String.format("%,.1f", value);
        }

        if (this.isPercentage) {
            formattedValue += "%";
        }

        return String.format("%s%s%s %s %s", colorTag, sign, formattedValue, this.symbol, this.getSimpleName());
    }
}
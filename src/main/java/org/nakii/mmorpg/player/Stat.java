package org.nakii.mmorpg.player;

public enum Stat {
    // --- Combat Stats ---
    DAMAGE("⚔ Damage", "⚔"),
    HEALTH("❤ Health", "❤"),
    DEFENSE("❈ Defense", "❈"),
    STRENGTH("❁ Strength", "❁"),
    INTELLIGENCE("✎ Intelligence", "✎"),
    CRIT_CHANCE("☣ Crit Chance", "☣"),
    CRIT_DAMAGE("☠ Crit Damage", "☠"),
    BONUS_ATTACK_SPEED("⚔ Bonus Attack Speed", "⚔"),
    ABILITY_DAMAGE("๑ Ability Damage", "๑"),
    TRUE_DEFENSE("❂ True Defense", "❂"),
    FEROCITY("⫽ Ferocity", "⫽"),
    HEALTH_REGEN("❣ Health Regen", "❣"),
    VITALITY("♨ Vitality", "♨"),
    SWING_RANGE("Ⓢ Swing Range", "Ⓢ"),

    // --- Gathering Stats ---
    MINING_SPEED("⸕ Mining Speed", "⸕"),
    BREAKING_POWER("Ⓟ Breaking Power", "Ⓟ"),
    FORAGING_FORTUNE("☘ Foraging Fortune", "☘"),
    FARMING_FORTUNE("☘ Farming Fortune", "☘"),
    MINING_FORTUNE("☘ Mining Fortune", "☘"),
    ORE_FORTUNE("☘ Ore Fortune", "☘"),
    BLOCK_FORTUNE("☘ Block Fortune", "☘"),

    // --- Wisdom Stats ---
    COMBAT_WISDOM("☯ Combat Wisdom", "☯"),
    MINING_WISDOM("☯ Mining Wisdom", "☯"),
    FARMING_WISDOM("☯ Farming Wisdom", "☯"),
    FORAGING_WISDOM("☯ Foraging Wisdom", "☯"),
    FISHING_WISDOM("☯ Fishing Wisdom", "☯"),
    ENCHANTING_WISDOM("☯ Enchanting Wisdom", "☯"),
    ALCHEMY_WISDOM("☯ Alchemy Wisdom", "☯"),
    CARPENTRY_WISDOM("☯ Carpentry Wisdom", "☯"),

    // --- Misc Stats ---
    SPEED("✦ Speed", "✦"),
    MAGIC_FIND("✯ Magic Find", "✯"),
    COLD_RESISTANCE("❄ Cold Resistance", "❄"),
    HEAT_RESISTANCE("♨ Heat Resistance", "♨"),

    // --- Fishing Stats ---
    SEA_CREATURE_CHANCE("α Sea Creature Chance", "α"),
    FISHING_SPEED("☂ Fishing Speed", "☂"),
    TREASURE_CHANCE("⛃ Treasure Chance", "⛃");

    private final String displayName;
    private final String symbol;

    Stat(String displayName, String symbol) {
        this.displayName = displayName;
        this.symbol = symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getSimpleName() {
        // Removes the symbol and any extra words for cleaner lore
        return this.displayName.replaceAll("^[❤❈❁✎☣☠⚔๑❂⫽❣♨Ⓢ⸕Ⓟ☘☯✦✯❄α☂⛃] ", "")
                .replace("Bonus ", "");
    }
}
package org.nakii.mmorpg.stats;

/**
 * Holds all stats for a player or mob.
 * Base values are defined in the default constructor.
 */
public class PlayerStats {

    //<editor-fold desc="Combat Stats">
    private double health = 100;
    private double defense = 0;
    private double strength = 0;
    private double intelligence = 100;
    private double critChance = 30;
    private double critDamage = 50;
    private double bonusAttackSpeed = 0;
    private double abilityDamage = 0;
    private double trueDefense = 0;
    private double ferocity = 0;
    private double healthRegen = 100;
    private double vitality = 100;
    private double swingRange = 3;
    //</editor-fold>

    //<editor-fold desc="Gathering Stats">
    private double miningSpeed = 0;
    private double breakingPower = 0;
    private double foragingFortune = 0;
    private double farmingFortune = 0;
    private double miningFortune = 0;
    private double oreFortune = 0;
    private double blockFortune = 0;
    //</editor-fold>

    //<editor-fold desc="Wisdom Stats">
    private double combatWisdom = 0;
    private double miningWisdom = 0;
    private double farmingWisdom = 0;
    private double foragingWisdom = 0;
    private double fishingWisdom = 0;
    private double enchantingWisdom = 0;
    private double alchemyWisdom = 0;
    private double carpentryWisdom = 0;
    //</editor-fold>

    //<editor-fold desc="Misc Stats">
    private double speed = 100;
    private double magicFind = 0;
    private double coldResistance = 0;
    private double heatResistance = 0;
    private double cold = 0;
    private double heat = 0;
    //</editor-fold>

    //<editor-fold desc="Fishing Stats">
    private double seaCreatureChance = 20;
    private double fishingSpeed = 0;
    private double treasureChance = 0;
    //</editor-fold>

    //<editor-fold desc="Internal / Non-Item Stats">
    private double damage = 1.0; // Base fist damage
    //</editor-fold>

    /**
     * Default constructor. Initializes stats with their base values.
     */
    public PlayerStats() {
    }

    /**
     * Empty constructor. Initializes all stats to zero for calculation purposes.
     * @param empty A boolean flag.
     */
    public PlayerStats(boolean empty) {
        if (empty) {
            // Set all stats to 0
            this.health = 0;
            this.defense = 0;
            this.strength = 0;
            this.intelligence = 0;
            this.critChance = 0;
            this.critDamage = 0;
            this.bonusAttackSpeed = 0;
            this.abilityDamage = 0;
            this.trueDefense = 0;
            this.ferocity = 0;
            this.healthRegen = 0;
            this.vitality = 0;
            this.swingRange = 0;
            this.miningSpeed = 0;
            this.breakingPower = 0;
            this.foragingFortune = 0;
            this.farmingFortune = 0;
            this.miningFortune = 0;
            this.oreFortune = 0;
            this.blockFortune = 0;
            this.combatWisdom = 0;
            this.miningWisdom = 0;
            this.farmingWisdom = 0;
            this.foragingWisdom = 0;
            this.fishingWisdom = 0;
            this.enchantingWisdom = 0;
            this.alchemyWisdom = 0;
            this.carpentryWisdom = 0;
            this.speed = 0;
            this.magicFind = 0;
            this.coldResistance = 0;
            this.heatResistance = 0;
            this.cold = 0;
            this.heat = 0;
            this.seaCreatureChance = 0;
            this.fishingSpeed = 0;
            this.treasureChance = 0;
            this.damage = 0;
        }
    }

    //Setter and Getters

    public double getHealth() { return health; }
    public void setHealth(double health) { this.health = health; }
    public void addHealth(double amount) { this.health += amount; }

    public double getDefense() { return defense; }
    public void setDefense(double defense) { this.defense = defense; }
    public void addDefense(double amount) { this.defense += amount; }

    public double getStrength() { return strength; }
    public void setStrength(double strength) { this.strength = strength; }
    public void addStrength(double amount) { this.strength += amount; }

    public double getIntelligence() { return intelligence; }
    public void setIntelligence(double intelligence) { this.intelligence = intelligence; }
    public void addIntelligence(double amount) { this.intelligence += amount; }

    public double getCritChance() { return critChance; }
    public void setCritChance(double critChance) { this.critChance = critChance; }
    public void addCritChance(double amount) { this.critChance += amount; }

    public double getCritDamage() { return critDamage; }
    public void setCritDamage(double critDamage) { this.critDamage = critDamage; }
    public void addCritDamage(double amount) { this.critDamage += amount; }

    public double getBonusAttackSpeed() { return bonusAttackSpeed; }
    public void setBonusAttackSpeed(double bonusAttackSpeed) { this.bonusAttackSpeed = bonusAttackSpeed; }
    public void addBonusAttackSpeed(double amount) { this.bonusAttackSpeed += amount; }

    public double getAbilityDamage() { return abilityDamage; }
    public void setAbilityDamage(double abilityDamage) { this.abilityDamage = abilityDamage; }
    public void addAbilityDamage(double amount) { this.abilityDamage += amount; }

    public double getTrueDefense() { return trueDefense; }
    public void setTrueDefense(double trueDefense) { this.trueDefense = trueDefense; }
    public void addTrueDefense(double amount) { this.trueDefense += amount; }

    public double getFerocity() { return ferocity; }
    public void setFerocity(double ferocity) { this.ferocity = ferocity; }
    public void addFerocity(double amount) { this.ferocity += amount; }

    public double getHealthRegen() { return healthRegen; }
    public void setHealthRegen(double healthRegen) { this.healthRegen = healthRegen; }
    public void addHealthRegen(double amount) { this.healthRegen += amount; }

    public double getVitality() { return vitality; }
    public void setVitality(double vitality) { this.vitality = vitality; }
    public void addVitality(double amount) { this.vitality += amount; }

    public double getSwingRange() { return swingRange; }
    public void setSwingRange(double swingRange) { this.swingRange = swingRange; }
    public void addSwingRange(double amount) { this.swingRange += amount; }

    public double getMiningSpeed() { return miningSpeed; }
    public void setMiningSpeed(double miningSpeed) { this.miningSpeed = miningSpeed; }
    public void addMiningSpeed(double amount) { this.miningSpeed += amount; }

    public double getBreakingPower() { return breakingPower; }
    public void setBreakingPower(double breakingPower) { this.breakingPower = breakingPower; }
    public void addBreakingPower(double amount) { this.breakingPower += amount; }

    public double getForagingFortune() { return foragingFortune; }
    public void setForagingFortune(double foragingFortune) { this.foragingFortune = foragingFortune; }
    public void addForagingFortune(double amount) { this.foragingFortune += amount; }

    public double getFarmingFortune() { return farmingFortune; }
    public void setFarmingFortune(double farmingFortune) { this.farmingFortune = farmingFortune; }
    public void addFarmingFortune(double amount) { this.farmingFortune += amount; }

    public double getMiningFortune() { return miningFortune; }
    public void setMiningFortune(double miningFortune) { this.miningFortune = miningFortune; }
    public void addMiningFortune(double amount) { this.miningFortune += amount; }

    public double getOreFortune() { return oreFortune; }
    public void setOreFortune(double oreFortune) { this.oreFortune = oreFortune; }
    public void addOreFortune(double amount) { this.oreFortune += amount; }

    public double getBlockFortune() { return blockFortune; }
    public void setBlockFortune(double blockFortune) { this.blockFortune = blockFortune; }
    public void addBlockFortune(double amount) { this.blockFortune += amount; }

    public double getCombatWisdom() { return combatWisdom; }
    public void setCombatWisdom(double combatWisdom) { this.combatWisdom = combatWisdom; }
    public void addCombatWisdom(double amount) { this.combatWisdom += amount; }

    public double getMiningWisdom() { return miningWisdom; }
    public void setMiningWisdom(double miningWisdom) { this.miningWisdom = miningWisdom; }
    public void addMiningWisdom(double amount) { this.miningWisdom += amount; }

    public double getFarmingWisdom() { return farmingWisdom; }
    public void setFarmingWisdom(double farmingWisdom) { this.farmingWisdom = farmingWisdom; }
    public void addFarmingWisdom(double amount) { this.farmingWisdom += amount; }

    public double getForagingWisdom() { return foragingWisdom; }
    public void setForagingWisdom(double foragingWisdom) { this.foragingWisdom = foragingWisdom; }
    public void addForagingWisdom(double amount) { this.foragingWisdom += amount; }

    public double getFishingWisdom() { return fishingWisdom; }
    public void setFishingWisdom(double fishingWisdom) { this.fishingWisdom = fishingWisdom; }
    public void addFishingWisdom(double amount) { this.fishingWisdom += amount; }

    public double getEnchantingWisdom() { return enchantingWisdom; }
    public void setEnchantingWisdom(double enchantingWisdom) { this.enchantingWisdom = enchantingWisdom; }
    public void addEnchantingWisdom(double amount) { this.enchantingWisdom += amount; }

    public double getAlchemyWisdom() { return alchemyWisdom; }
    public void setAlchemyWisdom(double alchemyWisdom) { this.alchemyWisdom = alchemyWisdom; }
    public void addAlchemyWisdom(double amount) { this.alchemyWisdom += amount; }

    public double getCarpentryWisdom() { return carpentryWisdom; }
    public void setCarpentryWisdom(double carpentryWisdom) { this.carpentryWisdom = carpentryWisdom; }
    public void addCarpentryWisdom(double amount) { this.carpentryWisdom += amount; }

    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }
    public void addSpeed(double amount) { this.speed += amount; }

    public double getMagicFind() { return magicFind; }
    public void setMagicFind(double magicFind) { this.magicFind = magicFind; }
    public void addMagicFind(double amount) { this.magicFind += amount; }

    public double getColdResistance() { return coldResistance; }
    public void setColdResistance(double coldResistance) { this.coldResistance = coldResistance; }
    public void addColdResistance(double amount) { this.coldResistance += amount; }

    public double getHeatResistance() { return heatResistance; }
    public void setHeatResistance(double heatResistance) { this.heatResistance = heatResistance; }
    public void addHeatResistance(double amount) { this.heatResistance += amount; }

    public double getPlayerCold() { return cold; }
    public void setPlayerCold(double cold) { this.cold = cold;}
    public void addPlayerCold(double amount) { this.cold += amount; }

    public double getPlayerHeat() { return heat; }
    public void setPlayerHeat(double heat) { this.heat = heat;}
    public void addPlayerHeat(double amount) { this.heat += amount; }

    public double getSeaCreatureChance() { return seaCreatureChance; }
    public void setSeaCreatureChance(double seaCreatureChance) { this.seaCreatureChance = seaCreatureChance; }
    public void addSeaCreatureChance(double amount) { this.seaCreatureChance += amount; }

    public double getFishingSpeed() { return fishingSpeed; }
    public void setFishingSpeed(double fishingSpeed) { this.fishingSpeed = fishingSpeed; }
    public void addFishingSpeed(double amount) { this.fishingSpeed += amount; }

    public double getTreasureChance() { return treasureChance; }
    public void setTreasureChance(double treasureChance) { this.treasureChance = treasureChance; }
    public void addTreasureChance(double amount) { this.treasureChance += amount; }

    public double getDamage() { return damage; }
    public void setDamage(double damage) { this.damage = damage; }
    public void addDamage(double amount) { this.damage += amount; }
}
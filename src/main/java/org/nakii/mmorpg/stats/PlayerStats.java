package org.nakii.mmorpg.stats;

public class PlayerStats {

    private double health;
    private double strength;
    private double critChance;
    private double critDamage;
    private double speed;
    private double luck;
    private double lethality;
    private double tenacity;
    private double armorPenetration;
    private double armor;
    private double mana;
    private double manaRegen;
    private double hpRegen;

    /**
     * Default constructor for base stats.
     */
    public PlayerStats() {
        this.health = 20.0;
        this.strength = 1.0;
        this.critChance = 5.0;
        this.critDamage = 50.0;
        this.speed = 100.0;
        this.luck = 0.0;
        this.lethality = 0.0;
        this.tenacity = 0.0;
        this.armorPenetration = 0.0;
        this.armor = 0.0;
        this.mana = 100.0;
        this.manaRegen = 1.0;
        this.hpRegen = 0.5;
    }

    /**
     * Constructor to create an empty stats object, typically for item or skill calculations.
     * @param empty If true, all stats are initialized to zero.
     */
    public PlayerStats(boolean empty) {
        if (empty) {
            this.health = 0.0;
            this.strength = 0.0;
            this.critChance = 0.0;
            this.critDamage = 0.0;
            this.speed = 0.0;
            this.luck = 0.0;
            this.lethality = 0.0;
            this.tenacity = 0.0;
            this.armorPenetration = 0.0;
            this.armor = 0.0;
            this.mana = 0.0;
            this.manaRegen = 0.0;
            this.hpRegen = 0.0;
        }
    }


    // Getters and Setters for all fields...

    public double getHealth() { return health; }
    public void setHealth(double health) { this.health = health; }
    public void addHealth(double amount) { this.health += amount; }

    public double getStrength() { return strength; }
    public void setStrength(double strength) { this.strength = strength; }
    public void addStrength(double amount) { this.strength += amount; }

    public double getCritChance() { return critChance; }
    public void setCritChance(double critChance) { this.critChance = critChance; }
    public void addCritChance(double amount) { this.critChance += amount; }

    public double getCritDamage() { return critDamage; }
    public void setCritDamage(double critDamage) { this.critDamage = critDamage; }
    public void addCritDamage(double amount) { this.critDamage += amount; }

    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed > 600 ? 600 : speed; }
    public void addSpeed(double amount) { setSpeed(this.speed + amount); }

    public double getLuck() { return luck; }
    public void setLuck(double luck) { this.luck = luck; }
    public void addLuck(double amount) { this.luck += amount; }

    public double getLethality() { return lethality; }
    public void setLethality(double lethality) { this.lethality = lethality; }
    public void addLethality(double amount) { this.lethality += amount; }

    public double getTenacity() { return tenacity; }
    public void setTenacity(double tenacity) { this.tenacity = tenacity; }
    public void addTenacity(double amount) { this.tenacity += amount; }

    public double getArmorPenetration() { return armorPenetration; }
    public void setArmorPenetration(double armorPenetration) { this.armorPenetration = armorPenetration; }
    public void addArmorPenetration(double amount) { this.armorPenetration += amount; }

    public double getArmor() { return armor; }
    public void setArmor(double armor) { this.armor = armor; }
    public void addArmor(double amount) { this.armor += amount; }

    public double getMana() { return mana; }
    public void setMana(double mana) { this.mana = mana; }
    public void addMana(double amount) { this.mana += amount; }

    public double getManaRegen() { return manaRegen; }
    public void setManaRegen(double manaRegen) { this.manaRegen = manaRegen; }
    public void addManaRegen(double amount) { this.manaRegen += amount; }

    public double getHpRegen() { return hpRegen; }
    public void setHpRegen(double hpRegen) { this.hpRegen = hpRegen; }
    public void addHpRegen(double amount) { this.hpRegen += amount; }
}
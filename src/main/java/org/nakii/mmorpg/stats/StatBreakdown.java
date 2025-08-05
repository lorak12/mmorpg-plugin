package org.nakii.mmorpg.stats;

public class StatBreakdown {

    private double baseHealth, itemHealth, skillHealth, totalHealth;
    private double baseStrength, itemStrength, skillStrength, totalStrength;
    private double baseCritChance, itemCritChance, skillCritChance, totalCritChance;
    private double baseCritDamage, itemCritDamage, skillCritDamage, totalCritDamage;
    private double baseSpeed, itemSpeed, skillSpeed, totalSpeed;
    private double baseLuck, itemLuck, skillLuck, totalLuck;
    private double baseLethality, itemLethality, skillLethality, totalLethality;
    private double baseTenacity, itemTenacity, skillTenacity, totalTenacity;
    private double baseArmorPen, itemArmorPen, skillArmorPen, totalArmorPen;
    private double baseArmor, itemArmor, skillArmor, totalArmor;
    private double baseMana, itemMana, skillMana, totalMana;
    private double baseManaRegen, itemManaRegen, skillManaRegen, totalManaRegen;
    private double baseHpRegen, itemHpRegen, skillHpRegen, totalHpRegen;

    // Getters
    public double getBaseHealth() { return baseHealth; }
    public double getItemHealth() { return itemHealth; }
    public double getSkillHealth() { return skillHealth; }
    public double getTotalHealth() { return totalHealth; }
    public double getBaseStrength() { return baseStrength; }
    public double getItemStrength() { return itemStrength; }
    public double getSkillStrength() { return skillStrength; }
    public double getTotalStrength() { return totalStrength; }
    public double getBaseCritChance() { return baseCritChance; }
    public double getItemCritChance() { return itemCritChance; }
    public double getSkillCritChance() { return skillCritChance; }
    public double getTotalCritChance() { return totalCritChance; }
    public double getBaseCritDamage() { return baseCritDamage; }
    public double getItemCritDamage() { return itemCritDamage; }
    public double getSkillCritDamage() { return skillCritDamage; }
    public double getTotalCritDamage() { return totalCritDamage; }
    public double getBaseSpeed() { return baseSpeed; }
    public double getItemSpeed() { return itemSpeed; }
    public double getSkillSpeed() { return skillSpeed; }
    public double getTotalSpeed() { return totalSpeed; }
    public double getBaseLuck() { return baseLuck; }
    public double getItemLuck() { return itemLuck; }
    public double getSkillLuck() { return skillLuck; }
    public double getTotalLuck() { return totalLuck; }
    public double getBaseLethality() { return baseLethality; }
    public double getItemLethality() { return itemLethality; }
    public double getSkillLethality() { return skillLethality; }
    public double getTotalLethality() { return totalLethality; }
    public double getBaseTenacity() { return baseTenacity; }
    public double getItemTenacity() { return itemTenacity; }
    public double getSkillTenacity() { return skillTenacity; }
    public double getTotalTenacity() { return totalTenacity; }
    public double getBaseArmorPen() { return baseArmorPen; }
    public double getItemArmorPen() { return itemArmorPen; }
    public double getSkillArmorPen() { return skillArmorPen; }
    public double getTotalArmorPen() { return totalArmorPen; }
    public double getBaseArmor() { return baseArmor; }
    public double getItemArmor() { return itemArmor; }
    public double getSkillArmor() { return skillArmor; }
    public double getTotalArmor() { return totalArmor; }
    public double getBaseMana() { return baseMana; }
    public double getItemMana() { return itemMana; }
    public double getSkillMana() { return skillMana; }
    public double getTotalMana() { return totalMana; }
    public double getBaseManaRegen() { return baseManaRegen; }
    public double getItemManaRegen() { return itemManaRegen; }
    public double getSkillManaRegen() { return skillManaRegen; }
    public double getTotalManaRegen() { return totalManaRegen; }
    public double getBaseHpRegen() { return baseHpRegen; }
    public double getItemHpRegen() { return itemHpRegen; }
    public double getSkillHpRegen() { return skillHpRegen; }
    public double getTotalHpRegen() { return totalHpRegen; }

    // Setters
    public void setBaseHealth(double baseHealth) { this.baseHealth = baseHealth; }
    public void setItemHealth(double itemHealth) { this.itemHealth = itemHealth; }
    public void setSkillHealth(double skillHealth) { this.skillHealth = skillHealth; }
    public void setTotalHealth(double totalHealth) { this.totalHealth = totalHealth; }
    public void setBaseStrength(double baseStrength) { this.baseStrength = baseStrength; }
    public void setItemStrength(double itemStrength) { this.itemStrength = itemStrength; }
    public void setSkillStrength(double skillStrength) { this.skillStrength = skillStrength; }
    public void setTotalStrength(double totalStrength) { this.totalStrength = totalStrength; }
    public void setBaseCritChance(double baseCritChance) { this.baseCritChance = baseCritChance; }
    public void setItemCritChance(double itemCritChance) { this.itemCritChance = itemCritChance; }
    public void setSkillCritChance(double skillCritChance) { this.skillCritChance = skillCritChance; }
    public void setTotalCritChance(double totalCritChance) { this.totalCritChance = totalCritChance; }
    public void setBaseCritDamage(double baseCritDamage) { this.baseCritDamage = baseCritDamage; }
    public void setItemCritDamage(double itemCritDamage) { this.itemCritDamage = itemCritDamage; }
    public void setSkillCritDamage(double skillCritDamage) { this.skillCritDamage = skillCritDamage; }
    public void setTotalCritDamage(double totalCritDamage) { this.totalCritDamage = totalCritDamage; }
    public void setBaseSpeed(double baseSpeed) { this.baseSpeed = baseSpeed; }
    public void setItemSpeed(double itemSpeed) { this.itemSpeed = itemSpeed; }
    public void setSkillSpeed(double skillSpeed) { this.skillSpeed = skillSpeed; }
    public void setTotalSpeed(double totalSpeed) { this.totalSpeed = totalSpeed; }
    public void setBaseLuck(double baseLuck) { this.baseLuck = baseLuck; }
    public void setItemLuck(double itemLuck) { this.itemLuck = itemLuck; }
    public void setSkillLuck(double skillLuck) { this.skillLuck = skillLuck; }
    public void setTotalLuck(double totalLuck) { this.totalLuck = totalLuck; }
    public void setBaseLethality(double baseLethality) { this.baseLethality = baseLethality; }
    public void setItemLethality(double itemLethality) { this.itemLethality = itemLethality; }
    public void setSkillLethality(double skillLethality) { this.skillLethality = skillLethality; }
    public void setTotalLethality(double totalLethality) { this.totalLethality = totalLethality; }
    public void setBaseTenacity(double baseTenacity) { this.baseTenacity = baseTenacity; }
    public void setItemTenacity(double itemTenacity) { this.itemTenacity = itemTenacity; }
    public void setSkillTenacity(double skillTenacity) { this.skillTenacity = skillTenacity; }
    public void setTotalTenacity(double totalTenacity) { this.totalTenacity = totalTenacity; }
    public void setBaseArmorPen(double baseArmorPen) { this.baseArmorPen = baseArmorPen; }
    public void setItemArmorPen(double itemArmorPen) { this.itemArmorPen = itemArmorPen; }
    public void setSkillArmorPen(double skillArmorPen) { this.skillArmorPen = skillArmorPen; }
    public void setTotalArmorPen(double totalArmorPen) { this.totalArmorPen = totalArmorPen; }
    public void setBaseArmor(double baseArmor) { this.baseArmor = baseArmor; }
    public void setItemArmor(double itemArmor) { this.itemArmor = itemArmor; }
    public void setSkillArmor(double skillArmor) { this.skillArmor = skillArmor; }
    public void setTotalArmor(double totalArmor) { this.totalArmor = totalArmor; }
    public void setBaseMana(double baseMana) { this.baseMana = baseMana; }
    public void setItemMana(double itemMana) { this.itemMana = itemMana; }
    public void setSkillMana(double skillMana) { this.skillMana = skillMana; }
    public void setTotalMana(double totalMana) { this.totalMana = totalMana; }
    public void setBaseManaRegen(double baseManaRegen) { this.baseManaRegen = baseManaRegen; }
    public void setItemManaRegen(double itemManaRegen) { this.itemManaRegen = itemManaRegen; }
    public void setSkillManaRegen(double skillManaRegen) { this.skillManaRegen = skillManaRegen; }
    public void setTotalManaRegen(double totalManaRegen) { this.totalManaRegen = totalManaRegen; }
    public void setBaseHpRegen(double baseHpRegen) { this.baseHpRegen = baseHpRegen; }
    public void setItemHpRegen(double itemHpRegen) { this.itemHpRegen = itemHpRegen; }
    public void setSkillHpRegen(double skillHpRegen) { this.skillHpRegen = skillHpRegen; }
    public void setTotalHpRegen(double totalHpRegen) { this.totalHpRegen = totalHpRegen; }
}
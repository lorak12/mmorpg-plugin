package org.nakii.mmorpg.requirements;

public class SlayerRequirement implements Requirement {

    private final String requiredSlayerType;
    private final int requiredLevel;

    public SlayerRequirement(String slayerType, int level) {
        this.requiredSlayerType = slayerType;
        this.requiredLevel = level;
    }

    public String getRequiredSlayerType() {
        return requiredSlayerType;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }
}
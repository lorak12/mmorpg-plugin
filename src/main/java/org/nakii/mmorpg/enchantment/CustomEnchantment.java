package org.nakii.mmorpg.enchantment;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a single custom enchantment loaded from the configuration.
 */
public class CustomEnchantment {

    private final String id;
    private final String displayName;
    private final int maxLevel;
    private final List<String> description;
    private final List<ApplicableType> applicableTypes;
    private final List<String> incompatibilities;
    private final Map<String, String> statModifiers;
    private final String customLogicKey;
    private final Map<String, String> attributes;

    public CustomEnchantment(String id, ConfigurationSection config) {
        this.id = id;
        this.displayName = config.getString("display-name", "Unnamed Enchantment");
        this.maxLevel = config.getInt("max-level", 1);
        this.description = config.getStringList("description");
        this.incompatibilities = config.getStringList("incompatibilities");

        this.applicableTypes = config.getStringList("applicable-to").stream()
                .map(String::toUpperCase)
                .map(ApplicableType::valueOf)
                .collect(Collectors.toList());

        // We now safely load the effects, checking if each subsection exists before accessing it.
        ConfigurationSection effects = config.getConfigurationSection("effects");
        if (effects != null) {
            this.customLogicKey = effects.getString("custom-logic-key");

            ConfigurationSection statModifiersSection = effects.getConfigurationSection("stat-modifiers");
            if (statModifiersSection != null) {
                this.statModifiers = statModifiersSection.getValues(false).entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
            } else {
                this.statModifiers = Collections.emptyMap();
            }

            ConfigurationSection attributesSection = effects.getConfigurationSection("attributes");
            if (attributesSection != null) {
                this.attributes = attributesSection.getValues(false).entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
            } else {
                this.attributes = Collections.emptyMap();
            }
        } else {
            this.statModifiers = Collections.emptyMap();
            this.customLogicKey = null;
            this.attributes = Collections.emptyMap();
        }
    }

    // Getters for all fields
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getMaxLevel() { return maxLevel; }
    public List<String> getDescription() { return description; }
    public List<ApplicableType> getApplicableTypes() { return applicableTypes; }
    public List<String> getIncompatibilities() { return incompatibilities; }
    public Map<String, String> getStatModifiers() { return statModifiers; }
    public String getCustomLogicKey() { return customLogicKey; }
    public Map<String, String> getAttributes() { return attributes; }
}
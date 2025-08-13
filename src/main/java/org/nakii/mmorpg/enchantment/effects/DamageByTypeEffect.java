package org.nakii.mmorpg.enchantment.effects;

import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.nakii.mmorpg.enchantment.CustomEnchantment;

import java.util.List;
import java.util.stream.Collectors;

public class DamageByTypeEffect implements EnchantmentEffect {

    @Override
    public double onDamageModify(double initialDamage, EntityDamageByEntityEvent event, CustomEnchantment enchantment, int level, boolean isCritical) {
        // We get the list of applicable mob types directly from the enchantment's description.
        // This is a simple way to store the configuration without adding more fields.
        // In a more advanced system, you might add a dedicated 'targets' list in the YAML.
        List<EntityType> targetTypes = enchantment.getDescription().stream()
                .map(s -> {
                    try {
                        // Assuming a line in the description is just the EntityType name, e.g., "ZOMBIE"
                        return EntityType.valueOf(s.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        if (targetTypes.isEmpty()) {
            // Failsafe if the description isn't configured correctly.
            return initialDamage;
        }

        EntityType victimType = event.getEntityType();

        if (targetTypes.contains(victimType)) {
            // The 'value' from YAML is the percentage damage bonus.
            double multiplier = 1.0 + (enchantment.getValue(level) / 100.0);
            return initialDamage * multiplier;
        }

        return initialDamage;
    }
}
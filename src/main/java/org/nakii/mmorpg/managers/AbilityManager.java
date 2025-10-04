package org.nakii.mmorpg.managers;

import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.abilities.Ability;
import org.nakii.mmorpg.abilities.DaggerThrowAbility;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AbilityManager {

    private final MMORPGCore plugin;
    private final Map<String, Ability> abilityRegistry = new HashMap<>();

    public AbilityManager(MMORPGCore plugin) {
        this.plugin = plugin;
        registerAbilities();
    }

    private void registerAbilities() {
        // In a real system, you might scan a package for classes implementing Ability.
        // For now, we'll register them manually.
        register(new DaggerThrowAbility(plugin));
        plugin.getLogger().info("Registered " + abilityRegistry.size() + " abilities.");
    }

    private void register(Ability ability) {
        abilityRegistry.put(ability.getKey().toUpperCase(), ability);
    }

    public Optional<Ability> getAbility(String key) {
        if (key == null) return Optional.empty();
        return Optional.ofNullable(abilityRegistry.get(key.toUpperCase()));
    }
}
package org.nakii.mmorpg.managers;

import org.nakii.mmorpg.enchantment.effects.*;
import java.util.HashMap;
import java.util.Map;

public class EnchantmentEffectManager {

    private final Map<String, EnchantmentEffect> effectRegistry = new HashMap<>();

    public EnchantmentEffectManager(
            DoTManager doTManager,
            CombatTracker combatTracker,
            DebuffManager debuffManager,
            TimedBuffManager timedBuffManager,
            StatsManager statsManager
    ) {
        // Register all effects, injecting their required dependencies.
        register("life_steal", new LifeStealEffect());
        register("cleave", new CleaveEffect());
        register("first_strike", new FirstStrikeEffect(combatTracker));
        register("execute", new ExecuteEffect());
        register("giant_killer", new GiantKillerEffect());
        register("vampirism", new VampirismEffect());
        register("lethality", new LethalityEffect(debuffManager));
        register("venomous", new VenomousEffect(debuffManager));
        register("triple_strike", new TripleStrikeEffect(combatTracker));
        register("damage_by_type", new DamageByTypeEffect());
        register("knockback", new KnockbackEffect());
        register("overload", new OverloadEffect(statsManager));
        register("champion", new ChampionEffect(combatTracker));
        register("reflection", new ReflectionEffect(statsManager));
        register("counter_strike", new CounterStrikeEffect(combatTracker, timedBuffManager));
        register("prosecute", new ProsecuteEffect());
        register("titan_killer", new TitanKillerEffect());
        register("thorns", new ThornsEffect());
        register("rejuvenate", new RejuvenateEffect());
        register("vicious", new ViciousEffect());
        register("fire_aspect", new FireAspectEffect(doTManager));
    }

    private void register(String key, EnchantmentEffect effect) {
        effectRegistry.put(key.toLowerCase(), effect);
    }

    public EnchantmentEffect getEffect(String key) {
        return effectRegistry.get(key.toLowerCase());
    }
}
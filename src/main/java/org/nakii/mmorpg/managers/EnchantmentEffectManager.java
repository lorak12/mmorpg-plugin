package org.nakii.mmorpg.managers;

import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.enchantment.effects.*;

import java.util.HashMap;
import java.util.Map;

public class EnchantmentEffectManager {

    private final Map<String, EnchantmentEffect> effectRegistry = new HashMap<>();

    public EnchantmentEffectManager(MMORPGCore plugin) {
        registerEffects();
    }

    private void registerEffects() {
        // Register all your custom logic enchantments here
        register("life_steal", new LifeStealEffect());
        register("cleave", new CleaveEffect());
        register("first_strike", new FirstStrikeEffect());
        register("execute", new ExecuteEffect());
        register("giant_killer", new GiantKillerEffect());
        register("vampirism", new VampirismEffect());
        register("lethality", new LethalityEffect());
        register("venomous", new VenomousEffect());
        register("triple_strike", new TripleStrikeEffect());
        register("damage_by_type", new DamageByTypeEffect());
        register("knockback", new KnockbackEffect());
        register("overload", new OverloadEffect());
        register("champion", new ChampionEffect());
        register("reflection", new ReflectionEffect());
        register("counter_strike", new CounterStrikeEffect());
        register("prosecute", new ProsecuteEffect());
        register("titan_killer", new TitanKillerEffect());
        register("thorns", new ThornsEffect());
        register("rejuvenate", new RejuvenateEffect());
        register("vicious", new ViciousEffect());
    }

    public void register(String key, EnchantmentEffect effect) {
        effectRegistry.put(key.toLowerCase(), effect);
    }

    public EnchantmentEffect getEffect(String key) {
        return effectRegistry.get(key.toLowerCase());
    }
}
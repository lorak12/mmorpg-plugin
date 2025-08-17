package org.nakii.mmorpg.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.BossAbilityManager;
import org.nakii.mmorpg.managers.MobManager;

import java.util.List;

public class BossAIController extends BukkitRunnable {

    private final MMORPGCore plugin;
    private final MobManager mobManager;
    private final BossAbilityManager abilityManager;

    public BossAIController(MMORPGCore plugin) {
        this.plugin = plugin;
        this.mobManager = plugin.getMobManager();
        this.abilityManager = plugin.getBossAbilityManager();
    }

    @Override
    public void run() {
        // Find all active slayer bosses in the world
        // This requires MobManager to have a way to identify bosses
        List<LivingEntity> activeBosses = mobManager.getActiveSlayerBosses();

        for (LivingEntity boss : activeBosses) {
            // In a real system, you'd get the boss's tier and abilities from its NBT data
            // For now, we can use a placeholder logic
            String mobId = mobManager.getMobId(boss);
            if (mobId == null) continue;

            // Simple timer-based logic: try to use an ability every 5 seconds
            if (boss.getTicksLived() % 100 == 0) {
                List<String> abilities = mobManager.getBossAbilities(boss);
                if (!abilities.isEmpty()) {
                    // Pick a random ability to execute
                    String abilityToUse = abilities.get( (int) (Math.random() * abilities.size()) );
                    abilityManager.executeAbility(abilityToUse, boss);
                }
            }
        }
    }
}
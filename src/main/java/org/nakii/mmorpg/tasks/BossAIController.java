package org.nakii.mmorpg.tasks;

import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.managers.BossAbilityManager;
import org.nakii.mmorpg.managers.MobManager;
import org.nakii.mmorpg.managers.SlayerManager;

import java.util.List;

public class BossAIController extends BukkitRunnable {

    private final MobManager mobManager;
    private final BossAbilityManager abilityManager;
    private final SlayerManager slayerManager; // Added dependency

    public BossAIController(MobManager mobManager, BossAbilityManager abilityManager, SlayerManager slayerManager) {
        this.mobManager = mobManager;
        this.abilityManager = abilityManager;
        this.slayerManager = slayerManager; // Store the dependency
    }

    @Override
    public void run() {
        // Find all active slayer bosses in the world. This method is now in SlayerManager.
        for (LivingEntity boss : slayerManager.getActiveSlayerBosses()) {
            // Simple timer-based logic: try to use an ability every 5 seconds
            if (boss.getTicksLived() % 100 == 0) {
                // Get abilities from SlayerManager now
                List<String> abilities = slayerManager.getBossAbilities(boss);
                if (!abilities.isEmpty()) {
                    String abilityToUse = abilities.get((int) (Math.random() * abilities.size()));
                    abilityManager.executeAbility(abilityToUse, boss);
                }
            }
        }
    }
}
package org.nakii.mmorpg.quest.compatibility.vault;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.QuestModule;
import org.nakii.mmorpg.quest.api.BetonQuestApi;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.quest.PrimaryServerThreadData;
import org.nakii.mmorpg.quest.api.quest.QuestTypeRegistries;
import org.nakii.mmorpg.quest.compatibility.Integrator;
import org.nakii.mmorpg.quest.compatibility.vault.condition.MoneyConditionFactory;
import org.nakii.mmorpg.quest.compatibility.vault.event.MoneyEventFactory;
import org.nakii.mmorpg.quest.compatibility.vault.event.PermissionEventFactory;
import org.nakii.mmorpg.quest.compatibility.vault.variable.MoneyVariableFactory;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;

/**
 * Integrator for <a href="https://github.com/MilkBowl/VaultAPI">Vault</a>.
 */
public class VaultIntegrator implements Integrator {

    /**
     * BetonQuest Plugin for registering.
     */
    private final QuestModule plugin;

    /**
     * Constructor for the Vault Integration.
     */
    public VaultIntegrator() {
        plugin = MMORPGCore.getInstance().getQuestModule();
    }

    @Override
    public void hook(final BetonQuestApi api) {
        final BetonQuestLogger log = api.getLoggerFactory().create(VaultIntegrator.class);
        final PrimaryServerThreadData data = api.getPrimaryServerThreadData();

        final ServicesManager servicesManager = Bukkit.getServer().getServicesManager();
        final RegisteredServiceProvider<Economy> economyProvider = servicesManager.getRegistration(Economy.class);
        if (economyProvider == null) {
            log.warn("There is no economy plugin on the server!");
        } else {
            final Economy economy = economyProvider.getProvider();
            final QuestTypeRegistries registries = api.getQuestRegistries();

            registries.event().register("money", new MoneyEventFactory(economy, api.getLoggerFactory(), data,
                    plugin.getPluginMessage(), plugin.getVariableProcessor()));
            registries.condition().register("money", new MoneyConditionFactory(economy, data));
            registries.variable().register("money", new MoneyVariableFactory(economy));
        }

        final RegisteredServiceProvider<Permission> permissionProvider = servicesManager.getRegistration(Permission.class);
        if (permissionProvider == null) {
            log.warn("Could not get permission provider!");
        } else {
            final Permission permission = permissionProvider.getProvider();
            api.getQuestRegistries().event().register("permission", new PermissionEventFactory(permission, data));
        }
    }

    @Override
    public void reload() {
        // Empty
    }

    @Override
    public void close() {
        // Empty
    }
}

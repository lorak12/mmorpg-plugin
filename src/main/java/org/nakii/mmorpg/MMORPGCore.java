package org.nakii.mmorpg;

import de.slikey.effectlib.EffectManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.commands.MmorpgCommand;
import org.nakii.mmorpg.commands.SkillsCommand;
import org.nakii.mmorpg.commands.StatsCommand;
import org.nakii.mmorpg.listeners.*;
import org.nakii.mmorpg.managers.*;

import java.io.File;
import java.sql.SQLException;


public final class MMORPGCore extends JavaPlugin {

    private static MMORPGCore instance;

    // Manager Instances
    private ItemManager itemManager;
    private StatsManager statsManager;
    private HealthManager healthManager;
    private DamageManager damageManager;
    private DatabaseManager databaseManager;
    private SkillManager skillManager;
    private GUIManager guiManager;
    private MobManager mobManager;
    private ZoneManager zoneManager;
    private AbilityManager abilityManager;
    private LootManager lootManager;
    private RecipeManager recipeManager;

    // API Hooks
    private EffectManager effectManager;
    private boolean libsDisguisesEnabled = false;

    @Override
    public void onEnable() {
        instance = this;

        // Hook into APIs first
        setupAPIHooks();

        this.abilityManager = new AbilityManager(this);
        startAutoSaveTask();

        // Create config folders + files
        saveDefaultConfig();
        createItemsFolder();
        saveResource("mobs.yml", false);
        saveResource("zones.yml", false);

        try {
            // Initialize managers
            databaseManager = new DatabaseManager(this);
            databaseManager.connect();

            skillManager = new SkillManager(this);
            itemManager = new ItemManager(this);
            statsManager = new StatsManager(this);
            healthManager = new HealthManager(this);
            damageManager = new DamageManager(this);
            guiManager = new GUIManager(this);
            mobManager = new MobManager(this);
            zoneManager = new ZoneManager(this);
            recipeManager = new RecipeManager(this);
            abilityManager = new AbilityManager(this);

            // Register events + commands
            registerListeners();
            registerCommands();

        } catch (SQLException e) {
            getLogger().severe("Could not connect to the database! Disabling plugin...");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }

        getLogger().info("MMORPGCore enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.disconnect();
        }

        if (effectManager != null) {
            effectManager.dispose(); // good practice
        }

        getLogger().info("MMORPGCore disabled.");
    }

    private void setupAPIHooks() {
        Plugin effectLib = Bukkit.getPluginManager().getPlugin("EffectLib");
        if (effectLib != null && effectLib.isEnabled()) {
            effectManager = new EffectManager(this);
            getLogger().info("Hooked into EffectLib.");
        } else {
            getLogger().warning("EffectLib not found. Some effects may not work.");
        }

        Plugin disguises = Bukkit.getPluginManager().getPlugin("LibsDisguises");
        if (disguises != null && disguises.isEnabled()) {
            libsDisguisesEnabled = true;
            getLogger().info("Hooked into LibsDisguises.");
        } else {
            getLogger().warning("LibsDisguises not found. Mob disguises disabled.");
        }
    }

    private void createItemsFolder() {
        File itemsFolder = new File(getDataFolder(), "items");
        if (!itemsFolder.exists() && itemsFolder.mkdirs()) {
            saveResource("items/example_sword.yml", false);
        }
    }

    private void registerListeners() {
        var pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new InventoryListener(this), this);
        pm.registerEvents(new CombatListener(this), this);
        pm.registerEvents(new EntitySpawningListener(this), this);
        pm.registerEvents(new MiningListener(this), this);
        pm.registerEvents(new FarmingListener(this), this);
        pm.registerEvents(new SmeltingListener(this), this);
        pm.registerEvents(new CraftingListener(this), this);
        pm.registerEvents(new AlchemyListener(this), this);
        pm.registerEvents(new FishingListener(this), this);
        pm.registerEvents(new MagicListener(this), this);
        pm.registerEvents(new ForgingListener(this), this);
        pm.registerEvents(new GUIListener(this), this);
        pm.registerEvents(new RecipeListener(this), this);
        pm.registerEvents(new ZoneWandListener(this), this);
    }

    private void registerCommands() {
        getCommand("mmorpg").setExecutor(new MmorpgCommand(this));
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("skills").setExecutor(new SkillsCommand(this));
    }

    private void startAutoSaveTask() {
        long interval = 20L * 60 * 5; // Every 5 minutes
        new BukkitRunnable() {
            @Override
            public void run() {
                getLogger().info("Auto-saving player skill data...");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    getSkillManager().savePlayerData(player);
                }
                getLogger().info("Auto-save complete.");
            }
        }.runTaskTimer(this, interval, interval);
    }

    // Static access to plugin
    public static MMORPGCore getInstance() {
        return instance;
    }

    // API + Manager Getters
    public boolean isLibsDisguisesEnabled() { return libsDisguisesEnabled; }
    public EffectManager getEffectManager() { return effectManager; }

    public ItemManager getItemManager() { return itemManager; }
    public StatsManager getStatsManager() { return statsManager; }
    public HealthManager getHealthManager() { return healthManager; }
    public DamageManager getDamageManager() { return damageManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public SkillManager getSkillManager() { return skillManager; }
    public GUIManager getGuiManager() { return guiManager; }
    public RecipeManager getRecipeManager() { return recipeManager; }
    public MobManager getMobManager() { return mobManager; }
    public ZoneManager getZoneManager() { return zoneManager; }
    public AbilityManager getAbilityManager() { return abilityManager; }
    public LootManager getLootManager() { return lootManager;  }
}

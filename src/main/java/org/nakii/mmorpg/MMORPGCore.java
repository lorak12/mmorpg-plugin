package org.nakii.mmorpg;

import org.bukkit.plugin.java.JavaPlugin;
import org.nakii.mmorpg.commands.MmorpgCommand;
import org.nakii.mmorpg.commands.SkillsCommand;
import org.nakii.mmorpg.commands.StatsCommand;
import org.nakii.mmorpg.listeners.*;
import org.nakii.mmorpg.managers.*;

import java.io.File;
import java.sql.SQLException;

public final class MMORPGCore extends JavaPlugin {


    private static MMORPGCore instance; // NEW

    // Manager Instances
    private ItemManager itemManager;
    private StatsManager statsManager;
    private HealthManager healthManager;
    private DamageManager damageManager;
    private DatabaseManager databaseManager;
    private SkillManager skillManager;
    private GUIManager guiManager;
    private RecipeManager recipeManager;


    @Override
    public void onEnable() {

        instance = this; // NEW

        // First, create configuration files and folders
        saveDefaultConfig();
        createItemsFolder();

        try {
            // Initialize Managers in order of dependency
            this.databaseManager = new DatabaseManager(this);
            databaseManager.connect();

            this.skillManager = new SkillManager(this);
            this.itemManager = new ItemManager(this);
            this.statsManager = new StatsManager(this);
            this.healthManager = new HealthManager(this);
            this.damageManager = new DamageManager(this);
            this.guiManager = new GUIManager(this);
            this.recipeManager = new RecipeManager(this); // Initialize last

            // Register Listeners
            registerListeners();

            // Register Commands
            registerCommands();

        } catch (SQLException e) {
            getLogger().severe("Could not connect to the database! The plugin will be disabled.");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("MMORPGCore has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Disconnect from the database safely
        if(databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info("MMORPGCore has been disabled.");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new EntitySpawningListener(this), this);
        getServer().getPluginManager().registerEvents(new MiningListener(this), this);
        getServer().getPluginManager().registerEvents(new FarmingListener(this), this);
        getServer().getPluginManager().registerEvents(new SmeltingListener(this), this);
        getServer().getPluginManager().registerEvents(new CraftingListener(this), this);
        getServer().getPluginManager().registerEvents(new AlchemyListener(this), this);
        getServer().getPluginManager().registerEvents(new FishingListener(this), this);
        getServer().getPluginManager().registerEvents(new MagicListener(this), this);
        getServer().getPluginManager().registerEvents(new ForgingListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new RecipeListener(this), this);
    }

    private void registerCommands() {
        this.getCommand("mmorpg").setExecutor(new MmorpgCommand(this));
        this.getCommand("stats").setExecutor(new StatsCommand(this));
        this.getCommand("skills").setExecutor(new SkillsCommand(this));
    }

    private void createItemsFolder() {
        File itemsFolder = new File(getDataFolder(), "items");
        if (!itemsFolder.exists()) {
            if (itemsFolder.mkdirs()) {
                saveResource("items/example_sword.yml", false);
            }
        }
    }

    // NEW STATIC GETTER
    public static MMORPGCore getInstance() {
        return instance;
    }

    // Manager Getters
    public ItemManager getItemManager() { return itemManager; }
    public StatsManager getStatsManager() { return statsManager; }
    public HealthManager getHealthManager() { return healthManager; }
    public DamageManager getDamageManager() { return damageManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public SkillManager getSkillManager() { return skillManager; }
    public GUIManager getGuiManager() { return guiManager; }
    public RecipeManager getRecipeManager() { return recipeManager; }
}
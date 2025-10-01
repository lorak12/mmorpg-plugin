### **The Project Briefing Template**

**Subject: Project Onboarding for MMORPG Minecraft Plugin**

Hello. I need you to act as a senior developer helping me with my ongoing project. Before we begin, you must understand the project's architecture, technical stack, and coding conventions. Read this entire document carefully and confirm you understand it before we proceed.

**1. Project Overview & Technical Stack**

*   **Project:** A comprehensive, MMORPG-style plugin for Minecraft.
*   **Server Software:** Paper 1.21+
*   **Language:** Java 21
*   **Build Tool:** Gradle with `build.gradle.kts`

**2. Core Architectural Principles**

*   **Manager-Based Architecture:** The plugin is divided into distinct managers (e.g., `ItemManager`, `StatsManager`), each with a single responsibility.
*   **Data-Driven Design:** All gameplay content (items, mobs, recipes, bank tiers) is defined in `.yml` files. The Java code is the engine that reads and processes this data. We avoid hardcoding values.
*   **Event-Driven for Decoupling:** We use custom Bukkit events (e.g., `PlayerBalanceChangeEvent`) to communicate between managers. This keeps them independent.
*   **NBT for Custom Data:** All custom data on `ItemStack`s is stored in the Persistent Data Container (NBT). The `ItemLoreGenerator` reads this data to create the visual lore. Data and presentation are separate.
*   **Paper API & MiniMessage:** We exclusively use the modern Paper API and the Adventure library's MiniMessage format for all text. **No legacy `§` color codes are used.** Item names are non-italic by default.

**3. Key File Structures**

*   **`org.nakii.mmorpg.managers`:** Contains all core logic controllers.
*   **`org.nakii.mmorpg.listeners`:** Contains all Bukkit event listeners.
*   **`org.nakii.mmorpg.item`, `mob`, `player`, `economy`, `crafting`:** These packages contain the data objects (POJOs/Records) for their respective systems.
*   **Resource Folders:** Content is loaded from `resources/items/`, `resources/mobs/`, `resources/recipes/`, etc.

**4. Critical Code Snippets (The "Ground Truth")**

This is the most important part. This is how our core systems are built.

*   **The `Stat` Enum (The language of our stats):**
    ```java
    // in org.nakii.mmorpg.player.Stat.java
    public enum Stat {
        HEALTH("❤ Health", "❤"),
        DEFENSE("❈ Defense", "❈"),
        STRENGTH("❁ Strength", "❁"),
        DAMAGE("⚔ Damage", "⚔"),
        // ... and all others
    
        public String getSimpleName() { /* ... */ }
    }
    ```

*   **The `PlayerStats` Data Object:**
    ```java
    // in org.nakii.mmorpg.player.PlayerStats.java
    public class PlayerStats {
        private final Map<Stat, Double> stats = new EnumMap<>(Stat.class);
        public double getStat(Stat stat) { /* ... */ }
        public void setStat(Stat stat, double value) { /* ... */ }
        public void addStat(Stat stat, double amount) { /* ... */ }
        // Convenience getters like getHealth(), getStrength(), etc.
    }
    ```

*   **The `StatsManager` Engine (`recalculateStats`):**
    ```java
    // in org.nakii.mmorpg.managers.StatsManager.java
    public void recalculateStats(Player player) {
        PlayerStats finalStats = new PlayerStats();
        // 1. Apply base stats
        // 2. Apply skill bonuses
        // 3. Loop through EQUIPPED ARMOR and apply stats from NBT.
        // 4. Check HELD ITEM (if not armor) and apply stats from NBT.
        // 5. Apply armor set bonuses.
        // 6. Apply timed buffs.
        // 7. Cache the final PlayerStats object and apply to the Bukkit player.
    }
    ```

*   **The `ItemManager` and NBT Keys:**
    ```java
    // in org.nakii.mmorpg.managers.ItemManager.java
    public class ItemManager {
        public static final NamespacedKey ITEM_ID_KEY = new NamespacedKey(MMORPGCore.getInstance(), "item_id");
        public static final NamespacedKey BASE_STATS_KEY = new NamespacedKey(MMORPGCore.getInstance(), "base_stats");
        // ... and other keys

        public ItemStack createItemStack(String itemId) { /* ... */ }
    }
    ```

*   **Example `items.yml` Structure:**
    ```yaml
    LIVID_DAGGER:
      material: DIAMOND_SWORD
      display-name: "Livid Dagger"
      rarity: LEGENDARY
      custom-model-data: 10001
      stats:
        DAMAGE: 160
        STRENGTH: 60
      requirements:
        - "SKILL:COMBAT:20"
    ```
### Part 5: Data Holding Classes (The "Blueprints")

These are the Java objects (POJOs or Records) that hold the parsed data from your YAML files. They are the in-memory representation of your game's content.

*   **`player/PlayerStats.java`**: Holds the **final, calculated stats** for a player after all bonuses (items, skills, buffs) have been applied. This is a temporary object, recalculated frequently by the `StatsManager`.
*   **`player/PlayerSkillData.java`**: Holds a player's **persistent skill progress**. It contains a map of each `Skill` to its total accumulated XP and current level. This is loaded from the database on join.
*   **`player/PlayerSlayerData.java`**: Holds a player's **persistent slayer progress**. It contains maps for each slayer type's total XP, current level, and highest boss tier defeated. Loaded from the database.
*   **`player/PlayerCollectionData.java`**: Holds a player's **persistent collection progress**. It contains a map of each collection ID (e.g., "CACTUS") to the total amount collected. Loaded from the database.
*   **`item/CustomItemTemplate.java`**: A **blueprint** for a custom item, loaded from `items/*.yml`. It contains the item's `material`, `rarity`, base `stats`, `requirements`, etc.
*   **`mob/CustomMobTemplate.java`**: A **blueprint** for a custom mob, loaded from `mobs/*.yml`. It contains the mob's `type`, `level`, `category`, base `stats`, `equipment`, and `loot-table`.
*   **`slayer/ActiveSlayerQuest.java`**: A **temporary data object** holding the state of a player's *currently active* slayer quest (e.g., current Combat XP progress, state, active boss entity). This is saved to the database on quit.
*   **`economy/PlayerEconomy.java`**: Holds a player's **persistent economic data**, including their purse/bank balance and bank account tier. Loaded from the database.

---

### Part 6: The `DatabaseManager` Explained

The `DatabaseManager` is the **sole authority on data persistence**. Its only job is to save and load the data-holding classes listed above to and from the `player_data.db` (SQLite) file.

*   **Single Responsibility:** It knows **nothing** about game logic. It doesn't know what a "level up" is or how damage is calculated. It only knows how to take a `PlayerSkillData` object and write its contents to the `player_skills` table, and vice-versa.
*   **Structure:** It uses separate, normalized tables for each type of data (`player_skills`, `player_slayers`, `player_collections`, `player_economy`, etc.). This is efficient and keeps the data organized.
*   **Interface:** All other managers (like `SkillManager`, `SlayerManager`) communicate with the `DatabaseManager` through its public methods (`savePlayerSkillData`, `loadPlayerSkillData`, etc.). This decouples the game logic from the storage mechanism. If you ever wanted to switch from SQLite to MySQL, you would only need to change the code inside the `DatabaseManager`, and the rest of the plugin would continue to work perfectly.

---

### Part 7: Example Configuration Files

These files are all designed to be interconnected, demonstrating how the systems work together.

#### `defaults.yml` (For Vanilla Item Formatting)
```yaml
# Maps vanilla materials to a default rarity when they are first obtained.
# Any material not listed here defaults to COMMON.
DIAMOND: RARE
EMERALD: RARE
GOLD_INGOT: UNCOMMON
IRON_INGOT: UNCOMMON
NETHERITE_INGOT: EPIC
NETHER_STAR: LEGENDARY
ROTTEN_FLESH: COMMON
BONE: COMMON
COAL: COMMON
```

#### `levels.yml` (Universal Skill Progression)
```yaml
# Defines the total (cumulative) XP required to reach a given level for any skill.
levels:
  0: { cumulative_xp: 0, coins: 0 }
  1: { cumulative_xp: 50, coins: 100 }
  2: { cumulative_xp: 125, coins: 250 }
  3: { cumulative_xp: 200, coins: 500 }
  4: { cumulative_xp: 300, coins: 750 }
  5: { cumulative_xp: 500, coins: 1000 }
  # ... and so on up to level 60
```

#### `skills.yml` (Skill-Specific Properties)
```yaml
FARMING:
  display-name: "<green>Farming"
  icon: "WHEAT"
  max-level: 60
  rewards-per-level: { HEALTH: 2, FARMING_FORTUNE: 0.5 }
  milestone-rewards:
    5: ["RECIPE_UNLOCK:FARMER_BOOTS", "COINS:1000"]
  xp-sources: # XP gained for performing an action
    WHEAT: 6
    PUMPKIN: 12

COMBAT:
  display-name: "<red>Combat"
  icon: "IRON_SWORD"
  max-level: 60
  rewards-per-level: { CRIT_CHANCE: 0.5 }
  milestone-rewards:
    10: ["RECIPE_UNLOCK:CLEAVER", "COINS:5000"]
```

#### `items.yml` (Custom Items)
```yaml
# A custom crafting material
ENCHANTED_BONE:
  material: BONE
  display-name: "<green>Enchanted Bone"
  rarity: UNCOMMON

# A powerful weapon with requirements
UNDEAD_SWORD:
  material: STONE_SWORD
  display-name: "<blue>Undead Sword"
  rarity: RARE
  stats:
    DAMAGE: 50
    STRENGTH: 10
  # This item cannot be used unless the player has unlocked Combat Level 5
  requirements:
    - "SKILL:COMBAT:5"
```

#### `recipes.yml` (Custom Crafting Recipes)
```yaml
UNDEAD_SWORD_RECIPE:
  # The item this recipe produces
  result: "UNDEAD_SWORD"
  # The shape of the recipe in the crafting grid
  shape:
    - " E "
    - " E "
    - " S "
  # The ingredients required for each character in the shape
  ingredients:
    E: "ENCHANTED_BONE"
    S: "STICK"
  # This recipe cannot be crafted unless the player has unlocked the Bone Collection Tier II
  requirements:
    - "COLLECTION:BONE:2"
```

#### `mobs.yml` (Standard Custom Mobs)
```yaml
CRYPT_GHOUL:
  type: ZOMBIE
  display-name: "<gray>Crypt Ghoul"
  level: 8
  category: "ZOMBIE" # This is used by the SlayerManager
  stats:
    HEALTH: 200
    DAMAGE: 35
  loot-table:
    # Guaranteed common drop
    - { item: "ROTTEN_FLESH", quantity: "1-3", chance: 1.0, magic-find: false }
    # Rare custom drop
    - { item: "ENCHANTED_BONE", quantity: "1", chance: 0.05, magic-find: true }
```

#### `slayer_bosses.yml` (Boss Base Templates)
```yaml
# These are the base blueprints. Stats are overridden by slayers.yml.
REVENANT_HORROR_1:
  type: ZOMBIE
  display-name: "<red>Revenant Horror</red>"
  level: 10
  category: "ZOMBIE_BOSS" # A unique category to distinguish bosses
  stats: { HEALTH: 500, DAMAGE: 15 }
  equipment: { main_hand: "IRON_SWORD" }
```

#### `slayers.yml` (Slayer Progression and Boss Tiers)
```yaml
ZOMBIE_SLAYER:
  display-name: "<red>Revenant Horror"
  icon: "ZOMBIE_HEAD"
  target-category: "ZOMBIE"
  leveling-xp: { 1: 5, 2: 20 } # Total Slayer XP required
  rewards:
    1: ["RECIPE_UNLOCK:REVENANT_FALCHION", "STAT_BOOST:HEALTH:2"]
  tiers:
    1:
      level: 10
      start-cost: 2000
      xp-to-spawn: 150 # Combat XP
      requirements: []
      slayer-xp-reward: 5 # Slayer XP
      boss:
        id: "REVENANT_HORROR_1"
        health: 500 # Overrides the template health
        damage: 15
        abilities: ["LIFE_DRAIN"]
        loot:
          guaranteed: ["REVENANT_FLESH:1-2"]
          rare-pool: [{"SCYTHE_BLADE": 0.1}, {"BEHEADED_HORROR": 0.5}]
```

#### `collections/combat/bone.yml` (A Specific Collection)
```yaml
id: "BONE"
display-name: "<white>Bone"
material: "BONE"
category: "COMBAT"
tiers:
  1: { required: 100, rewards: ["SKILL_XP:COMBAT:100"], skill-xp: 2 }
  2: { required: 250, rewards: ["RECIPE_UNLOCK:ENCHANTED_BONE"], skill-xp: 3 }
```

#### `zones/the_crypts.yml` (An Example Zone)
```yaml
id: "the_crypts"
display-name: "<dark_gray>The Crypts"
bounds: { min-y: 20, max-y: 50, points: ["0,0", "100,0", "100,100", "0,100"] }
flags:
  mob-spawning:
    spawn-cap: 30
    mobs:
      CRYPT_GHOUL: 10 # This mob is 10x more likely to spawn
      SKELETON: 1     # Vanilla skeletons can also spawn here
```

#### `enchantments.yml` (Custom Enchantments)
```yaml
GROWTH:
  display-name: "Growth"
  max-level: 7
  # This enchantment directly boosts stats.
  stat-bonuses:
    HEALTH: 15 # +15 Health per level
  # What item types it can be applied to.
  valid-items: ["HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"]

LIFE_STEAL:
  display-name: "Life Steal"
  max-level: 5
  # This enchantment uses custom Java logic, identified by this key.
  # The EnchantmentEffectManager maps this key to the LifeStealEffect.java class.
  custom-logic-key: "life_steal"
  valid-items: ["SWORD", "AXE"]
```

#### `bank.yml` (Bank Tiers and Interest)
```yaml
STARTER_ACCOUNT:
  max-balance: 50000000
  interest-tranches:
    - { up-to: 10000000, rate: 0.02 } # 2% interest on the first 10M
    - { up-to: 50000000, rate: 0.01 } # 1% interest on the next 40M
  upgrade-cost: 0
  
GOLD_ACCOUNT:
  max-balance: 100000000
  interest-tranches:
    - { up-to: 20000000, rate: 0.025 }
  upgrade-cost: 5000000
  requirements:
    - "COLLECTION:GOLD_INGOT:5"
```

Here are the most critical systems and managers that still need to be explained:

### 1. The Core Engine: `MMORPGCore.java` and Key Managers

This is the heart of the plugin's operation.

*   **`MMORPGCore.java` (The Central Hub):**
    *   **Purpose:** This is the main plugin class that starts and stops everything.
    *   **Explanation Needed:** How does it ensure the correct startup order? The principle of **dependency injection** is key here. It must initialize managers in a specific sequence (e.g., `DatabaseManager` first, then `ItemManager` and `SkillManager`, and finally `StatsManager` which depends on the others) to prevent errors. Its `onEnable` method is the master blueprint for the entire plugin's launch sequence.

*   **`StatsManager.java` (The Player's Heartbeat):**
    *   **Purpose:** This is arguably the most important manager. It is responsible for calculating a player's final stats at any given moment.
    *   **Explanation Needed:** The `recalculateStats(player)` method is the core logic. A full explanation would detail the **order of operations**:
        1.  Start with base stats.
        2.  Add permanent bonuses from the `SkillManager` (`rewards-per-level`).
        3.  Add permanent bonuses from the `RewardManager` (`STAT_BOOST` rewards).
        4.  Loop through the player's equipped armor and held item, reading the base stats and reforge stats from their NBT tags.
        5.  Check for and apply full-set armor bonuses.
        6.  Apply any temporary buffs (e.g., from potions or abilities).
        7.  Finally, cache this final `PlayerStats` object and apply the core attributes (like Health) to the Bukkit `Player` entity.

*   **`ItemManager` vs. `ItemLoreGenerator` (Data vs. Presentation):**
    *   **Purpose:** This pair perfectly demonstrates a core architectural principle.
    *   **Explanation Needed:**
        *   `ItemManager` is a **factory**. Its job is to load templates from `.yml` files and build an `ItemStack`. When it builds the item, it writes all the important data (its ID, stats, rarity, requirements) to the item's **NBT (Persistent Data Container)**. It does not care what the item *looks* like.
        *   `ItemLoreGenerator` is an **artist**. Its only job is to take an `ItemStack`, read the data from its NBT, and generate the beautiful, formatted lore that the player sees. This separation is crucial: you can change the entire visual style of your item lore just by editing the `ItemLoreGenerator`, without ever touching the item's actual data.

### 2. The Gameplay Loop: Listeners and Event Flow

This explains how the "engine" connects to the live game world.

*   **The Listener Philosophy (Thin Listeners):**
    *   **Purpose:** Listeners should be "thin." They should contain very little logic themselves.
    *   **Explanation Needed:** Their primary role is to act as **dispatchers**. A listener intercepts a Bukkit event (like `EntityDeathEvent`), gathers the necessary context (the player, the mob), and then calls the appropriate manager (like `PlayerDamageListener` calling `LootManager` and `SkillManager`). This keeps your logic centralized in the managers and makes the listeners clean and easy to read.

*   **`PlayerDamageListener.java` (The Central Station for Combat):**
    *   **Purpose:** This is the most complex and important listener.
    *   **Explanation Needed:** A full explanation would trace a single hit:
        1.  An `EntityDamageByEntityEvent` is fired.
        2.  The listener gets the attacker and victim.
        3.  It calls `StatsManager` to get the stats for both.
        4.  It sends these stats to the `DamageManager` (a pure calculation service) to get the final damage number.
        5.  It sets the event's final damage.
        6.  If the hit is fatal, its `onEntityDeath` handler then calls `LootManager` to calculate drops and `SkillManager` to grant Combat XP.

*   **The GUI Framework (`AbstractGui` & `GUIListener`):**
    *   **Purpose:** This is the foundation for all your custom menus.
    *   **Explanation Needed:** How does the system work?
        1.  A command (`/slayer`, `/skills`) creates a new instance of a GUI class (e.g., `new SlayerGui(...)`).
        2.  The `open()` method in `AbstractGui` creates the `Inventory` and registers itself in a static map (`OPEN_GUIS`).
        3.  A single, global `GUIListener` listens for all `InventoryClickEvent`s.
        4.  When a click happens, it checks the static map to see if the player has a custom GUI open.
        5.  If they do, it gets the specific `AbstractGui` object and calls that object's unique `handleClick` method. This is a powerful pattern that centralizes all GUI click handling.

Here is a detailed explanation of those systems and other key areas designed for easy expansion.

---

### 1. The `ScoreboardProvider` Interface

This is one of the most powerful decoupling patterns in the plugin. It allows any system to temporarily display contextual information on the scoreboard without the `ScoreboardManager` needing to know anything about that system.

#### What it is:
A simple `interface` with one method: `List<String> getScoreboardLines(Player player);`. Any manager that wants to show information on the scoreboard (like `SlayerManager`) must implement this interface.

#### How to Use It:
The process is a simple three-step lifecycle:

**Step 1: Become the Provider**
When your system starts for a player (e.g., a Slayer quest begins), you tell the `ScoreboardManager` that you are now in control of the dynamic section of the scoreboard.

```java
// In SlayerManager.startQuest(player, ...)
plugin.getScoreboardManager().setActiveProvider(player, this);
```
*   `this` refers to the `SlayerManager` instance, which implements `ScoreboardProvider`. The `ScoreboardManager` now knows to ask the `SlayerManager` for its lines.

**Step 2: Provide the Lines**
Whenever `ScoreboardManager.updateScoreboard(player)` is called, it checks if that player has an active provider. If it does, it calls the `getScoreboardLines` method on that provider.

```java
// In SlayerManager.java
@Override
public List<String> getScoreboardLines(Player player) {
    ActiveSlayerQuest quest = getActiveSlayerQuest(player);
    if (quest == null) return List.of(); // Return an empty list if there's nothing to show

    // ... (logic to check quest state: GATHERING_XP, BOSS_FIGHT, etc.) ...

    // Return the formatted lines. The ScoreboardManager will display them.
    return List.of(
        "<white>Slayer Quest</white>",
        bossName,
        "<red><b>SLAY THE BOSS</b></red>"
    );
}
```

**Step 3: Relinquish Control**
When your system is finished for the player (e.g., the Slayer quest ends), you must tell the `ScoreboardManager` to clear the provider.

```java
// In SlayerManager.endQuest(player)
plugin.getScoreboardManager().clearActiveProvider(player);
```
*   The scoreboard will immediately update, and the dynamic section will disappear until another system takes control.

**Why it's flexible:** The `ScoreboardManager` has zero knowledge of Slayers, Dungeons, or any other system. To add a new contextual display (e.g., a "Dungeon Run" provider), you just implement the interface in your `DungeonManager` and call the `set`/`clear` methods. You never need to touch the `ScoreboardManager`'s code.

---

### 2. GUI Blueprints: The `AbstractGui` Framework

This system is the blueprint for creating any custom inventory menu in the plugin. It handles all the complex, repetitive boilerplate code so you can focus only on the content and logic of your specific menu.

#### What it Provides (The "Free" Features):
*   **Inventory Management:** It automatically creates and manages the `Inventory` object.
*   **Centralized Click Handling:** A single `GUIListener` listens for all clicks. If the inventory is a custom one, it forwards the event to that specific GUI object's `handleClick` method.
*   **Pagination:** It has built-in methods (`nextPage()`, `previousPage()`) and logic to draw navigation buttons, which you can use in any GUI that needs multiple pages.
*   **Helper Methods:** Provides `createItem()` to easily create named and lored `ItemStack`s, and `drawBaseLayout()` for a standard border.

#### How to Create a New GUI (The Blueprint):
Creating a brand-new, fully functional GUI is a simple three-step process:

**Step 1: Extend `AbstractGui`**
Create your new class, for example, `ReforgeGui.java`.

```java
public class ReforgeGui extends AbstractGui {
    public ReforgeGui(MMORPGCore plugin, Player player) {
        super(plugin, player);
    }
    // ... implement required methods ...
}
```

**Step 2: Implement the Three Abstract Methods**
These are the only methods you *must* implement to define your GUI's appearance.

*   **`public String getTitle()`**: Return the title that appears at the top of the inventory window.
*   **`public int getSize()`**: Return the number of slots for the inventory (must be a multiple of 9). This can be dynamic, as we saw in the `SlayerGui`.
*   **`public void populateItems()`**: This is the most important method. Here, you use `inventory.setItem(slot, itemStack)` to place all your buttons, icons, and information panels.

**Step 3: (Optional) Override `handleClick`**
This method defines what happens when a player clicks inside your GUI.

```java
@Override
public void handleClick(InventoryClickEvent event) {
    event.setCancelled(true); // Always cancel the click in a menu
    
    // Check which item was clicked
    if (event.getSlot() == 22) {
        // Player clicked the "Confirm Reforge" button
        player.sendMessage("Reforging item!");
        player.closeInventory();
    } else {
        // Handle other buttons or fall back to the parent for page/close clicks
        super.handleClick(event);
    }
}
```

**How to open your new GUI:**
From any command or listener, it's just one line: `new ReforgeGui(plugin, player).open();`

---

### 3. Other Highly Flexible Systems

#### The `Requirement` System
This is a prime example of an easily extensible system. To add a completely new type of requirement (e.g., checking if a player has a certain amount of money), you only need to:
1.  Create a new class `MoneyRequirement implements Requirement`.
2.  Add one line to the `switch` statement in the `Requirement.fromString()` static factory method: `case "MONEY" -> new MoneyRequirement(value);`.
    The rest of the plugin (items, recipes, zones) will instantly support this new requirement type without any further changes.

#### The `RewardManager`
This works just like the `Requirement` system. To add a new reward type (e.g., to run a command), you just add a new `case "COMMAND":` to the `switch` statement in the `grantRewards` method.

#### Data-Driven Design (YAML Files)
This is the most powerful flexibility in the entire project. Adding new content almost never requires writing new Java code.
*   **Want a new sword?** Add a new entry in an `items.yml` file.
*   **Want a new collection?** Add a new `collection_name.yml` file.
*   **Want to rebalance all skill XP?** Edit the single `levels.yml` file.

This architectural approach—using decoupled managers, powerful interfaces like `ScoreboardProvider`, and strict data-driven content—is what makes the codebase robust, maintainable, and incredibly flexible for future expansion.


---
**Please read and internalize this information. Do not suggest any code yet. Simply confirm that you have understood the project's architecture and are ready to proceed.**
---
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

---
**Please read and internalize this information. Do not suggest any code yet. Simply confirm that you have understood the project's architecture and are ready to proceed.**
---
# Contributing to the MMORPG Project

Welcome, and thank you for your interest in contributing! This document serves as the primary guide for new developers joining the project. Its goal is to explain our architecture, coding practices, and overall philosophy to help you get up to speed quickly and contribute effectively.

## Project Philosophy

Our goal is to create a deeply engaging, MMORPG-style experience within Minecraft. To achieve this, we follow a few core principles:

1.  **Data-Driven Design:** As much as possible, gameplay values (item stats, mob health, recipe ingredients, etc.) should be defined in `.yml` configuration files, not hardcoded in Java. This allows for rapid balancing and content creation without needing to recompile the plugin.
2.  **Manager-Based Architecture:** The plugin is divided into distinct, specialized "Managers" (e.g., `ItemManager`, `StatsManager`, `SlayerManager`). Each manager has a single, clearly defined responsibility. This keeps the codebase organized and prevents classes from becoming bloated.
3.  **Decoupling with Events:** When one manager needs to cause a change in another, we prefer to use custom Bukkit events (e.g., `PlayerBalanceChangeEvent`). The first manager *fires* an event, and the second manager *listens* for it. This keeps the managers from being tightly coupled and makes the code easier to debug and extend.
4.  **Modern, Paper-First API:** We are building for **Paper 1.21+** and **Java 21**. We exclusively use the modern Adventure library for all text components (MiniMessage format) and prioritize using Paper-specific APIs (like the Dialog API) over older Bukkit methods.

## File Structure

The project is organized into a standard Maven/Gradle structure. The most important part is the `org.nakii.mmorpg` package, which is structured as follows:

```
org/nakii/mmorpg/
├── MMORPGCore.java        // The main plugin class. Initializes all systems.
├── commands/              // All player and admin command classes.
├── crafting/              // Classes related to the custom crafting system (e.g., CustomRecipe).
├── economy/               // Data objects for the economy (PlayerEconomy, Transaction).
├── enchantment/           // The core enchantment system.
│   └── effects/           // Individual logic classes for each custom enchantment effect.
├── events/                // Definitions for all our custom Bukkit events.
├── guis/                  // All custom inventory GUI classes (BankGui, CraftingGui, etc.).
├── item/                  // Data objects for the item system (CustomItemTemplate, Rarity).
├── listeners/             // All Bukkit event listeners (PlayerDamageListener, GUIListener, etc.).
├── managers/              // The core of the plugin. Each manager handles a specific system.
├── mob/                   // Data objects for the mob system (CustomMobTemplate).
├── player/                // Data objects related to a player's state (PlayerStats, Stat enum).
├── requirements/          // The modular requirement system for items/crafting.
├── scoreboard/            // The PlayerScoreboard helper class.
├── slayer/                // Classes for the Slayer quest system.
└── utils/                 // Utility classes (ChatUtils, ItemBuilder, etc.).
```

## Core Systems Explained

Understanding how our main systems interact is key to understanding the project.

#### The Data Flow: from Config to Player

1.  **Configuration (`.yml`):** Everything starts in a `.yml` file (e.g., `items/swords.yml`). Here, we define an item's stats, rarity, and other properties.
2.  **Template Objects:** On startup, a corresponding Manager (e.g., `ItemManager`) loads the YAML and parses it into a "Template" object in memory (e.g., `CustomItemTemplate`). This is the blueprint for the item.
3.  **Instance Creation:** When an item is needed (e.g., from a mob drop), the `ItemManager` uses the template to create a real `ItemStack`.
4.  **NBT Data Storage:** Crucially, all of the item's custom properties (its ID, stats, rarity, requirements) are written to the `ItemStack`'s **Persistent Data Container (NBT)**. The item itself holds all its data.
5.  **Lore Generation:** The `ItemLoreGenerator` reads this NBT data and builds the visual lore for the player. The data and its presentation are kept separate.

#### The Combat & Stats Loop

1.  **Stat Calculation (`StatsManager`):** When a player's gear changes, `StatsManager.recalculateStats()` is called. It gathers stats from all sources: base stats, skill bonuses, and the NBT data of equipped items and their enchantments. The result is a `PlayerStats` object.
2.  **Damage Event (`PlayerDamageListener`):** When a player attacks a mob, the listener triggers.
3.  **Damage Calculation (`DamageManager`):** The listener gets the attacker's `PlayerStats` and passes it to the `DamageManager`, which contains the core mathematical damage formulas.
4.  **Enchantment Effects:** The listener then calls the `EnchantmentEffectManager` to apply any custom logic from enchantments (like `First Strike` or `Cleave`).
5.  **Final Damage:** The final calculated damage is set on the event.

## Coding Practices & Conventions

Please adhere to the following practices to maintain code quality and consistency.

#### 1. Managers are the Single Source of Truth
Never modify another system's data directly. For example, do not try to change a player's purse balance from the `SlayerManager`. Instead, get the `EconomyManager` and call its methods. A manager is the sole gatekeeper for its own system.

#### 2. Use the `Stat` Enum
When referring to player or item stats, always use the `Stat` enum (e.g., `Stat.STRENGTH`), never a raw string like `"STRENGTH"`. This prevents typos and makes the code type-safe.

#### 3. Use MiniMessage for All Text
All player-facing text (chat messages, item names, lore, scoreboard lines, GUI titles) **must** use the MiniMessage format (e.g., `<red>Hello</red>`). Do not use legacy `§` color codes. Use `ChatUtils.format()` to parse these strings into `Component` objects.

#### 4. Null Safety
Prefer using `Optional` for getters that might return nothing (e.g., `template.getArmorSetInfo()`). For methods that can accept a null parameter (like a `Player viewer` for lore generation), use the `@Nullable` annotation.

#### 5. Custom Events for Decoupling
If you are adding a feature where System A needs to trigger an action in System B, consider creating a custom event.
*   **Good:** `BankManager` fires `PlayerBalanceChangeEvent` -> `ScoreboardListener` hears it and updates the scoreboard.
*   **Bad:** `BankManager` directly gets the `ScoreboardManager` and calls its update method.

#### 6. Final Fields
Managers and other dependencies within a class should be declared as `private final`. This makes the code safer and clearly indicates that these dependencies are required and will not be changed after the object is created.

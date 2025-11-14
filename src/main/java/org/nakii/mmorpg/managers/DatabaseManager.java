package org.nakii.mmorpg.managers;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.collection.PlayerCollectionData;
import org.nakii.mmorpg.economy.PlayerEconomy;
import org.nakii.mmorpg.economy.Transaction;
import org.nakii.mmorpg.player.PlayerBonusStats;
import org.nakii.mmorpg.player.Stat;
import org.nakii.mmorpg.skills.PlayerSkillData;
import org.nakii.mmorpg.skills.Skill;
import org.nakii.mmorpg.slayer.ActiveSlayerQuest;
import org.nakii.mmorpg.slayer.PlayerSlayerData;

import java.io.File;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;

public class DatabaseManager {

    private final MMORPGCore plugin;
    private Connection connection;
    private final Gson gson = new Gson();

    // The SlayerManager dependency has been removed to break the circular dependency.
    public DatabaseManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Establishes a connection to the SQLite database and creates necessary tables.
     */
    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }
        File dbFile = new File(plugin.getDataFolder(), "player_data.db");
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        connection = DriverManager.getConnection(url);

        // Create all tables on connection
        createPlayerSkillsTable();
        createPlayerEconomyTable();
        createWorldDataTable();
        createPlayerSlayersTable();
        createActiveQuestsTable();
        createPlayerCollectionsTable();
        createPlayerBonusStatsTable();
        createPlayerQuestDataTable();
    }

    /**
     * Safely disconnects from the database.
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- Table Creation ---

    private void createPlayerSkillsTable() throws SQLException {
        String sql = """
        CREATE TABLE IF NOT EXISTS player_skills (
            uuid TEXT NOT NULL,
            skill TEXT NOT NULL,
            level INTEGER NOT NULL,
            experience REAL NOT NULL,
            PRIMARY KEY (uuid, skill)
        );
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    // --- Player Skill Data ---

    public void savePlayerSkillData(UUID uuid, PlayerSkillData data) throws SQLException {
        String sql = "INSERT OR REPLACE INTO player_skills (uuid, skill, level, experience) VALUES (?, ?, ?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Skill skill : Skill.values()) {
                pstmt.setString(1, uuid.toString());
                pstmt.setString(2, skill.name());
                pstmt.setInt(3, data.getLevel(skill));
                pstmt.setDouble(4, data.getXp(skill));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    public PlayerSkillData loadPlayerSkillData(UUID uuid) throws SQLException {
        PlayerSkillData data = new PlayerSkillData();
        String sql = "SELECT skill, level, experience FROM player_skills WHERE uuid = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Skill skill = Skill.valueOf(rs.getString("skill"));
                data.setLevel(skill, rs.getInt("level"));
                data.setXp(skill, rs.getDouble("experience"));
            }
        }
        return data;
    }

    // --- World Data Section  ---

    private void createWorldDataTable() throws SQLException {
        String sql = """
        CREATE TABLE IF NOT EXISTS world_data (
            key TEXT PRIMARY KEY,
            value INTEGER NOT NULL
        );
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void saveWorldTime(long totalSeconds) throws SQLException {
        String sql = "INSERT OR REPLACE INTO world_data (key, value) VALUES ('world_time', ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, totalSeconds);
            pstmt.executeUpdate();
        }
    }

    public long loadWorldTime() throws SQLException {
        String sql = "SELECT value FROM world_data WHERE key = 'world_time';";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("value");
            }
        }
        return 0; // Default to 0 if no time has ever been saved
    }

    // --- Player Economy Data ---

    private void createPlayerEconomyTable() throws SQLException {
        String sql = """
        CREATE TABLE IF NOT EXISTS player_economy (
            uuid TEXT PRIMARY KEY,
            purse REAL NOT NULL DEFAULT 0,
            bank REAL NOT NULL DEFAULT 0,
            tier TEXT NOT NULL DEFAULT 'STARTER',
            upgrades_unlocked INTEGER NOT NULL DEFAULT 0, -- Store boolean as 0 or 1
            history TEXT -- Store transaction list as a JSON string
        );
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void savePlayerEconomy(UUID uuid, PlayerEconomy economy) throws SQLException {
        String sql = "INSERT OR REPLACE INTO player_economy (uuid, purse, bank, tier, upgrades_unlocked, history) VALUES (?, ?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setDouble(2, economy.getPurse());
            pstmt.setDouble(3, economy.getBank());
            pstmt.setString(4, economy.getAccountTier());
            pstmt.setInt(5, economy.hasUnlockedUpgrades() ? 1 : 0);
            pstmt.setString(6, gson.toJson(economy.getTransactionHistory())); // Serialize history to JSON
            pstmt.executeUpdate();
        }
    }

    public PlayerEconomy loadPlayerEconomy(UUID uuid) throws SQLException {
        String sql = "SELECT purse, bank, tier, upgrades_unlocked, history FROM player_economy WHERE uuid = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double purse = rs.getDouble("purse");
                double bank = rs.getDouble("bank");
                String tier = rs.getString("tier");
                boolean unlocked = rs.getInt("upgrades_unlocked") == 1;
                String historyJson = rs.getString("history");

                Type historyType = new TypeToken<LinkedList<Transaction>>(){}.getType();
                List<Transaction> history = gson.fromJson(historyJson, historyType);

                return new PlayerEconomy(uuid, purse, bank, tier, unlocked, history);
            } else {
                return new PlayerEconomy(uuid); // This is a new player
            }
        }
    }
    // ---- Slayer data -----

    private void createPlayerSlayersTable() throws SQLException {
        String sql = """
        CREATE TABLE IF NOT EXISTS player_slayers (
            uuid TEXT NOT NULL,
            slayer_type TEXT NOT NULL,
            level INTEGER NOT NULL DEFAULT 0,
            experience INTEGER NOT NULL DEFAULT 0,
            highest_tier_defeated INTEGER NOT NULL DEFAULT 0,
            PRIMARY KEY (uuid, slayer_type)
        );
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public PlayerSlayerData loadPlayerSlayerData(Player player) throws SQLException {
        PlayerSlayerData data = new PlayerSlayerData();
        String sql = "SELECT slayer_type, level, experience, highest_tier_defeated FROM player_slayers WHERE uuid = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String slayerType = rs.getString("slayer_type");
                data.setLevel(slayerType, rs.getInt("level"));
                data.setXp(slayerType, rs.getInt("experience"));
                data.setHighestTierDefeated(slayerType, rs.getInt("highest_tier_defeated"));
            }
        }
        return data;
    }

    // This method no longer needs SlayerManager. It iterates over the data it is given.
    public void savePlayerSlayerData(Player player, PlayerSlayerData data) throws SQLException {
        String sql = "INSERT OR REPLACE INTO player_slayers (uuid, slayer_type, level, experience, highest_tier_defeated) VALUES (?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // Iterate over the keys in the data object, not the config file.
            for (String slayerType : data.getSlayerXpMap().keySet()) {
                pstmt.setString(1, player.getUniqueId().toString());
                pstmt.setString(2, slayerType.toUpperCase());
                pstmt.setInt(3, data.getLevel(slayerType));
                pstmt.setInt(4, data.getXp(slayerType));
                pstmt.setInt(5, data.getHighestTierDefeated(slayerType));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private void createActiveQuestsTable() throws SQLException {
        String sql = """
        CREATE TABLE IF NOT EXISTS active_quests (
            uuid TEXT PRIMARY KEY,
            quest_type TEXT NOT NULL,
            tier INTEGER NOT NULL,
            xp_to_spawn INTEGER NOT NULL,
            current_xp REAL NOT NULL
        );
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void saveActiveSlayerQuest(UUID uuid, ActiveSlayerQuest quest) throws SQLException {
        String sql = "INSERT OR REPLACE INTO active_quests (uuid, quest_type, tier, xp_to_spawn, current_xp) VALUES (?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, quest.getSlayerType());
            pstmt.setInt(3, quest.getTier());
            pstmt.setInt(4, quest.getXpToSpawn());
            pstmt.setDouble(5, quest.getCurrentXp());
            pstmt.executeUpdate();
        }
    }

    public Optional<ActiveSlayerQuest> loadActiveSlayerQuest(UUID uuid) throws SQLException {
        String sql = "SELECT quest_type, tier, xp_to_spawn, current_xp FROM active_quests WHERE uuid = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String type = rs.getString("quest_type");
                int tier = rs.getInt("tier");
                int xpToSpawn = rs.getInt("xp_to_spawn");
                double currentXp = rs.getDouble("current_xp");

                ActiveSlayerQuest quest = new ActiveSlayerQuest(type, tier, xpToSpawn);
                quest.setCurrentXp(currentXp);
                return Optional.of(quest);
            }
        }
        return Optional.empty();
    }

    public void deleteActiveSlayerQuest(UUID uuid) throws SQLException {
        String sql = "DELETE FROM active_quests WHERE uuid = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.executeUpdate();
        }
    }

    // --- Player Collections Data ---
    private void createPlayerCollectionsTable() throws SQLException {
        String sql = """
        CREATE TABLE IF NOT EXISTS player_collections (
            uuid TEXT NOT NULL,
            collection_id TEXT NOT NULL,
            amount INTEGER NOT NULL DEFAULT 0,
            PRIMARY KEY (uuid, collection_id)
        );
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public PlayerCollectionData loadPlayerCollectionData(Player player) throws SQLException {
        PlayerCollectionData data = new PlayerCollectionData();
        String sql = "SELECT collection_id, amount FROM player_collections WHERE uuid = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                data.setProgress(rs.getString("collection_id"), rs.getInt("amount"));
            }
        }
        return data;
    }

    public void savePlayerCollectionData(Player player, PlayerCollectionData data) throws SQLException {
        String sql = "INSERT OR REPLACE INTO player_collections (uuid, collection_id, amount) VALUES (?, ?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Map.Entry<String, Integer> entry : data.getCollectionProgressMap().entrySet()) {
                pstmt.setString(1, player.getUniqueId().toString());
                pstmt.setString(2, entry.getKey());
                pstmt.setInt(3, entry.getValue());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    // --- Player Bonus Stats ---
    private void createPlayerBonusStatsTable() throws SQLException {
        String sql = """
        CREATE TABLE IF NOT EXISTS player_bonus_stats (
            uuid TEXT NOT NULL,
            stat TEXT NOT NULL,
            value REAL NOT NULL,
            PRIMARY KEY (uuid, stat)
        );
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public PlayerBonusStats loadPlayerBonusStats(UUID uuid) throws SQLException {
        PlayerBonusStats data = new PlayerBonusStats();
        String sql = "SELECT stat, value FROM player_bonus_stats WHERE uuid = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                try {
                    Stat stat = Stat.valueOf(rs.getString("stat"));
                    data.setBonus(stat, rs.getDouble("value"));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return data;
    }

    public void savePlayerBonusStats(UUID uuid, PlayerBonusStats data) throws SQLException {
        String sql = "INSERT OR REPLACE INTO player_bonus_stats (uuid, stat, value) VALUES (?, ?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Map.Entry<Stat, Double> entry : data.getBonusStatsMap().entrySet()) {
                pstmt.setString(1, uuid.toString());
                pstmt.setString(2, entry.getKey().name());
                pstmt.setDouble(3, entry.getValue());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    // --- Player Quest Data ---
    private void createPlayerQuestDataTable() throws SQLException {
        String sql = """
        CREATE TABLE IF NOT EXISTS player_quest_data (
            uuid TEXT PRIMARY KEY,
            quest_data TEXT NOT NULL
        );
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public String loadPlayerQuestData(UUID uuid) throws SQLException {
        String sql = "SELECT quest_data FROM player_quest_data WHERE uuid = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("quest_data");
            }
        }
        return null;
    }

    public void savePlayerQuestData(UUID uuid, String jsonData) throws SQLException {
        String sql = "INSERT OR REPLACE INTO player_quest_data (uuid, quest_data) VALUES (?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, jsonData);
            pstmt.executeUpdate();
        }
    }
}
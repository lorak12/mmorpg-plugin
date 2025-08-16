package org.nakii.mmorpg.managers;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.economy.PlayerEconomy;
import org.nakii.mmorpg.economy.Transaction;
import org.nakii.mmorpg.skills.PlayerSkillData;
import org.nakii.mmorpg.skills.Skill;

import java.io.File;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {

    private final MMORPGCore plugin;
    private Connection connection;

    private final Gson gson = new Gson();

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

                // --- THE FIX: Call the constructor with the UUID first ---
                return new PlayerEconomy(uuid, purse, bank, tier, unlocked, history);
            } else {
                // --- THE FIX: Call the new player constructor with the UUID ---
                return new PlayerEconomy(uuid); // This is a new player
            }
        }
    }
}
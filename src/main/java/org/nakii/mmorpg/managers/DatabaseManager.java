package org.nakii.mmorpg.managers;

import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.skills.PlayerSkillData;
import org.nakii.mmorpg.skills.Skill;

import java.sql.*;
import java.util.UUID;

public class DatabaseManager {
    private final org.nakii.mmorpg.MMORPGCore plugin;
    private Connection connection;

    public DatabaseManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }
        String url = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/player_data.db";
        connection = DriverManager.getConnection(url);
        createTables();
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // CORRECTED: The primary key is now a combination of uuid and skill.
            String sql = "CREATE TABLE IF NOT EXISTS player_skills (" +
                    "uuid TEXT NOT NULL," +
                    "skill TEXT NOT NULL," +
                    "level INTEGER NOT NULL," +
                    "experience REAL NOT NULL," +
                    "PRIMARY KEY (uuid, skill));";
            statement.execute(sql);
        }
    }


    // Simplified save logic: insert or replace.
    public void savePlayerSkillData(UUID uuid, PlayerSkillData data) throws SQLException {
        String sql = "INSERT OR REPLACE INTO player_skills (uuid, skill, level, experience) VALUES (?, ?, ?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Skill skill : Skill.values()) {
                pstmt.setString(1, uuid.toString());
                pstmt.setString(2, skill.name());
                pstmt.setInt(3, data.getLevel(skill));
                pstmt.setDouble(4, data.getExperience(skill));
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
                data.setExperience(skill, rs.getDouble("experience"));
            }
        }
        return data;
    }
}
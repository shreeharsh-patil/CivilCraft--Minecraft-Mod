package com.civilcraftai.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static HikariDataSource dataSource;

    public static void initialize() throws SQLException {
        // Create database directory if it doesn't exist
        File dbDir = new File("config/civilcraftai");
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:config/civilcraftai/world_state.db");
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(10);
        config.setConnectionTestQuery("SELECT 1;");
        config.addDataSourceProperty("journal_mode", "WAL");
        config.addDataSourceProperty("synchronous", "NORMAL");

        dataSource = new HikariDataSource(config);

        // Execute table creation queries
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Towns Table
            stmt.execute("CREATE TABLE IF NOT EXISTS towns (" +
                    "id TEXT PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "founder_uuid TEXT NOT NULL," +
                    "balance INTEGER DEFAULT 0," +
                    "created_at INTEGER NOT NULL" +
                    ");");

            // Claims Table
            stmt.execute("CREATE TABLE IF NOT EXISTS claims (" +
                    "dimension TEXT NOT NULL," +
                    "chunk_x INTEGER NOT NULL," +
                    "chunk_z INTEGER NOT NULL," +
                    "town_id TEXT," +
                    "PRIMARY KEY (dimension, chunk_x, chunk_z)," +
                    "FOREIGN KEY (town_id) REFERENCES towns(id)" +
                    ");");

            // Agent Memory Table
            stmt.execute("CREATE TABLE IF NOT EXISTS agent_memory (" +
                    "entity_uuid TEXT PRIMARY KEY," +
                    "personality_json TEXT NOT NULL," +
                    "summary TEXT," +
                    "last_interaction INTEGER" +
                    ");");

            // Dialogue Logs Table
            stmt.execute("CREATE TABLE IF NOT EXISTS dialogue_logs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "entity_uuid TEXT NOT NULL," +
                    "speaker_name TEXT NOT NULL," +
                    "message TEXT NOT NULL," +
                    "timestamp INTEGER NOT NULL," +
                    "FOREIGN KEY (entity_uuid) REFERENCES agent_memory(entity_uuid)" +
                    ");");
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new IllegalStateException("DatabaseManager is not initialized.");
        }
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}

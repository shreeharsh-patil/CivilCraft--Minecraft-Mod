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

            // Town Technology Table
            stmt.execute("CREATE TABLE IF NOT EXISTS town_tech (" +
                    "town_id TEXT NOT NULL," +
                    "tech_id TEXT NOT NULL," +
                    "PRIMARY KEY (town_id, tech_id)" +
                    ");");

            // Diplomacy Table
            stmt.execute("CREATE TABLE IF NOT EXISTS diplomacy (" +
                    "town_a TEXT NOT NULL," +
                    "town_b TEXT NOT NULL," +
                    "relation_state TEXT NOT NULL," +
                    "PRIMARY KEY (town_a, town_b)" +
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

    public static void registerAgent(String uuid, String name, String personality) {
        try (Connection conn = getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                     "INSERT OR IGNORE INTO agent_memory (entity_uuid, personality_json, summary, last_interaction) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, uuid);
            com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
            obj.addProperty("name", name);
            obj.addProperty("personality", personality);
            ps.setString(2, obj.toString());
            ps.setString(3, "Initial state of " + name);
            ps.setLong(4, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static com.google.gson.JsonObject getAgentInfo(String uuid) {
        try (Connection conn = getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                     "SELECT personality_json FROM agent_memory WHERE entity_uuid = ?")) {
            ps.setString(1, uuid);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String json = rs.getString("personality_json");
                    return com.google.gson.JsonParser.parseString(json).getAsJsonObject();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveDialogueLog(String uuid, String speaker, String message) {
        try (Connection conn = getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO dialogue_logs (entity_uuid, speaker_name, message, timestamp) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, uuid);
            ps.setString(2, speaker);
            ps.setString(3, message);
            ps.setLong(4, System.currentTimeMillis());
            ps.executeUpdate();

            // Update last interaction timestamp
            try (java.sql.PreparedStatement ps2 = conn.prepareStatement(
                    "UPDATE agent_memory SET last_interaction = ? WHERE entity_uuid = ?")) {
                ps2.setLong(1, System.currentTimeMillis());
                ps2.setString(2, uuid);
                ps2.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getRecentDialogueHistory(String uuid, int limit) {
        StringBuilder builder = new StringBuilder();
        try (Connection conn = getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                     "SELECT speaker_name, message FROM dialogue_logs WHERE entity_uuid = ? ORDER BY timestamp DESC LIMIT ?")) {
            ps.setString(1, uuid);
            ps.setInt(2, limit);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                java.util.List<String> logs = new java.util.ArrayList<>();
                while (rs.next()) {
                    logs.add(rs.getString("speaker_name") + ": " + rs.getString("message"));
                }
                // Reverse logs to make it chronological
                for (int i = logs.size() - 1; i >= 0; i--) {
                    builder.append(logs.get(i)).append("\n");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public static void createTown(String townId, String name, String founderUuid) {
        try (Connection conn = getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO towns (id, name, founder_uuid, balance, created_at) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, townId);
            ps.setString(2, name);
            ps.setString(3, founderUuid);
            ps.setInt(4, 100); // 100 coins initial balance
            ps.setLong(5, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void claimChunk(String dimension, int chunkX, int chunkZ, String townId) {
        try (Connection conn = getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                     "INSERT OR REPLACE INTO claims (dimension, chunk_x, chunk_z, town_id) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, dimension);
            ps.setInt(2, chunkX);
            ps.setInt(3, chunkZ);
            ps.setString(4, townId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getChunkOwnerTownId(String dimension, int chunkX, int chunkZ) {
        try (Connection conn = getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                     "SELECT town_id FROM claims WHERE dimension = ? AND chunk_x = ? AND chunk_z = ?")) {
            ps.setString(1, dimension);
            ps.setInt(2, chunkX);
            ps.setInt(3, chunkZ);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("town_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getTownName(String townId) {
        try (Connection conn = getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                     "SELECT name FROM towns WHERE id = ?")) {
            ps.setString(1, townId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isTownMember(String townId, String playerUuid) {
        try (Connection conn = getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                     "SELECT id FROM towns WHERE id = ? AND founder_uuid = ?")) {
            ps.setString(1, townId);
            ps.setString(2, playerUuid);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean depositCoins(String townId, int amount) {
        try (Connection conn = getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                     "UPDATE towns SET balance = balance + ? WHERE id = ?")) {
            ps.setInt(1, amount);
            ps.setString(2, townId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean withdrawCoins(String townId, int amount) {
        try (Connection conn = getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                     "SELECT balance FROM towns WHERE id = ?")) {
            ps.setString(1, townId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int balance = rs.getInt("balance");
                    if (balance >= amount) {
                        try (java.sql.PreparedStatement psUpdate = conn.prepareStatement(
                                "UPDATE towns SET balance = balance - ? WHERE id = ?")) {
                            psUpdate.setInt(1, amount);
                            psUpdate.setString(2, townId);
                            return psUpdate.executeUpdate() > 0;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getTownBalance(String townId) {
        try (Connection conn = getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                     "SELECT balance FROM towns WHERE id = ?")) {
            ps.setString(1, townId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("balance");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void unlockTech(String townId, String techId) {
        try (Connection conn = getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                     "INSERT OR IGNORE INTO town_tech (town_id, tech_id) VALUES (?, ?)")) {
            ps.setString(1, townId);
            ps.setString(2, techId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean isTechUnlocked(String townId, String techId) {
        try (Connection conn = getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                     "SELECT town_id FROM town_tech WHERE town_id = ? AND tech_id = ?")) {
            ps.setString(1, townId);
            ps.setString(2, techId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void setRelation(String townA, String townB, String state) {
        // Ensure lexicographical order to prevent duplicates like (A, B) and (B, A)
        String first = townA.compareTo(townB) < 0 ? townA : townB;
        String second = townA.compareTo(townB) < 0 ? townB : townA;

        try (Connection conn = getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                     "INSERT OR REPLACE INTO diplomacy (town_a, town_b, relation_state) VALUES (?, ?, ?)")) {
            ps.setString(1, first);
            ps.setString(2, second);
            ps.setString(3, state);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getRelation(String townA, String townB) {
        if (townA.equals(townB)) return "SAME_TOWN";
        String first = townA.compareTo(townB) < 0 ? townA : townB;
        String second = townA.compareTo(townB) < 0 ? townB : townA;

        try (Connection conn = getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                     "SELECT relation_state FROM diplomacy WHERE town_a = ? AND town_b = ?")) {
            ps.setString(1, first);
            ps.setString(2, second);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("relation_state");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "NEUTRAL"; // Default relation
    }
}

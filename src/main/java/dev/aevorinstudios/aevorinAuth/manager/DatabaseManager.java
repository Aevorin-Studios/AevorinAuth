package dev.aevorinstudios.aevorinAuth.manager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.aevorinstudios.aevorinAuth.AevorinAuth;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    private final AevorinAuth plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(AevorinAuth plugin) {
        this.plugin = plugin;
        setupDatabase();
        createTable();
    }

    private void setupDatabase() {
        HikariConfig config = new HikariConfig();
        String type = plugin.getConfig().getString("storage.type", "sqlite").toLowerCase();

        if (type.equals("mysql")) {
            String host = plugin.getConfig().getString("storage.host");
            int port = plugin.getConfig().getInt("storage.port");
            String database = plugin.getConfig().getString("storage.database");
            String username = plugin.getConfig().getString("storage.username");
            String password = plugin.getConfig().getString("storage.password");

            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
            config.setUsername(username);
            config.setPassword(password);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        } else {
            File dbFile = new File(plugin.getDataFolder(), "auth.db");
            config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            config.setDriverClassName("org.sqlite.JDBC");
        }

        config.setMaximumPoolSize(plugin.getConfig().getInt("storage.pool.maximum-pool-size", 10));
        config.setMinimumIdle(plugin.getConfig().getInt("storage.pool.minimum-idle", 10));
        config.setMaxLifetime(plugin.getConfig().getLong("storage.pool.maximum-lifetime", 1800000));
        config.setConnectionTimeout(plugin.getConfig().getLong("storage.pool.connection-timeout", 5000));

        dataSource = new HikariDataSource(config);
    }

    private void createTable() {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS auth_users (" +
                                "uuid VARCHAR(36) PRIMARY KEY," +
                                "password VARCHAR(255) NOT NULL" +
                                ")")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not create users table: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public CompletableFuture<String> getPassword(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection();
                    PreparedStatement stmt = conn.prepareStatement("SELECT password FROM auth_users WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("password");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error getting password for " + uuid + ": " + e.getMessage());
            }
            return null;
        });
    }

    public CompletableFuture<Boolean> isRegistered(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection();
                    PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM auth_users WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error checking registration for " + uuid + ": " + e.getMessage());
            }
            return false;
        });
    }

    public CompletableFuture<Void> register(UUID uuid, String hashedPassword) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                    PreparedStatement stmt = conn
                            .prepareStatement("INSERT INTO auth_users (uuid, password) VALUES (?, ?)")) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, hashedPassword);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error registering user " + uuid + ": " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Void> unregister(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                    PreparedStatement stmt = conn.prepareStatement("DELETE FROM auth_users WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error unregistering user " + uuid + ": " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Void> updatePassword(UUID uuid, String hashedPassword) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                    PreparedStatement stmt = conn
                            .prepareStatement("UPDATE auth_users SET password = ? WHERE uuid = ?")) {
                stmt.setString(1, hashedPassword);
                stmt.setString(2, uuid.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error updating password for user " + uuid + ": " + e.getMessage());
            }
        });
    }
}

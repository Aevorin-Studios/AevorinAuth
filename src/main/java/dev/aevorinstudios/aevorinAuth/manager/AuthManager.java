package dev.aevorinstudios.aevorinAuth.manager;

import dev.aevorinstudios.aevorinAuth.AevorinAuth;
import org.bukkit.ChatColor;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AuthManager {

    private final AevorinAuth plugin;
    private final DatabaseManager databaseManager;
    private final Set<UUID> authenticatedPlayers = new HashSet<>();
    private final Set<UUID> registeredPlayers = new HashSet<>();

    public AuthManager(AevorinAuth plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    public CompletableFuture<Boolean> isRegistered(UUID uuid) {
        return databaseManager.isRegistered(uuid).thenApply(registered -> {
            if (registered)
                registeredPlayers.add(uuid);
            else
                registeredPlayers.remove(uuid);
            return registered;
        });
    }

    public boolean isRegisteredSync(UUID uuid) {
        return registeredPlayers.contains(uuid);
    }

    public CompletableFuture<Boolean> register(UUID uuid, String password) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        return databaseManager.register(uuid, hashedPassword).thenApply(v -> {
            registeredPlayers.add(uuid);
            return true;
        });
    }

    public CompletableFuture<Boolean> checkPassword(UUID uuid, String password) {
        return databaseManager.getPassword(uuid).thenApply(hashedPassword -> {
            if (hashedPassword == null)
                return false;
            return BCrypt.checkpw(password, hashedPassword);
        });
    }

    public CompletableFuture<Void> unregister(UUID uuid) {
        return databaseManager.unregister(uuid).thenRun(() -> {
            setAuthenticated(uuid, false);
            registeredPlayers.remove(uuid);
        });
    }

    public CompletableFuture<Void> changePassword(UUID uuid, String newPassword) {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        return databaseManager.updatePassword(uuid, hashedPassword);
    }

    public void setAuthenticated(UUID uuid, boolean authenticated) {
        if (authenticated) {
            authenticatedPlayers.add(uuid);
        } else {
            authenticatedPlayers.remove(uuid);
        }
    }

    public boolean isAuthenticated(UUID uuid) {
        return authenticatedPlayers.contains(uuid);
    }

    public String getMessage(String path) {
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&bAevorinAuth&8] &r");
        String message = plugin.getConfig().getString("messages." + path);
        if (message == null)
            return "Missing message: " + path;
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }
}

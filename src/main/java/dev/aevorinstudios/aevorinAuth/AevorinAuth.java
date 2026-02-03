package dev.aevorinstudios.aevorinAuth;

import dev.aevorinstudios.aevorinAuth.commands.AuthCommand;
import dev.aevorinstudios.aevorinAuth.commands.ChangePasswordCommand;
import dev.aevorinstudios.aevorinAuth.commands.LoginCommand;
import dev.aevorinstudios.aevorinAuth.commands.RegisterCommand;
import dev.aevorinstudios.aevorinAuth.commands.UnregisterCommand;
import dev.aevorinstudios.aevorinAuth.listeners.AuthListener;
import dev.aevorinstudios.aevorinAuth.manager.AuthManager;
import dev.aevorinstudios.aevorinAuth.manager.AuthProtocolManager;
import dev.aevorinstudios.aevorinAuth.manager.DatabaseManager;
import dev.aevorinstudios.aevorinAuth.utils.ConfigUpdater;
import dev.aevorinstudios.aevorinAuth.utils.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class AevorinAuth extends JavaPlugin implements Listener {

    private DatabaseManager databaseManager;
    private AuthManager authManager;
    private AuthProtocolManager protocolManager;
    private UpdateChecker updateChecker;

    @Override
    public void onEnable() {
        // Safe Config Update
        new ConfigUpdater(this).updateConfig();

        // Save default config (creates if doesn't exist)
        saveDefaultConfig();

        // Update Checker
        updateChecker = new UpdateChecker(this, "bPv33o7a");
        updateChecker.checkForUpdates();

        // Initialize Database
        databaseManager = new DatabaseManager(this);

        // Initialize Manager
        authManager = new AuthManager(this, databaseManager);
        protocolManager = new AuthProtocolManager(this, authManager);
        protocolManager.register();

        // Register Listeners
        getServer().getPluginManager().registerEvents(new AuthListener(this, authManager), this);
        getServer().getPluginManager().registerEvents(this, this);

        // Register Commands
        getCommand("auth").setExecutor(new AuthCommand(this, authManager));
        getCommand("register").setExecutor(new RegisterCommand(this, authManager));
        getCommand("login").setExecutor(new LoginCommand(this, authManager));
        getCommand("unregister").setExecutor(new UnregisterCommand(authManager));
        getCommand("changepassword").setExecutor(new ChangePasswordCommand(authManager));

        getLogger().info("AevorinAuth enabled! Using " + getConfig().getString("storage.type") + " storage.");
    }

    @EventHandler
    public void onAdminJoin(PlayerJoinEvent event) {
        if (event.getPlayer().isOp() || event.getPlayer().hasPermission("aevorinauth.admin")) {
            String notification = updateChecker.getNewVersionNotification();
            if (notification != null) {
                // Send after a short delay so it doesn't get lost in join messages
                dev.aevorinstudios.aevorinAuth.utils.SchedulerUtils.runEntityLater(this, event.getPlayer(), () -> {
                    if (event.getPlayer().isOnline()) {
                        event.getPlayer().sendMessage(notification);
                    }
                }, 40L); // 2 second delay
            }
        }
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public AuthProtocolManager getProtocolManager() {
        return protocolManager;
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }
}

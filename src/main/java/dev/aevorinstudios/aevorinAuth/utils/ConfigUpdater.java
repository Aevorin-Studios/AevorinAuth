package dev.aevorinstudios.aevorinAuth.utils;

import dev.aevorinstudios.aevorinAuth.AevorinAuth;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Set;

public class ConfigUpdater {

    private final AevorinAuth plugin;

    public ConfigUpdater(AevorinAuth plugin) {
        this.plugin = plugin;
    }

    public void updateConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
            return;
        }

        FileConfiguration currentConfig = YamlConfiguration.loadConfiguration(configFile);
        InputStream defaultStream = plugin.getResource("config.yml");
        if (defaultStream == null)
            return;

        FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));

        int currentVersion = currentConfig.getInt("config-version", 0);
        int defaultVersion = defaultConfig.getInt("config-version", 0);

        if (currentVersion >= defaultVersion) {
            return;
        }

        plugin.getLogger().info("Updating configuration file...");

        // 1. Create backup
        File backupFile = new File(plugin.getDataFolder(), "config.yml.old");
        try {
            Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create config backup: " + e.getMessage());
            return;
        }

        try {
            // 2. Merge missing keys
            boolean modified = merge(defaultConfig, currentConfig);

            // 3. Handle structure changes (migration)
            migrate(currentConfig, currentVersion, defaultVersion);

            // Always update the version key
            currentConfig.set("config-version", defaultVersion);

            // 4. Save merged config
            currentConfig.save(configFile);
            plugin.getLogger().info("Configuration updated successfully. Backup saved as config.yml.old");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to update configuration: " + e.getMessage());
            // 5. Restore from backup
            try {
                Files.copy(backupFile.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().info("Restored configuration from backup.");
            } catch (IOException ex) {
                plugin.getLogger().severe("Critical: Could not restore backup! " + ex.getMessage());
            }
        }
    }

    private boolean merge(FileConfiguration internal, FileConfiguration external) {
        boolean modified = false;
        Set<String> keys = internal.getKeys(true);
        for (String key : keys) {
            if (!external.contains(key)) {
                external.set(key, internal.get(key));
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Handle manual structure changes here based on versions.
     */
    private void migrate(FileConfiguration config, int oldVersion, int newVersion) {
        // Example migration:
        // if (oldVersion < 2) {
        // if (config.contains("old-key")) {
        // config.set("new-key", config.get("old-key"));
        // config.set("old-key", null);
        // }
        // }
    }
}

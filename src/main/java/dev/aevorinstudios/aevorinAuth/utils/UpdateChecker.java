package dev.aevorinstudios.aevorinAuth.utils;

import dev.aevorinstudios.aevorinAuth.AevorinAuth;
import org.bukkit.ChatColor;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class UpdateChecker {

    private final AevorinAuth plugin;
    private final String projectId;
    private String latestVersion;

    public UpdateChecker(AevorinAuth plugin, String projectId) {
        this.plugin = plugin;
        this.projectId = projectId;
    }

    public CompletableFuture<String> getLatestVersion() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = URI.create("https://api.modrinth.com/v2/project/" + projectId + "/version").toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "AevorinAuth-UpdateChecker");

                if (connection.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONArray versions = new JSONArray(response.toString());
                    if (versions.length() > 0) {
                        JSONObject latest = versions.getJSONObject(0);
                        this.latestVersion = latest.getString("version_number");
                        return this.latestVersion;
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Could not check for updates: " + e.getMessage());
            }
            return null;
        });
    }

    public void checkForUpdates() {
        getLatestVersion().thenAccept(version -> {
            if (version == null)
                return;

            String currentVersion = plugin.getPluginMeta().getVersion();
            if (!currentVersion.equalsIgnoreCase(version)) {
                plugin.getLogger()
                        .info("A new update is available: v" + version + " (Current: v" + currentVersion + ")");
                plugin.getLogger().info("Download it at: https://modrinth.com/plugin/aevorinauth");
            }
        });
    }

    public String getNewVersionNotification() {
        if (latestVersion == null)
            return null;
        String currentVersion = plugin.getPluginMeta().getVersion();
        if (currentVersion.equalsIgnoreCase(latestVersion))
            return null;

        return ChatColor.GOLD + "[AevorinAuth] " + ChatColor.YELLOW + "A new update is available: " +
                ChatColor.GREEN + "v" + latestVersion + ChatColor.YELLOW + " (Running: v" + currentVersion + ")\n" +
                ChatColor.YELLOW + "Download: " + ChatColor.AQUA + "https://modrinth.com/plugin/aevorinauth";
    }
}

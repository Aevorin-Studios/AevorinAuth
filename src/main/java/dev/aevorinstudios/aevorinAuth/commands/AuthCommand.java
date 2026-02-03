package dev.aevorinstudios.aevorinAuth.commands;

import dev.aevorinstudios.aevorinAuth.AevorinAuth;
import dev.aevorinstudios.aevorinAuth.manager.AuthManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class AuthCommand implements CommandExecutor {

    private final AevorinAuth plugin;
    private final AuthManager authManager;

    public AuthCommand(AevorinAuth plugin, AuthManager authManager) {
        this.plugin = plugin;
        this.authManager = authManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!sender.hasPermission("aevorinauth.admin")) {
            sender.sendMessage(authManager.getMessage("no-permission"));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage(authManager.getMessage("reload-success"));
            return true;
        }

        sender.sendMessage(authManager.getMessage("prefix") + "§eUsage: /auth reload");
        return true;
    }
}

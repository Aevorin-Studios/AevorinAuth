package dev.aevorinstudios.aevorinAuth.commands;

import dev.aevorinstudios.aevorinAuth.manager.AuthManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class UnregisterCommand implements CommandExecutor {

    private final AuthManager authManager;

    public UnregisterCommand(AuthManager authManager) {
        this.authManager = authManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(authManager.getMessage("only-players"));
            return true;
        }

        if (!authManager.isAuthenticated(player.getUniqueId())) {
            player.sendMessage(authManager.getMessage("must-login"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(authManager.getMessage("unregister-usage"));
            return true;
        }

        String password = args[0];

        authManager.checkPassword(player.getUniqueId(), password).thenAccept(correct -> {
            if (correct) {
                authManager.unregister(player.getUniqueId()).thenRun(() -> {
                    player.sendMessage(authManager.getMessage("unregister-success"));
                });
            } else {
                player.sendMessage(authManager.getMessage("wrong-password"));
            }
        });

        return true;
    }
}

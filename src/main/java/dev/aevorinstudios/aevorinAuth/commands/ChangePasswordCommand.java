package dev.aevorinstudios.aevorinAuth.commands;

import dev.aevorinstudios.aevorinAuth.manager.AuthManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChangePasswordCommand implements CommandExecutor {

    private final AuthManager authManager;

    public ChangePasswordCommand(AuthManager authManager) {
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

        if (args.length != 2) {
            player.sendMessage(authManager.getMessage("changepassword-usage"));
            return true;
        }

        String oldPassword = args[0];
        String newPassword = args[1];

        authManager.checkPassword(player.getUniqueId(), oldPassword).thenAccept(correct -> {
            if (correct) {
                authManager.changePassword(player.getUniqueId(), newPassword).thenRun(() -> {
                    player.sendMessage(authManager.getMessage("password-change-success"));
                });
            } else {
                player.sendMessage(authManager.getMessage("wrong-password"));
            }
        });

        return true;
    }
}

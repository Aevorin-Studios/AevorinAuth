package dev.aevorinstudios.aevorinAuth.commands;

import dev.aevorinstudios.aevorinAuth.AevorinAuth;
import dev.aevorinstudios.aevorinAuth.manager.AuthManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class RegisterCommand implements CommandExecutor {

    private final AevorinAuth plugin;
    private final AuthManager authManager;

    public RegisterCommand(AevorinAuth plugin, AuthManager authManager) {
        this.plugin = plugin;
        this.authManager = authManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(authManager.getMessage("only-players"));
            return true;
        }

        if (authManager.isAuthenticated(player.getUniqueId())) {
            player.sendMessage(authManager.getMessage("already-logged-in"));
            return true;
        }

        authManager.isRegistered(player.getUniqueId()).thenAccept(registered -> {
            if (registered) {
                player.sendMessage(authManager.getMessage("already-registered"));
                return;
            }

            if (args.length != 2) {
                player.sendMessage(authManager.getMessage("register-usage"));
                return;
            }

            String password = args[0];
            String confirmPassword = args[1];

            if (!password.equals(confirmPassword)) {
                player.sendMessage(authManager.getMessage("passwords-dont-match"));
                return;
            }

            authManager.register(player.getUniqueId(), password).thenAccept(success -> {
                dev.aevorinstudios.aevorinAuth.utils.SchedulerUtils.runEntitySync(plugin, player, () -> {
                    if (success) {
                        authManager.setAuthenticated(player.getUniqueId(), true);
                        player.removePotionEffect(PotionEffectType.BLINDNESS);
                        plugin.getProtocolManager().refreshPlayerPosition(player);
                        player.sendMessage(authManager.getMessage("register-success"));
                    }
                });
            });
        });

        return true;
    }
}

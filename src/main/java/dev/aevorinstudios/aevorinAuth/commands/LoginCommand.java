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

public class LoginCommand implements CommandExecutor {

    private final AevorinAuth plugin;
    private final AuthManager authManager;

    public LoginCommand(AevorinAuth plugin, AuthManager authManager) {
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
            if (!registered) {
                player.sendMessage(authManager.getMessage("not-registered"));
                return;
            }

            if (args.length != 1) {
                player.sendMessage(authManager.getMessage("login-usage"));
                return;
            }

            String password = args[0];

            authManager.checkPassword(player.getUniqueId(), password).thenAccept(correct -> {
                dev.aevorinstudios.aevorinAuth.utils.SchedulerUtils.runEntitySync(plugin, player, () -> {
                    if (correct) {
                        authManager.setAuthenticated(player.getUniqueId(), true);
                        player.removePotionEffect(PotionEffectType.BLINDNESS);
                        plugin.getProtocolManager().refreshPlayerPosition(player);
                        player.sendMessage(authManager.getMessage("login-success"));
                    } else {
                        player.sendMessage(authManager.getMessage("wrong-password"));
                    }
                });
            });
        });

        return true;
    }
}

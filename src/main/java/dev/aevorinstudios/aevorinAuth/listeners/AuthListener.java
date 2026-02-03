package dev.aevorinstudios.aevorinAuth.listeners;

import dev.aevorinstudios.aevorinAuth.AevorinAuth;
import dev.aevorinstudios.aevorinAuth.manager.AuthManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AuthListener implements Listener {

    private final AevorinAuth plugin;
    private final AuthManager authManager;

    public AuthListener(AevorinAuth plugin, AuthManager authManager) {
        this.plugin = plugin;
        this.authManager = authManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        authManager.setAuthenticated(player.getUniqueId(), false);

        if (plugin.getConfig().getBoolean("settings.apply-blindness", true)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1, false, false));
        }

        authManager.isRegistered(player.getUniqueId()).thenAccept(registered -> {
            if (registered) {
                player.sendMessage(authManager.getMessage("welcome-login"));
            } else {
                player.sendMessage(authManager.getMessage("welcome-register"));
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        authManager.setAuthenticated(event.getPlayer().getUniqueId(), false);
        plugin.getProtocolManager().cleanupPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!authManager.isAuthenticated(event.getPlayer().getUniqueId())) {
            boolean allowHead = plugin.getConfig().getBoolean("settings.allow-head-movement", false);

            if (allowHead) {
                // If coordinates changed, teleport back but keep rotation
                if (event.getFrom().getX() != event.getTo().getX() ||
                        event.getFrom().getY() != event.getTo().getY() ||
                        event.getFrom().getZ() != event.getTo().getZ()) {

                    event.setTo(event.getFrom().setDirection(event.getTo().getDirection()));
                }
            } else {
                // Block everything
                event.setTo(event.getFrom());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!authManager.isAuthenticated(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(authManager.getMessage("must-login"));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase();
        if (message.startsWith("/login") || message.startsWith("/register"))
            return;

        if (!authManager.isAuthenticated(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(authManager.getMessage("must-login"));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!authManager.isAuthenticated(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!authManager.isAuthenticated(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!authManager.isAuthenticated(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player player) {
            if (!authManager.isAuthenticated(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!authManager.isAuthenticated(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPickupItem(PlayerAttemptPickupItemEvent event) {
        if (!authManager.isAuthenticated(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            if (!authManager.isAuthenticated(player.getUniqueId())) {
                if (plugin.getConfig().getBoolean("settings.prevent-inventory-click", true)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerSwapItem(PlayerSwapHandItemsEvent event) {
        if (!authManager.isAuthenticated(event.getPlayer().getUniqueId())) {
            if (plugin.getConfig().getBoolean("settings.prevent-item-swap", true)) {
                event.setCancelled(true);
            }
        }
    }
}

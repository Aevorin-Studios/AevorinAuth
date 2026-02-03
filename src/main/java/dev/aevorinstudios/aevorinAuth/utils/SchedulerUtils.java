package dev.aevorinstudios.aevorinAuth.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public class SchedulerUtils {

    private static final boolean IS_FOLIA = isClassPresent("io.papermc.paper.threadedregionscheduler.RegionScheduler");

    private static boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void runAsync(Plugin plugin, Runnable runnable) {
        if (IS_FOLIA) {
            Bukkit.getAsyncScheduler().runNow(plugin, task -> runnable.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
        }
    }

    public static void runSync(Plugin plugin, Runnable runnable) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().run(plugin, task -> runnable.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    public static void runLater(Plugin plugin, Runnable runnable, long delayTicks) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> runnable.run(), delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
        }
    }

    public static void runEntitySync(Plugin plugin, Entity entity, Runnable runnable) {
        if (IS_FOLIA) {
            entity.getScheduler().run(plugin, task -> runnable.run(), null);
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    public static void runEntityLater(Plugin plugin, Entity entity, Runnable runnable, long delayTicks) {
        if (IS_FOLIA) {
            entity.getScheduler().runDelayed(plugin, task -> runnable.run(), null, delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
        }
    }
}

package io.github.qgerman2.jp;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Log {
    private static JavaPlugin Plugin;
    private static final PlayerMap<Long> time = new PlayerMap<>();
    public static void initialize(JavaPlugin Plugin) {
        Log.Plugin = Plugin;
    }
    public static void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            Database.newPlayerEntry(player.getName());
        }
        time.put(player.getName(), System.currentTimeMillis());
    }
    public static void onPlayerQuit(PlayerQuitEvent event) {
        if (time.containsKey(event.getPlayer().getName())) {
            long t = System.currentTimeMillis() - time.get(event.getPlayer().getName());
            Database.updatePlayerTime(event.getPlayer().getName(), t);
        }
    }
    public static void onPlayerDeath(PlayerDeathEvent event) {
        String reason = "UNKNOWN";
        String by = "UNKNOWN";
        EntityDamageEvent damage = event.getEntity().getLastDamageCause();
        if (damage != null) {
            DamageCause cause = damage.getCause();
            reason = cause.name();
        }
        if (damage instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) damage).getDamager();
            by = damager.getName();
        }
        Database.updatePlayerDeath(event.getEntity().getName(), reason, by);
    }
}

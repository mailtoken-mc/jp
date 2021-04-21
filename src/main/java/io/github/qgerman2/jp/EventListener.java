package io.github.qgerman2.jp;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class EventListener implements Listener {
    private static JavaPlugin Plugin;
    public static void initialize(JavaPlugin Plugin) {
        EventListener.Plugin = Plugin;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Log.onPlayerJoin(event);
        PlayerMap.onPlayerJoin(event.getPlayer().getName());
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Log.onPlayerQuit(event);
        PlayerMap.onPlayerQuit(event.getPlayer().getName());
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Log.onPlayerDeath(event);
    }
    @EventHandler
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {
        Log.onPlayerAdvancementDone(event);
    }
}

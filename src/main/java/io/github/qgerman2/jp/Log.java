package io.github.qgerman2.jp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.advancement.Advancement;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Log {
    private static JavaPlugin Plugin;
    private static final PlayerMap<Long> time = new PlayerMap<>();
    private static FileConfiguration advancementsData;
    public static void initialize(JavaPlugin Plugin) {
        Log.Plugin = Plugin;
        File advancementsYml = new File(Plugin.getDataFolder(), "advancements.yml");
        Plugin.saveResource(advancementsYml.getName(), true);
        advancementsData = YamlConfiguration.loadConfiguration(advancementsYml);
    }
    public static void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Database.checkPlayerEntry(player.getName());
        time.put(player.getName(), System.currentTimeMillis());
        if (!player.hasPlayedBefore()) {
            Database.newEvent("JOIN", player.getName(), "",
            "{\"time\":" + System.currentTimeMillis() + "}");
        }
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
        String perp = "";
        EntityDamageEvent damage = event.getEntity().getLastDamageCause();
        if (damage != null) {
            DamageCause cause = damage.getCause();
            reason = cause.name();
        }
        if (damage instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) damage).getDamager();
            by = damager.getName();
            if (damager instanceof Player) {
                perp = by;
                reason = "PLAYER_ATTACK";
            }
        }
        Database.updatePlayerDeath(event.getEntity().getName(), reason, by);
        Database.newEvent("DEATH", event.getEntity().getName(), perp,
                "{" +
                        "\"time\":" + System.currentTimeMillis() + "," +
                        "\"reason\":\"" + reason + "\"," +
                        "\"by\":\"" + by + "\"" +
                "}");
        if (!perp.equals("")) {
            Database.updatePlayerKills(perp);
        }
    }
    public static void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {
        Advancement advancement = event.getAdvancement();
        String advancementName = advancement.getKey().toString();
        if (advancementName.startsWith("minecraft:recipes")) {
            return;
        }
        advancementName = advancementName.substring(10);
        Integer advancementNumber = (Integer) advancementsData.getValues(false).get(advancementName);
        Database.updatePlayerAdvancement(event.getPlayer().getName(), advancementNumber);
        Database.newEvent("ADVANCEMENT", event.getPlayer().getName(), "",
        "{" +
                "\"time\":" + System.currentTimeMillis() + "," +
                "\"advancement\":\"" + advancementName + "\"" +
        "}");
    }
}

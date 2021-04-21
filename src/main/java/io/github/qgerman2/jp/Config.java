package io.github.qgerman2.jp;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config {
    private static JavaPlugin Plugin;
    private static FileConfiguration config;
    public static void initialize(JavaPlugin Plugin) {
        Config.Plugin = Plugin;
        Plugin.saveDefaultConfig();
        config = Plugin.getConfig();
    }
    public static String getDB(String key) {
        return config.getConfigurationSection("db").getValues(false).get(key).toString();
    }
}
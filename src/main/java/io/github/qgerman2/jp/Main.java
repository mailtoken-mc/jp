package io.github.qgerman2.jp;

import org.bukkit.advancement.Advancement;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        Config.initialize(this);
        Database.initialize(this);
        EventListener.initialize(this);
        Log.initialize(this);
        getServer().getPluginManager().registerEvents(new EventListener(), this);
    }
    @Override
    public void onDisable() {

    }
}

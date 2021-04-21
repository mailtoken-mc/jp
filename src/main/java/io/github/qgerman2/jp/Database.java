package io.github.qgerman2.jp;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.mariadb.jdbc.Driver;
import java.sql.*;

public class Database {
    private static JavaPlugin Plugin;
    private static Connection conn;
    public static void initialize(JavaPlugin Plugin) {
        Database.Plugin = Plugin;
        try {
            String query;
            PreparedStatement stmnt;
            ResultSet result;
            //Connection itself
            Class.forName("org.mariadb.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/" + Config.getDB("name"),
                    Config.getDB("user"), Config.getDB("pass"));
            Plugin.getLogger().info("Successfully connected to database server");
            //Check for player table
            query = "SELECT `TABLE_NAME` " +
                    "FROM `information_schema`.`TABLES` " +
                    "WHERE `TABLE_SCHEMA` = ? " +
                    "AND `TABLE_NAME` = ? ";
            stmnt = conn.prepareStatement(query);
            stmnt.setString(1, Config.getDB("name"));
            stmnt.setString(2, Config.getDB("playertable"));
            result = stmnt.executeQuery();
            if (result.next()) {
                Plugin.getLogger().info("Found player database '" + Config.getDB("playertable") + "'");
            } else {
                Plugin.getLogger().info("Couldn't find player database '" + Config.getDB("playertable") + "', creating it...");
                query = "CREATE TABLE `" + Config.getDB("playertable") + "` (" +
                        "`name` TINYTEXT NOT NULL DEFAULT ''," +
                        "`alive` INT(1) UNSIGNED NULL DEFAULT '1'," +
                        "`born` TIMESTAMP NOT NULL DEFAULT current_timestamp()," +
                        "`died` TIMESTAMP NULL DEFAULT NULL," +
                        "`death_reason` TINYTEXT NULL DEFAULT NULL," +
                        "`death_by` TINYTEXT DEFAULT NULL, " +
                        "`kills` INT(10) UNSIGNED NOT NULL DEFAULT '0'," +
                        "`experience` INT(10) UNSIGNED NOT NULL DEFAULT '0'," +
                        "`time` INT(10) UNSIGNED NOT NULL DEFAULT '0'," +
                        "`advancements` BINARY(80) NOT NULL DEFAULT repeat('0', 80)," +
                        "CONSTRAINT UNIQUE (name))";
                stmnt = conn.prepareStatement(query);
                stmnt.executeQuery();
            }
            //Check for history table
            query = "SELECT `TABLE_NAME` " +
                    "FROM `information_schema`.`TABLES` " +
                    "WHERE `TABLE_SCHEMA` = ? " +
                    "AND `TABLE_NAME` = ? ";
            stmnt = conn.prepareStatement(query);
            stmnt.setString(1, Config.getDB("name"));
            stmnt.setString(2, Config.getDB("historytable"));
            result = stmnt.executeQuery();
            if (result.next()) {
                Plugin.getLogger().info("Found history database '" + Config.getDB("historytable") + "'");
            } else {
                Plugin.getLogger().info("Couldn't find player database '" + Config.getDB("historytable") + "', creating it...");
                query = "CREATE TABLE `" + Config.getDB("historytable") + "` (" +
                        "`i` INT UNSIGNED NOT NULL AUTO_INCREMENT," +
                        "`player1` TINYTEXT NULL DEFAULT '0'," +
                        "`player2` TINYTEXT NULL DEFAULT '0'," +
                        "`event` TINYTEXT NULL DEFAULT NULL," +
                        "`json` TEXT NULL DEFAULT NULL," +
                        "CONSTRAINT UNIQUE (i))";
                stmnt = conn.prepareStatement(query);
                stmnt.executeQuery();
            }
            Plugin.getLogger().info("Done");
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }
    public static void checkPlayerEntry(String name) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String query;
                    PreparedStatement stmnt;
                    //Check entry
                    query = "SELECT `name` " +
                            "FROM `" + Config.getDB("name") + "`.`" + Config.getDB("playertable") + "` " +
                            "WHERE `name` = ? LIMIT 1";
                    stmnt = conn.prepareStatement(query);
                    stmnt.setString(1, name);
                    ResultSet results = stmnt.executeQuery();
                    if (!results.next()) {
                        //Add entry
                        query = "INSERT " +
                                "INTO `" + Config.getDB("name") + "`.`" + Config.getDB("playertable") + "` " +
                                "(`name`) VALUES (?)";
                        stmnt = conn.prepareStatement(query);
                        stmnt.setString(1, name);
                        stmnt.executeQuery();
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Plugin);
    }
    public static void updatePlayerTime(String name, long time) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String query = "UPDATE " +
                            "`" + Config.getDB("name") + "`.`" + Config.getDB("playertable") + "` " +
                            "SET `time` = `time` + ? " +
                            "WHERE `name` = ?";
                    PreparedStatement stmnt = conn.prepareStatement(query);
                    stmnt.setLong(1, time);
                    stmnt.setString(2, name);
                    stmnt.executeQuery();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Plugin);
    }
    public static void updatePlayerDeath(String name, String reason, String by) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String query = "UPDATE " +
                            "`" + Config.getDB("name") + "`.`" + Config.getDB("playertable") + "` " +
                            "SET `alive` = 0, `died` = current_timestamp(), `death_reason` = ?, `death_by` = ? " +
                            "WHERE `name` = ?";
                    PreparedStatement stmnt = conn.prepareStatement(query);
                    stmnt.setString(1, reason);
                    stmnt.setString(2, by);
                    stmnt.setString(3, name);
                    stmnt.executeQuery();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Plugin);
    }
    public static void newEvent(String type, String player1, String player2, String json) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String query = "INSERT " +
                            "INTO `" + Config.getDB("name") + "`.`" + Config.getDB("historytable") + "` " +
                            "(`i`, `player1`, `player2`, `event`, `json`) " +
                            "VALUES (NULL, ?, ?, ?, ?)";
                    PreparedStatement stmnt = conn.prepareStatement(query);
                    stmnt.setString(1, player1);
                    stmnt.setString(2, player2);
                    stmnt.setString(3, type);
                    stmnt.setString(4, json);
                    stmnt.executeQuery();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Plugin);
    }
    public static void updatePlayerKills(String player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String query = "UPDATE " +
                            "`" + Config.getDB("name") + "`.`" + Config.getDB("playertable") + "` " +
                            "SET `kills` = `kills` + 1 " +
                            "WHERE `name` = ?";
                    PreparedStatement stmnt = conn.prepareStatement(query);
                    stmnt.setString(1, player);
                    stmnt.executeQuery();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Plugin);
    }
    public static void updatePlayerAdvancement(String player, Integer advancement) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String query = "UPDATE " +
                            "`" + Config.getDB("name") + "`.`" + Config.getDB("playertable") + "` " +
                            "SET `advancements` = INSERT(`advancements`, ?, 1, '1') " +
                            "WHERE `name` = ?";
                    PreparedStatement stmnt = conn.prepareStatement(query);
                    stmnt.setInt(1, advancement);
                    stmnt.setString(2, player);
                    stmnt.executeQuery();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Plugin);
    }
}

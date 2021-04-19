package io.github.qgerman2.jp;

import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.scheduler.BukkitRunnable;
import org.mariadb.jdbc.Driver;

import javax.xml.transform.Result;
import java.sql.*;

public class Database {
    private static JavaPlugin Plugin;
    private static Connection conn;
    private static final String table = "jugador";
    public static void initialize(JavaPlugin Plugin) {
        Database.Plugin = Plugin;
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    //Connection itself
                    Class.forName("org.mariadb.jdbc.Driver");
                    conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/" + Config.getDB("name"),
                            Config.getDB("user"), Config.getDB("pass"));
                    Plugin.getLogger().info("Successfully connected to database server");
                    //Check for table
                    String check = "SELECT `TABLE_NAME` " +
                            "FROM `information_schema`.`TABLES` " +
                            "WHERE `TABLE_SCHEMA` = ? " +
                            "AND `TABLE_NAME` = ? ";
                    PreparedStatement stmnt1 = conn.prepareStatement(check);
                    stmnt1.setString(1, Config.getDB("name"));
                    stmnt1.setString(2, Config.getDB("table"));
                    ResultSet result = stmnt1.executeQuery();
                    if (result.next()) {
                        Plugin.getLogger().info("Found player database '" + Config.getDB("table") + "'");
                    } else {
                        Plugin.getLogger().info("Couldn't find player database '" + Config.getDB("table") + "', creating it...");
                        //Create if it doesn't exist
                        String create = "CREATE TABLE `" + Config.getDB("table") + "` (" +
                                "`name` TINYTEXT NOT NULL DEFAULT ''," +
                                "`alive` INT(1) UNSIGNED NULL DEFAULT '1'," +
                                "`born` TIMESTAMP NOT NULL DEFAULT current_timestamp()," +
                                "`died` TIMESTAMP NULL DEFAULT NULL," +
                                "`death_reason` TINYTEXT NULL DEFAULT NULL," +
                                "`death_by` TINYTEXT DEFAULT NULL, " +
                                "`kills` INT(10) UNSIGNED NOT NULL DEFAULT '0'," +
                                "`experience` INT(10) UNSIGNED NOT NULL DEFAULT '0'," +
                                "`time` INT(10) UNSIGNED NOT NULL DEFAULT '0'," +
                                "`advancements` BINARY(8) NOT NULL DEFAULT repeat('0', 8), " +
                                "CONSTRAINT UNIQUE (name))";
                        PreparedStatement stmnt2 = conn.prepareStatement(create);
                        stmnt2.executeQuery();
                        Plugin.getLogger().info("Done");
                    }
                } catch (SQLException | ClassNotFoundException throwables) {
                    throwables.printStackTrace();
                }
            }
        }.runTask(Plugin);
    }
    public static void newPlayerEntry(String name) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    //Add entry
                    String query = "INSERT INTO " +
                            "`" + Config.getDB("name") + "`.`" + Config.getDB("table") + "` " +
                            "(`name`) VALUES (?)";
                    PreparedStatement stmnt = conn.prepareStatement(query);
                    stmnt.setString(1, name);
                    stmnt.executeQuery();
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
                            "`" + Config.getDB("name") + "`.`" + Config.getDB("table") + "` " +
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
                            "`" + Config.getDB("name") + "`.`" + Config.getDB("table") + "` " +
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
}

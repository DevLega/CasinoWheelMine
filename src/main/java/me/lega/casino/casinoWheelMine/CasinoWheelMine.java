package me.lega.casino.casinoWheelMine;

import me.lega.casino.casinoWheelMine.Database.ExternalDatabaseManager;
import me.lega.casino.casinoWheelMine.Database.TransactionDatabaseManager;
import me.lega.casino.casinoWheelMine.Database.WheelDatabase;
import me.lega.casino.casinoWheelMine.Listeners.PlayerJoinListener;
import me.lega.casino.casinoWheelMine.Listeners.WheelMenuListener;
import me.lega.casino.casinoWheelMine.Utils.ActionBarTask;
import me.lega.casino.casinoWheelMine.Utils.WheelSpawn;
import me.lega.casino.casinoWheelMine.Utils.WheelSpenVirtual;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class CasinoWheelMine extends JavaPlugin {
    private WheelSpawn wheelSpawn;
    private WheelDatabase wheelDatabase;
    private static CasinoWheelMine instance;
    private ExternalDatabaseManager externalDatabaseManager;
    private TransactionDatabaseManager transactionDatabaseManager;
    private Set<UUID> activePlayers = new HashSet<>();

    public Set<UUID> getActivePlayers() {
        return activePlayers;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        initializeDatabases();
        saveDefaultConfig();
        removeAllItemDisplays();
        saveCurrentAngle(0);
        saveCurrentIsSpeen(false);
        wheelSpawn = new WheelSpawn(this);
        wheelSpawn.spawnItem();
        wheelSpawn.spawnPimpochka();
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoinListener(wheelDatabase), this);
        Bukkit.getServer().getPluginManager().registerEvents(new WheelMenuListener(wheelDatabase, externalDatabaseManager, transactionDatabaseManager, activePlayers), this);
        new ActionBarTask(wheelDatabase).runTaskTimer(this, 0, 20);
        new WheelSpenVirtual(wheelDatabase);
        scheduleWheelSpinTask();
    }

    @Override
    public void onDisable() {
        instance = null;
        if (wheelSpawn != null) {
            wheelSpawn.removeItem();
            wheelSpawn.removePimpochka();
        }

        if (wheelDatabase != null) {
            try {
                wheelDatabase.closeConnection();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (externalDatabaseManager != null) {
            externalDatabaseManager.closeConnection();
        }
        if (transactionDatabaseManager != null) {
            transactionDatabaseManager.closeConnection();
        }
        saveCurrentAngle(0);
    }

    private void initializeDatabases() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            wheelDatabase = new WheelDatabase(getDataFolder().getAbsolutePath() + "/wheel.db");
            System.out.println("Database connection established.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Failed to connect to the database: " + ex.getMessage());
        }

        externalDatabaseManager = new ExternalDatabaseManager(this);
        externalDatabaseManager.connectToExternalDatabase();
        transactionDatabaseManager = new TransactionDatabaseManager(this);
        transactionDatabaseManager.connectToTransactionDatabase();
    }

    private void scheduleWheelSpinTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            WheelSpenVirtual wheelSpenVirtual = new WheelSpenVirtual(wheelDatabase);
            try {
                wheelSpenVirtual.spinWheel();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, 0L, 600L); // 600 ticks = 30 seconds
    }

    private void removeAllItemDisplays() {
        Bukkit.getWorlds().forEach(world -> {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof ItemDisplay) {
                    entity.remove();
                }
            }
        });
    }

    public ItemDisplay getWheel() {
        return wheelSpawn != null ? wheelSpawn.getWheel() : null;
    }

    public static CasinoWheelMine getInstance() {
        return instance;
    }

    public int getSpinNumber() {
        return getConfig().getInt("spinNumber", 1); // 1 is the default value if not found
    }

    public void setSpinNumber(int spinNumber) {
        getConfig().set("spinNumber", spinNumber);
        saveConfig();
    }

    public void saveCurrentAngle(double angle) {
        getConfig().set("currentAngle", angle);
        saveConfig();
    }

    public double getCurrentAngle() {
        return getConfig().getDouble("currentAngle", 0.0);
    }

    public void saveCurrentColor(String color) {
        getConfig().set("color", color);
        saveConfig();
    }

    public String getCurrentColor() {
        return getConfig().getString("color", "white");
    }

    public void saveCurrentIsSpeen(Boolean isSpeen) {
        getConfig().set("isSpeen", isSpeen);
        saveConfig();
    }

    public Boolean getCurrentIsSpeen() {
        return getConfig().getBoolean("isSpeen", false);
    }
}

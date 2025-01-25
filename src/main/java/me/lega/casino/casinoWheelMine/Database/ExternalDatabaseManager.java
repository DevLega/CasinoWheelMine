package me.lega.casino.casinoWheelMine.Database;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExternalDatabaseManager {
    private Connection connection;
    private final JavaPlugin plugin;

    public ExternalDatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public synchronized void connectToExternalDatabase() {
        try {
            String url = "jdbc:sqlite:plugins/BankPluginNew/accounts.db";
            connection = DriverManager.getConnection(url);
            plugin.getLogger().info("Connected to the external database.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to connect to the external database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public synchronized Connection getConnection() {
        return connection;
    }

    public synchronized void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("External database connection closed.");
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to close the external database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public String getAccountName(String accountNumber) {
        return getStringField(accountNumber, "name", "Без имени");
    }

    public int getBalance(String accountNumber) {
        return getIntField(accountNumber, "balance");
    }

    public void depositBalance(String accountNumber, int amount) {
        executeUpdate("UPDATE accounts SET balance = balance + ? WHERE account_number = ?", amount, accountNumber);
    }

    public void withdrawBalance(String accountNumber, int amount) {
        executeUpdate("UPDATE accounts SET balance = balance - ? WHERE account_number = ?", amount, accountNumber);
    }

    public List<String> getAccountsByOwner(String owner) {
        List<String> accounts = new ArrayList<>();
        String querySQL = "SELECT account_number FROM accounts WHERE owner = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(querySQL)) {
            pstmt.setString(1, owner);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                accounts.add(rs.getString("account_number"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accounts;
    }

    public int getAccountCountByOwnerName(String ownerName) {
        String querySQL = "SELECT COUNT(*) AS account_count FROM accounts WHERE owner_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(querySQL)) {
            pstmt.setString(1, ownerName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("account_count");
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[BankPlugin] Ошибка при подсчете аккаунтов игрока: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    private String getStringField(String accountNumber, String field, String defaultValue) {
        String querySQL = "SELECT " + field + " FROM accounts WHERE account_number = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(querySQL)) {
            pstmt.setString(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString(field);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    private int getIntField(String accountNumber, String field) {
        String querySQL = "SELECT " + field + " FROM accounts WHERE account_number = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(querySQL)) {
            pstmt.setString(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(field);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private boolean executeUpdate(String sql, Object... params) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

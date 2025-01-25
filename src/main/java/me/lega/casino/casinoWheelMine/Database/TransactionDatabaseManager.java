package me.lega.casino.casinoWheelMine.Database;

import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;

public class TransactionDatabaseManager {
    private Connection connection;
    private final JavaPlugin plugin;

    public TransactionDatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public synchronized void connectToTransactionDatabase() {
        try {
            String url = "jdbc:sqlite:plugins/BankPluginNew/transactions.db";
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

    public void createWithdrawTransaction(int amount, String recipient, String recipientAccount) {
        String insertSQL = "INSERT INTO transactions (transaction_type, recipient, recipient_account, amount, date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setString(1, "withdraw_casino");
            pstmt.setString(2, recipient);
            pstmt.setString(3, recipientAccount);
            pstmt.setInt(4, amount);
            pstmt.setString(5, getCurrentFormattedDate());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createDepositTransaction(int amount, String recipient, String recipientAccount) {
        String insertSQL = "INSERT INTO transactions (transaction_type, recipient, recipient_account, amount, date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setString(1, "deposit_casino");
            pstmt.setString(2, recipient);
            pstmt.setString(3, recipientAccount);
            pstmt.setInt(4, amount);
            pstmt.setString(5, getCurrentFormattedDate());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getCurrentFormattedDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yy, HH:mm");
        return sdf.format(new java.util.Date());
    }

    private int getNextTransactionNumber() {
        String querySQL = "SELECT MAX(transaction_number) AS max_transaction_number FROM transactions";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(querySQL)) {
            if (rs.next()) {
                return rs.getInt("max_transaction_number") + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1; // Default to 1 if no transactions exist
    }
}

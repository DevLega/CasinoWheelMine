package me.lega.casino.casinoWheelMine.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WheelDatabase {

    private final Connection connection;

    public WheelDatabase(String path) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS bets ("
                    + "bet_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "player_uuid TEXT NOT NULL,"
                    + "player_name TEXT NOT NULL,"
                    + "bet_amount INTEGER NOT NULL,"
                    + "bet_color TEXT NOT NULL,"
                    + "spin_number INTEGER NOT NULL"
                    + ")");
            statement.execute("CREATE TABLE IF NOT EXISTS prizes ("
                    + "prize_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "player_uuid TEXT NOT NULL,"
                    + "player_name TEXT NOT NULL,"
                    + "prize_amount INTEGER NOT NULL,"
                    + "claimed BOOLEAN NOT NULL,"
                    + "bet_id INTEGER NOT NULL,"
                    + "FOREIGN KEY(bet_id) REFERENCES bets(bet_id)"
                    + ")");
            statement.execute("CREATE TABLE IF NOT EXISTS players ("
                    + "player_uuid TEXT PRIMARY KEY,"
                    + "player_money INTEGER NOT NULL"
                    + ")");
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public void addBet(String playerUUID, String playerName, int betAmount, String betColor, int spinNumber) throws SQLException {
        String sql = "INSERT INTO bets (player_uuid, player_name, bet_amount, bet_color, spin_number) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            pstmt.setString(2, playerName);
            pstmt.setInt(3, betAmount);
            pstmt.setString(4, betColor);
            pstmt.setInt(5, spinNumber);
            pstmt.executeUpdate();
        }
    }

    // Метод для добавления приза
    public void addPrize(String playerUUID, String playerName, int prizeAmount, boolean claimed, int betId) throws SQLException {
        String sql = "INSERT INTO prizes (player_uuid, player_name, prize_amount, claimed, bet_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            pstmt.setString(2, playerName);
            pstmt.setInt(3, prizeAmount);
            pstmt.setBoolean(4, claimed);
            pstmt.setInt(5, betId);
            pstmt.executeUpdate();
        }
    }

    // Метод для получения ID ставки по игроку и цвету
    public int getBetIdByPlayerAndColor(String playerUUID, String color, int spinNumber) throws SQLException {
        String sql = "SELECT bet_id FROM bets WHERE player_uuid = ? AND bet_color = ? AND spin_number = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            pstmt.setString(2, color);
            pstmt.setInt(3, spinNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("bet_id");
                } else {
                    throw new SQLException("Bet not found for player UUID: " + playerUUID + ", color: " + color + ", and spin number: " + spinNumber);
                }
            }
        }
    }

    // Метод для получения суммы ставки по ID
    public int getBetAmountById(int betId) throws SQLException {
        String sql = "SELECT bet_amount FROM bets WHERE bet_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, betId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("bet_amount");
                } else {
                    throw new SQLException("Bet not found for ID: " + betId);
                }
            }
        }
    }

    // Метод для обновления суммы ставки по ID
    public void updateBetAmountById(int betId, int newBetAmount) throws SQLException {
        String sql = "UPDATE bets SET bet_amount = ? WHERE bet_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, newBetAmount);
            pstmt.setInt(2, betId);
            pstmt.executeUpdate();
        }
    }

    // Метод для удаления ставки по ID
    public void deleteBetById(int betId) throws SQLException {
        String sql = "DELETE FROM bets WHERE bet_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, betId);
            pstmt.executeUpdate();
        }
    }

    // Метод для проверки наличия ставки на цвет и спин
    public boolean hasBetOnColorAndSpin(String playerUUID, String color, int spinNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bets WHERE player_uuid = ? AND bet_color = ? AND spin_number = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            pstmt.setString(2, color);
            pstmt.setInt(3, spinNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                } else {
                    return false;
                }
            }
        }
    }

    public List<Integer> getBetIdsBySpinNumberAndColor(int spinNumber, String color) throws SQLException {
        String sql = "SELECT bet_id FROM bets WHERE spin_number = ? AND bet_color = ?";
        List<Integer> betIds = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, spinNumber);
            pstmt.setString(2, color);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    betIds.add(rs.getInt("bet_id"));
                }
            }
        }
        return betIds;
    }

    public String getPlayerUUIDByBetId(int betId) throws SQLException {
        String sql = "SELECT player_uuid FROM bets WHERE bet_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, betId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("player_uuid");
                } else {
                    throw new SQLException("Player UUID not found for bet ID: " + betId);
                }
            }
        }
    }

    // Method to get playerName by bet ID
    public String getPlayerNameByBetId(int betId) throws SQLException {
        String sql = "SELECT player_name FROM bets WHERE bet_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, betId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("player_name");
                } else {
                    throw new SQLException("Player name not found for bet ID: " + betId);
                }
            }
        }
    }

    // Method to get betColor by bet ID
    public String getBetColorByBetId(int betId) throws SQLException {
        String sql = "SELECT bet_color FROM bets WHERE bet_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, betId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("bet_color");
                } else {
                    throw new SQLException("Bet color not found for bet ID: " + betId);
                }
            }
        }
    }

    public Integer getLastUnclaimedPrizeIdByPlayerUUID(String playerUUID) throws SQLException {
        String sql = "SELECT prize_id FROM prizes WHERE player_uuid = ? AND claimed = false ORDER BY prize_id DESC LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("prize_id");
                } else {
                    return null; // No unclaimed prize found
                }
            }
        }
    }

    public int getPrizeAmountByPrizeId(int prizeId) throws SQLException {
        String sql = "SELECT prize_amount FROM prizes WHERE prize_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, prizeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("prize_amount");
                } else {
                    throw new SQLException("Prize amount not found for prize ID: " + prizeId);
                }
            }
        }
    }

    public void updatePrizeClaimedStatusById(int prizeId) throws SQLException {
        String sql = "UPDATE prizes SET claimed = true WHERE prize_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, prizeId);
            pstmt.executeUpdate();
        }
    }

    // Method to add a player
    public void addPlayer(String playerUUID, int playerMoney) throws SQLException {
        String sql = "INSERT INTO players (player_uuid, player_money) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            pstmt.setInt(2, playerMoney);
            pstmt.executeUpdate();
        }
    }

    // Method to update player money
    public void updatePlayerMoney(String playerUUID, int newPlayerMoney) throws SQLException {
        String sql = "UPDATE players SET player_money = ? WHERE player_uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, newPlayerMoney);
            pstmt.setString(2, playerUUID);
            pstmt.executeUpdate();
        }
    }

    // Method to get player money by UUID
    public int getPlayerMoney(String playerUUID) throws SQLException {
        String sql = "SELECT player_money FROM players WHERE player_uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("player_money");
                } else {
                    throw new SQLException("Player money not found for UUID: " + playerUUID);
                }
            }
        }
    }

    public boolean playerExists(String playerUUID) throws SQLException {
        String sql = "SELECT COUNT(*) FROM players WHERE player_uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                } else {
                    return false;
                }
            }
        }
    }

    public List<Integer> getBetIdsBySpinNumber(int spinNumber) throws SQLException {
        String sql = "SELECT bet_id FROM bets WHERE spin_number = ?";
        List<Integer> betIds = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, spinNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    betIds.add(rs.getInt("bet_id"));
                }
            }
        }
        return betIds;
    }
}

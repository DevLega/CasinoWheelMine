package me.lega.casino.casinoWheelMine.Utils;

import me.lega.casino.casinoWheelMine.CasinoWheelMine;
import me.lega.casino.casinoWheelMine.Database.WheelDatabase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.*;

public class WheelSpenVirtual {

    private final WheelDatabase wheelDatabase;

    public WheelSpenVirtual(WheelDatabase wheelDatabase) {
        this.wheelDatabase = wheelDatabase;
    }

    public void spinWheel() throws SQLException {
        CasinoWheelMine instance = CasinoWheelMine.getInstance();
        int currentSpinNumber = instance.getSpinNumber();

        List<Integer> betIds = wheelDatabase.getBetIdsBySpinNumber(currentSpinNumber);
        if (betIds == null || betIds.isEmpty()) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            InventoryView inventoryView = player.getOpenInventory();
            Bukkit.getLogger().warning(" " + inventoryView.getTitle());
            if ("§f<shift:-8>ꐚ<shift:-158>ꐿ".equals(inventoryView.getTitle())) {
                player.closeInventory();
            }
        }


        instance.setSpinNumber(currentSpinNumber + 1);

        WheelRotationTask rotationTask = new WheelRotationTask(instance.getWheel(), 24000);
        rotationTask.start();
        instance.saveCurrentIsSpeen(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                String color = CasinoWheelMine.getInstance().getCurrentColor();
                int multiplier = getMultiplier(color);
                instance.saveCurrentIsSpeen(false);

                // Хранение проигрышей
                Map<String, Integer> lossMap = new HashMap<>();

                try {
                    List<Integer> allBetIds = wheelDatabase.getBetIdsBySpinNumber(currentSpinNumber);
                    List<Integer> winningBetIds = wheelDatabase.getBetIdsBySpinNumberAndColor(currentSpinNumber, color);

                    for (int betId : allBetIds) {
                        String playerUUID = wheelDatabase.getPlayerUUIDByBetId(betId);
                        String playerName = wheelDatabase.getPlayerNameByBetId(betId);
                        int betAmount = wheelDatabase.getBetAmountById(betId);
                        Player player = Bukkit.getPlayer(UUID.fromString(playerUUID));
                        int playerBalance = wheelDatabase.getPlayerMoney(playerUUID);

                        boolean hasWon = winningBetIds.contains(betId);

                        if (hasWon) {
                            int prizeAmount = betAmount * multiplier;
                            boolean claimed = false;
                            wheelDatabase.addPrize(playerUUID, playerName, prizeAmount, claimed, betId);

                            if (player != null && player.isOnline()) {
                                player.sendMessage(net.md_5.bungee.api.ChatColor.of("#63c132") + "Вы выиграли " + net.md_5.bungee.api.ChatColor.of("#ffa630") + prizeAmount + "§fꐽ" + net.md_5.bungee.api.ChatColor.of("#63c132") + " поставив " + net.md_5.bungee.api.ChatColor.of("#ffa630") + betAmount + "§fꐽ" + net.md_5.bungee.api.ChatColor.of("#63c132") + " на " + colorText(color));
                                DiscordWebhookSender.sendCasinoWin(playerName, prizeAmount, betAmount, color, playerBalance);
                            }
                        } else {
                            // Сохраняем проигрыш игрока
                            lossMap.put(playerUUID, lossMap.getOrDefault(playerUUID, 0) + betAmount);
                        }
                    }

                    // Отправляем сообщение о проигрышах
                    for (Map.Entry<String, Integer> entry : lossMap.entrySet()) {
                        String playerUUID = entry.getKey();
                        int totalLoss = entry.getValue();
                        Player player = Bukkit.getPlayer(UUID.fromString(playerUUID));
                        DiscordWebhookSender.sendCasinoLoose(player.getName(), totalLoss, wheelDatabase.getPlayerMoney(playerUUID));
                        if (player != null && player.isOnline()) {
                            player.sendMessage(net.md_5.bungee.api.ChatColor.of("#ff4c4c") + "Вы проиграли " + net.md_5.bungee.api.ChatColor.of("#ffa630") + totalLoss + "§fꐽ" + net.md_5.bungee.api.ChatColor.of("#ff4c4c") + ", выпал " + colorText(color));
                        }
                    }

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }.runTaskLater(CasinoWheelMine.getInstance(), rotationTask.getDuration() / 50 + 40);
    }

    public int getMultiplier(String color) {
        switch (color) {
            case "white":
                return 2;
            case "green":
                return 3;
            case "blue":
                return 5;
            case "violet":
                return 8;
            case "red":
                return 12;
            default:
                return 1;
        }
    }

    public String colorText(String color) {
        switch (color) {
            case "white":
                return "§fБелый";
            case "green":
                return net.md_5.bungee.api.ChatColor.of("#63c132") + "Зелёный";
            case "blue":
                return "§3Синий";
            case "violet":
                return net.md_5.bungee.api.ChatColor.of("#a630ff") + "Пурпурный";
            case "red":
                return net.md_5.bungee.api.ChatColor.of("#ff3f30") + "Красный";
            default:
                return net.md_5.bungee.api.ChatColor.of("#ff3f30") + "Ничего";
        }
    }

}
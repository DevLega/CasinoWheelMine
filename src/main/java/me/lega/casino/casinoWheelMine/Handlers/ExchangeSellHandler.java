package me.lega.casino.casinoWheelMine.Handlers;

import me.lega.casino.casinoWheelMine.CasinoWheelMine;
import me.lega.casino.casinoWheelMine.Database.ExternalDatabaseManager;
import me.lega.casino.casinoWheelMine.Database.TransactionDatabaseManager;
import me.lega.casino.casinoWheelMine.Database.WheelDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ExchangeSellHandler {

    private WheelDatabase wheelDatabase;
    private ExternalDatabaseManager externalDatabaseManager;
    private TransactionDatabaseManager transactionDatabaseManager;
    private Set<UUID> activePlayers;

    public ExchangeSellHandler(WheelDatabase wheelDatabase, ExternalDatabaseManager externalDatabaseManager, TransactionDatabaseManager transactionDatabaseManager, Set<UUID> activePlayers) {
        this.wheelDatabase = wheelDatabase;
        this.externalDatabaseManager = externalDatabaseManager;
        this.transactionDatabaseManager = transactionDatabaseManager;
        this.activePlayers = activePlayers;
    }

    public void startSellInput(Player player) throws SQLException {
        UUID playerUUID = player.getUniqueId();
        if (activePlayers.contains(playerUUID)) {
            player.sendMessage(net.md_5.bungee.api.ChatColor.of("#d64933") + "Вы уже начали процесс ввода.");
            return;
        }

        activePlayers.add(playerUUID);
        player.closeInventory();
        int currentMoney = wheelDatabase.getPlayerMoney(playerUUID.toString());
        player.sendMessage(net.md_5.bungee.api.ChatColor.of("#adb5bd") + "Введите количество ар: §8(макс. " + currentMoney / 7 + ")");
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerChat(AsyncPlayerChatEvent event) throws SQLException {
                if (event.getPlayer() != player) return;

                event.setCancelled(true);
                String input = event.getMessage();

                if(externalDatabaseManager.getAccountCountByOwnerName(player.getName()) < 1) {
                    player.sendMessage(net.md_5.bungee.api.ChatColor.of("#adb5bd") + "Сперва откройте счёт в банке");
                    cleanup();
                    return;
                }
                String recipientAccountNumber = externalDatabaseManager.getAccountsByOwner(playerUUID.toString()).get(0);

                int itemCount;
                try {
                    itemCount = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    player.sendMessage(net.md_5.bungee.api.ChatColor.of("#d64933") + "Введите корректное число.");
                    cleanup();
                    return;
                }

                if (itemCount <= 9) {
                    player.sendMessage(net.md_5.bungee.api.ChatColor.of("#d64933") + "Количество ар должно быть больше или равно 10!");
                    cleanup();
                    return;
                }

                if (itemCount > wheelDatabase.getPlayerMoney(playerUUID.toString())) {
                    player.sendMessage(net.md_5.bungee.api.ChatColor.of("#d64933") + "У вас недостаточно монет!");
                    cleanup();
                    return;
                }

                Bukkit.getScheduler().runTask(CasinoWheelMine.getInstance(), () -> {
                    try {
                        processTransaction(player, itemCount, recipientAccountNumber);
                        transactionDatabaseManager.createWithdrawTransaction(itemCount, player.getName(), recipientAccountNumber);
                        player.sendMessage(net.md_5.bungee.api.ChatColor.of("#63c132") + "Игрок Server перевёл вам " + itemCount + " АР на счёт " + net.md_5.bungee.api.ChatColor.of("#ffa630") + "«" + externalDatabaseManager.getAccountName(recipientAccountNumber) + "» #" + recipientAccountNumber + net.md_5.bungee.api.ChatColor.of("#63c132") + " с комментарием: " + "'Вывод баланса'!");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                });

                cleanup();
            }

            private void cleanup() {
                AsyncPlayerChatEvent.getHandlerList().unregister(this);
                activePlayers.remove(playerUUID);
            }
        }, CasinoWheelMine.getInstance());
    }

    private void processTransaction(Player player, int itemCount, String recipientAccountNumber) throws SQLException {
        int totalCoins = itemCount * 7;

        externalDatabaseManager.depositBalance(recipientAccountNumber, itemCount);
        externalDatabaseManager.withdrawBalance("000001", itemCount);
        String playerUUID = player.getUniqueId().toString();
        int currentMoney = wheelDatabase.getPlayerMoney(playerUUID);
        wheelDatabase.updatePlayerMoney(playerUUID, currentMoney - totalCoins);
    }
}
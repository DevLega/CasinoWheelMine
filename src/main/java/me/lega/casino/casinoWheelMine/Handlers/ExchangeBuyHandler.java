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
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ExchangeBuyHandler {
    private WheelDatabase wheelDatabase;
    private ExternalDatabaseManager externalDatabaseManager;
    private TransactionDatabaseManager transactionDatabaseManager;
    private Set<UUID> activePlayers;

    public ExchangeBuyHandler(WheelDatabase wheelDatabase, ExternalDatabaseManager externalDatabaseManager, TransactionDatabaseManager transactionDatabaseManager, Set<UUID> activePlayers) {
        this.wheelDatabase = wheelDatabase;
        this.externalDatabaseManager = externalDatabaseManager;
        this.transactionDatabaseManager = transactionDatabaseManager;
        this.activePlayers = activePlayers;
    }

    public void startBuyInput(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (activePlayers.contains(playerUUID)) {
            player.sendMessage(net.md_5.bungee.api.ChatColor.of("#d64933") + "Вы уже начали процесс ввода.");
            return;
        }

        activePlayers.add(playerUUID);
        player.closeInventory();
        int maxItemAmount = countItem(player, Material.DEEPSLATE_DIAMOND_ORE);
        player.sendMessage(net.md_5.bungee.api.ChatColor.of("#adb5bd") + "Введите количество ар: §8(макс. " + maxItemAmount + ")");
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerChat(AsyncPlayerChatEvent event) throws SQLException {
                if (event.getPlayer() != player) return;

                event.setCancelled(true);
                String input = event.getMessage();

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

                int maxItemAmount = countItem(player, Material.DEEPSLATE_DIAMOND_ORE);
                if (itemCount > maxItemAmount) {
                    player.sendMessage(net.md_5.bungee.api.ChatColor.of("#d64933") + "У вас недостаточно ар!");
                    cleanup();
                    return;
                }

                Bukkit.getScheduler().runTask(CasinoWheelMine.getInstance(), () -> {
                    try {
                        processTransaction(player, itemCount);
                        player.sendMessage(net.md_5.bungee.api.ChatColor.of("#63c132") + "Вы успешно пополнили баланс!");
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

    private void processTransaction(Player player, int itemCount) throws SQLException {
        Integer playerDiamonds = countItem(player, Material.DEEPSLATE_DIAMOND_ORE);
        if(playerDiamonds < itemCount) return;
        removeItem(player, Material.DEEPSLATE_DIAMOND_ORE, itemCount);

        int totalCoins = itemCount * 7;
        int tenPercent = (int) Math.ceil(itemCount * 0.1);
        int ninetyPercent = itemCount - tenPercent;

        externalDatabaseManager.depositBalance("000000", tenPercent);
        externalDatabaseManager.depositBalance("000001", ninetyPercent);

        transactionDatabaseManager.createDepositTransaction(itemCount, player.getName(), "Casino");

        String playerUUID = player.getUniqueId().toString();
        int currentMoney = wheelDatabase.getPlayerMoney(playerUUID);
        wheelDatabase.updatePlayerMoney(playerUUID, ninetyPercent * 7 + currentMoney);
    }

    private int countItem(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private void removeItem(Player player, Material material, int amount) {
        ItemStack[] inventory = player.getInventory().getContents();

        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item != null && item.getType() == material) {
                if (item.getAmount() > amount) {
                    item.setAmount(item.getAmount() - amount);
                    break;
                } else {
                    amount -= item.getAmount();
                    inventory[i] = null;
                    if (amount <= 0) break;
                }
            }
        }

        player.getInventory().setContents(inventory);
        player.updateInventory();
    }
}
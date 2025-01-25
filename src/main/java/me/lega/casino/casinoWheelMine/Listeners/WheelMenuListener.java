package me.lega.casino.casinoWheelMine.Listeners;

import me.lega.casino.casinoWheelMine.CasinoWheelMine;
import me.lega.casino.casinoWheelMine.Database.ExternalDatabaseManager;
import me.lega.casino.casinoWheelMine.Database.TransactionDatabaseManager;
import me.lega.casino.casinoWheelMine.Database.WheelDatabase;
import me.lega.casino.casinoWheelMine.Handlers.BetInputHandler;
import me.lega.casino.casinoWheelMine.Handlers.ExchangeBuyHandler;
import me.lega.casino.casinoWheelMine.Handlers.ExchangeSellHandler;
import me.lega.casino.casinoWheelMine.Handlers.NewBetInputHandler;
import me.lega.casino.casinoWheelMine.Menu.ExchangeMenu;
import me.lega.casino.casinoWheelMine.Menu.WheelMenu;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class WheelMenuListener implements Listener {
    private WheelDatabase wheelDatabase;
    private ExternalDatabaseManager externalDatabaseManager;
    private TransactionDatabaseManager transactionDatabaseManager;
    private Set<UUID> activePlayers;

    public WheelMenuListener(WheelDatabase wheelDatabase, ExternalDatabaseManager externalDatabaseManager, TransactionDatabaseManager transactionDatabaseManager, Set<UUID> activePlayers) {
        this.wheelDatabase = wheelDatabase;
        this.externalDatabaseManager = externalDatabaseManager;
        this.transactionDatabaseManager = transactionDatabaseManager;
        this.activePlayers = activePlayers;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) throws SQLException {
        Player player = event.getPlayer();

        Action action = event.getAction();
        if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
            Location targetLocation = player.getTargetBlockExact(100) != null
                    ? player.getTargetBlockExact(100).getLocation()
                    : null;

            if (targetLocation != null) {
                if (isInTargetArea(targetLocation)) {
                    event.setCancelled(true);
                    player.openInventory(WheelMenu.openWheelMenu(player, wheelDatabase));
                } else if (isExchangeBlock(targetLocation)) {
                    event.setCancelled(true);
                    player.openInventory(ExchangeMenu.openExchangeMenu(player, wheelDatabase));
                }
            }
        }
    }

    private boolean isExchangeBlock(Location location) {
        return location.getBlockX() == -140 && location.getBlockY() == 68 && location.getBlockZ() == -51;
    }

    private boolean isInTargetArea(Location location) {
        int minX = -139, maxX = -138;
        int minY = 68, maxY = 72;
        int minZ = -52, maxZ = -50;

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        return x >= minX && x <= maxX &&
                y >= minY && y <= maxY &&
                z >= minZ && z <= maxZ;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) throws SQLException {
        if (event.getView().getTitle().equals("§f<shift:-8>ꐚ<shift:-158>ꐿ")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            UUID playerUUID = player.getUniqueId();
            InventoryAction action = event.getAction();
            int slot = event.getRawSlot();

            if (event.getClickedInventory() != null && event.getCurrentItem() != null) {
                ItemStack currentItem = event.getCurrentItem();
                if (currentItem.hasItemMeta() && currentItem.getItemMeta().hasCustomModelData()) {
                    int customModelData = currentItem.getItemMeta().getCustomModelData();
                    int spinNumber = CasinoWheelMine.getInstance().getSpinNumber();
                    if (customModelData == 777) {
                        switch (slot) {
                            case 11:
                                handleBetInput(player, "white", spinNumber, action);
                                break;
                            case 12:
                                handleBetInput(player, "green", spinNumber, action);
                                break;
                            case 13:
                                handleBetInput(player, "blue", spinNumber, action);
                                break;
                            case 14:
                                handleBetInput(player, "violet", spinNumber, action);
                                break;
                            case 15:
                                handleBetInput(player, "red", spinNumber, action);
                                break;
                            case 31:
                                try {
                                    Integer playerBalance = wheelDatabase.getPlayerMoney(playerUUID.toString());
                                    Integer prizeId = wheelDatabase.getLastUnclaimedPrizeIdByPlayerUUID(player.getUniqueId().toString());
                                    if (prizeId != null) {
                                        Integer prizeAmount = wheelDatabase.getPrizeAmountByPrizeId(prizeId);
                                        player.closeInventory();
                                        wheelDatabase.updatePrizeClaimedStatusById(prizeId);
                                        wheelDatabase.updatePlayerMoney(playerUUID.toString(), playerBalance + prizeAmount);
                                        player.sendMessage(net.md_5.bungee.api.ChatColor.of("#63c132") + "На ваш счёт было зачислено " + net.md_5.bungee.api.ChatColor.of("#ffa630") + prizeAmount + "§fꐽ");
                                        player.openInventory(WheelMenu.openWheelMenu(player, wheelDatabase));
                                    }
                                } catch (SQLException e) {
                                }

                            default:
                                break;
                        }
                    }
                }
            }
        } else if (event.getView().getTitle().equals("Обмен монет")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            int slot = event.getRawSlot();

            if (event.getClickedInventory() != null && event.getCurrentItem() != null) {
                ItemStack currentItem = event.getCurrentItem();
                if (currentItem.hasItemMeta() && currentItem.getItemMeta().hasCustomModelData()) {
                    int customModelData = currentItem.getItemMeta().getCustomModelData();
                    if (customModelData == 777) {
                        switch (slot) {
                            case 21:
                                ExchangeBuyHandler exchangeBuyHandler = new ExchangeBuyHandler(wheelDatabase, externalDatabaseManager, transactionDatabaseManager, activePlayers);
                                exchangeBuyHandler.startBuyInput(player);
                                break;
                            case 23:
                                ExchangeSellHandler exchangeSellHandler = new ExchangeSellHandler(wheelDatabase, externalDatabaseManager, transactionDatabaseManager, activePlayers);
                                exchangeSellHandler.startSellInput(player);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }
    }

    private void handleBetInput(Player player, String color, int spinNumber, InventoryAction action) throws SQLException {

        if(wheelDatabase.getLastUnclaimedPrizeIdByPlayerUUID(player.getUniqueId().toString()) != null) {
            player.sendMessage(net.md_5.bungee.api.ChatColor.of("#d64933") + "Сперва заберите приз!");
        } else if (activePlayers.contains(player.getUniqueId())) {
            player.sendMessage(net.md_5.bungee.api.ChatColor.of("#d64933") + "Вы уже начали процесс ввода.");
        } else if(CasinoWheelMine.getInstance().getCurrentIsSpeen()) {
            player.sendMessage(net.md_5.bungee.api.ChatColor.of("#d64933") + "Дождитесь пока прокрутится колесо!");
        } else if (action == InventoryAction.PICKUP_ALL && wheelDatabase.hasBetOnColorAndSpin(player.getUniqueId().toString(), color, spinNumber)) {
            player.closeInventory();
            NewBetInputHandler newBetInputHandler = new NewBetInputHandler(wheelDatabase, activePlayers);
            newBetInputHandler.startBetInput(player, color, spinNumber);
        } else if (action == InventoryAction.PICKUP_HALF && wheelDatabase.hasBetOnColorAndSpin(player.getUniqueId().toString(), color, spinNumber)) {
            player.closeInventory();
            Integer betId = wheelDatabase.getBetIdByPlayerAndColor(player.getUniqueId().toString(), color, spinNumber);
            Integer betAmount = wheelDatabase.getBetAmountById(betId);
            wheelDatabase.deleteBetById(betId);
            int playerBalance = wheelDatabase.getPlayerMoney(player.getUniqueId().toString());
            wheelDatabase.updatePlayerMoney(player.getUniqueId().toString(), playerBalance + betAmount);
            player.openInventory(WheelMenu.openWheelMenu(player, wheelDatabase));
        } else {
            BetInputHandler betInputHandler = new BetInputHandler(wheelDatabase, activePlayers);
            betInputHandler.startBetInput(player, color, spinNumber);
        }
    }
}

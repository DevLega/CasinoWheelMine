package me.lega.casino.casinoWheelMine.Handlers;

import me.lega.casino.casinoWheelMine.CasinoWheelMine;
import me.lega.casino.casinoWheelMine.Database.WheelDatabase;
import me.lega.casino.casinoWheelMine.Menu.WheelMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

public class BetInputHandler {
    private WheelDatabase wheelDatabase;
    private Set<UUID> activePlayers;


    public BetInputHandler(WheelDatabase wheelDatabase, Set<UUID> activePlayers) {
        this.wheelDatabase = wheelDatabase;
        this.activePlayers = activePlayers;
    }

    public void startBetInput(Player player, String color, Integer spinNumber) {

        UUID playerUUID = player.getUniqueId();
        if (activePlayers.contains(playerUUID)) {
            player.sendMessage(net.md_5.bungee.api.ChatColor.of("#d64933") + "Вы уже начали процесс ввода.");
            return;
        }
        activePlayers.add(playerUUID);
        player.closeInventory();

        player.sendMessage(net.md_5.bungee.api.ChatColor.of("#adb5bd") + "Напишите сумму:");
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerChat(AsyncPlayerChatEvent event) throws SQLException {

                if(event.getPlayer() != player) return;

                event.setCancelled(true);

                String amount = event.getMessage();

                player.sendMessage(net.md_5.bungee.api.ChatColor.of("#adb5bd") + "- " + amount);

                int betAmount;
                try {
                    betAmount = Integer.parseInt(amount);
                } catch (NumberFormatException e) {
                    player.sendMessage(net.md_5.bungee.api.ChatColor.of("#d64933") + "Введите корректное число");
                    AsyncPlayerChatEvent.getHandlerList().unregister(this);
                    activePlayers.remove(playerUUID);
                    return;
                }

                if(betAmount <= 0) {
                    player.sendMessage(net.md_5.bungee.api.ChatColor.of("#d64933") + "Сумма должна быть больше 0!");
                    AsyncPlayerChatEvent.getHandlerList().unregister(this);
                    activePlayers.remove(playerUUID);
                    return;
                }

                int playerBalance = wheelDatabase.getPlayerMoney(player.getUniqueId().toString());
                if (playerBalance < betAmount) {
                    player.sendMessage(net.md_5.bungee.api.ChatColor.of("#d64933") + "Не достаточно средств");
                    AsyncPlayerChatEvent.getHandlerList().unregister(this);
                    activePlayers.remove(playerUUID);
                    return;
                }

                if(CasinoWheelMine.getInstance().getCurrentIsSpeen()) {
                    player.sendMessage(net.md_5.bungee.api.ChatColor.of("#d64933") + "Дождитесь пока прокрутится колесо!");
                    AsyncPlayerChatEvent.getHandlerList().unregister(this);
                    activePlayers.remove(playerUUID);
                    return;
                }

                Bukkit.getScheduler().runTask(CasinoWheelMine.getInstance(), () -> {

                    try {
                        wheelDatabase.addBet(player.getUniqueId().toString(), player.getName(), Integer.parseInt(amount), color, spinNumber);
                        wheelDatabase.updatePlayerMoney(player.getUniqueId().toString(), playerBalance - betAmount);
                        player.openInventory(WheelMenu.openWheelMenu(player, wheelDatabase));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                });
                activePlayers.remove(playerUUID);
                AsyncPlayerChatEvent.getHandlerList().unregister(this);
            }
        }, CasinoWheelMine.getInstance());
    }
}

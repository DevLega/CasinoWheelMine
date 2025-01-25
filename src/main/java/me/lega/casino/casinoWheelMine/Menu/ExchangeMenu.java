package me.lega.casino.casinoWheelMine.Menu;

import me.lega.casino.casinoWheelMine.Database.WheelDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.util.List;

public class ExchangeMenu {
    public static Inventory openExchangeMenu(Player player, WheelDatabase wheelDatabase) throws SQLException {
        Inventory inventory = Bukkit.createInventory(null, 27, "Обмен монет");

        ItemStack wheelInfo = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta wheelInfoMeta = wheelInfo.getItemMeta();
        wheelInfoMeta.setCustomModelData(777);
        wheelInfoMeta.setDisplayName("§fИнформация");
        List<String> infoLore = List.of(
                net.md_5.bungee.api.ChatColor.of("#adb5bd") + "§f• Курс 1АР = 7 монет ",
                net.md_5.bungee.api.ChatColor.of("#adb5bd") + "• 10% от каждого обмена",
                net.md_5.bungee.api.ChatColor.of("#adb5bd") + "идут в казну"
        );
        wheelInfoMeta.setLore(infoLore);
        wheelInfo.setItemMeta(wheelInfoMeta);

        ItemStack whiteSlot = new ItemStack(Material.PAPER);
        ItemMeta whiteSlotMeta = whiteSlot.getItemMeta();
        whiteSlotMeta.setCustomModelData(777);
        whiteSlotMeta.setDisplayName(net.md_5.bungee.api.ChatColor.of("#ffa630") + "Купить");
        whiteSlotMeta.setLore(List.of(net.md_5.bungee.api.ChatColor.of("#63c132") + "→ Нажмите что-бы купить монеты"));
        whiteSlot.setItemMeta(whiteSlotMeta);

        ItemStack greenSlot = new ItemStack(Material.PAPER);
        ItemMeta greenSlotMeta = greenSlot.getItemMeta();
        greenSlotMeta.setCustomModelData(777);
        greenSlotMeta.setDisplayName(net.md_5.bungee.api.ChatColor.of("#ffa630") + "Продать");
        greenSlotMeta.setLore(List.of(net.md_5.bungee.api.ChatColor.of("#d64933") + "→ Нажмите что-бы продать монеты"));
        greenSlot.setItemMeta(greenSlotMeta);

        inventory.setItem(4, wheelInfo);
        inventory.setItem(21, whiteSlot);
        inventory.setItem(23, greenSlot);
        return inventory;
    }
}

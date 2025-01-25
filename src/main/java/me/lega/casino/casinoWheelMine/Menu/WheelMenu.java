package me.lega.casino.casinoWheelMine.Menu;

import me.lega.casino.casinoWheelMine.CasinoWheelMine;
import me.lega.casino.casinoWheelMine.Database.WheelDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.util.List;

public class WheelMenu {

    public static Inventory openWheelMenu(Player player, WheelDatabase wheelDatabase) throws SQLException {
        Inventory inventory = Bukkit.createInventory(null, 36, "§f<shift:-8>ꐚ<shift:-158>ꐿ");

        ItemStack whiteSlot = new ItemStack(Material.CLAY_BALL);
        ItemMeta whiteSlotMeta = whiteSlot.getItemMeta();
        whiteSlotMeta.setCustomModelData(777);
        whiteSlotMeta.setDisplayName("§fБелый " + net.md_5.bungee.api.ChatColor.of("#ffa630") + "x2");
        whiteSlotMeta.setLore(List.of(net.md_5.bungee.api.ChatColor.of("#30f1ff") + "→ Нажмите что-бы поставить"));
        whiteSlot.setItemMeta(whiteSlotMeta);

        ItemStack greenSlot = new ItemStack(Material.CLAY_BALL);
        ItemMeta greenSlotMeta = greenSlot.getItemMeta();
        greenSlotMeta.setCustomModelData(777);
        greenSlotMeta.setDisplayName(net.md_5.bungee.api.ChatColor.of("#63c132") + "Зелёный " + net.md_5.bungee.api.ChatColor.of("#ffa630") + "x3");
        greenSlotMeta.setLore(List.of(net.md_5.bungee.api.ChatColor.of("#30f1ff") + "→ Нажмите что-бы поставить"));
        greenSlot.setItemMeta(greenSlotMeta);

        ItemStack blueSlot = new ItemStack(Material.CLAY_BALL);
        ItemMeta blueSlotMeta = blueSlot.getItemMeta();
        blueSlotMeta.setCustomModelData(777);
        blueSlotMeta.setDisplayName("§3Синий " + net.md_5.bungee.api.ChatColor.of("#ffa630") + "x5");
        blueSlotMeta.setLore(List.of(net.md_5.bungee.api.ChatColor.of("#30f1ff") + "→ Нажмите что-бы поставить"));
        blueSlot.setItemMeta(blueSlotMeta);

        ItemStack violetSlot = new ItemStack(Material.CLAY_BALL);
        ItemMeta violetSlotMeta = violetSlot.getItemMeta();
        violetSlotMeta.setCustomModelData(777);
        violetSlotMeta.setDisplayName(net.md_5.bungee.api.ChatColor.of("#a630ff") + "Пурпурный " + net.md_5.bungee.api.ChatColor.of("#ffa630") + "x8");
        violetSlotMeta.setLore(List.of(net.md_5.bungee.api.ChatColor.of("#30f1ff") + "→ Нажмите что-бы поставить"));
        violetSlot.setItemMeta(violetSlotMeta);

        ItemStack redSlot = new ItemStack(Material.CLAY_BALL);
        ItemMeta redSlotMeta = redSlot.getItemMeta();
        redSlotMeta.setCustomModelData(777);
        redSlotMeta.setDisplayName(net.md_5.bungee.api.ChatColor.of("#ff3f30") + "Красный " + net.md_5.bungee.api.ChatColor.of("#ffa630") + "x12");
        redSlotMeta.setLore(List.of(net.md_5.bungee.api.ChatColor.of("#30f1ff") + "→ Нажмите что-бы поставить"));
        redSlot.setItemMeta(redSlotMeta);

        ItemStack wheelInfo = new ItemStack(Material.CLAY_BALL);
        ItemMeta wheelInfoMeta = wheelInfo.getItemMeta();
        wheelInfoMeta.setCustomModelData(777);
        wheelInfoMeta.setDisplayName("§fИнформация");
        List<String> infoLore = List.of(
                net.md_5.bungee.api.ChatColor.of("#adb5bd") + "• Что-бы сделать ставку нажмите на цвет",
                net.md_5.bungee.api.ChatColor.of("#adb5bd") + "и укажите сумму",
                "§f• Ставить можно сразу на несколько цветов",
                net.md_5.bungee.api.ChatColor.of("#adb5bd") + "• При выиграше вам не возращается",
                net.md_5.bungee.api.ChatColor.of("#adb5bd") + "ставка",
                "§fПример: ставка 5 монет на зелёный,",
                "§fзначит вернётся 5*3"
        );
        wheelInfoMeta.setLore(infoLore);
        wheelInfo.setItemMeta(wheelInfoMeta);

        Integer spinNumberInt = CasinoWheelMine.getInstance().getSpinNumber();
        String playerUUID = player.getUniqueId().toString();

        inventory.setItem(11, whiteSlot);
        inventory.setItem(12, greenSlot);
        inventory.setItem(13, blueSlot);
        inventory.setItem(14, violetSlot);
        inventory.setItem(15, redSlot);

        String[] colors = {"white", "green", "blue", "violet", "red"};
        int[] slots = {11, 12, 13, 14, 15};

        for (int i = 0; i < colors.length; i++) {
            try {
                Integer betId = wheelDatabase.getBetIdByPlayerAndColor(playerUUID, colors[i], spinNumberInt);
                Integer betAmount = wheelDatabase.getBetAmountById(betId);
                if (betAmount > 0) {
                    ItemStack betItem = new ItemStack(Material.PAPER);
                    ItemMeta betMeta = betItem.getItemMeta();
                    betMeta.setCustomModelData(777);
                    betMeta.setDisplayName(net.md_5.bungee.api.ChatColor.of("#ffa630") + "Ваша ставка: " + betAmount + "§fꐽ");
                    List<String> infoBetLore = List.of(
                            net.md_5.bungee.api.ChatColor.of("#63c132") + "→ Нажмите" + net.md_5.bungee.api.ChatColor.of("#ffa630") + " ЛКМ " + net.md_5.bungee.api.ChatColor.of("#63c132") + "что-бы изменить ставку",
                            net.md_5.bungee.api.ChatColor.of("#30f1ff") + "→ Нажмите" + net.md_5.bungee.api.ChatColor.of("#ffa630") + " ПКМ " + net.md_5.bungee.api.ChatColor.of("#30f1ff") + "что-бы убрать ставку"
                    );
                    betMeta.setLore(infoBetLore);
                    betItem.setItemMeta(betMeta);
                    inventory.setItem(slots[i], betItem);
                }
            } catch (SQLException e) {
            }
        }

        try {
            Integer prizeId = wheelDatabase.getLastUnclaimedPrizeIdByPlayerUUID(player.getUniqueId().toString());
            if (prizeId != null) {
                Integer prizeAmount = wheelDatabase.getPrizeAmountByPrizeId(prizeId);
                ItemStack prize = new ItemStack(Material.PAPER);
                ItemMeta prizeMeta = prize.getItemMeta();
                prizeMeta.setCustomModelData(777);
                prizeMeta.setDisplayName(net.md_5.bungee.api.ChatColor.of("#ffa630") + "Ваш выигрыш: " + prizeAmount + "§fꐽ");
                prizeMeta.setLore(List.of(net.md_5.bungee.api.ChatColor.of("#63c132") + "→ Нажмите" + net.md_5.bungee.api.ChatColor.of("#ffa630") + " ЛКМ/ПКМ " + net.md_5.bungee.api.ChatColor.of("#63c132") + "что-бы забрать выигрыш"));
                prize.setItemMeta(prizeMeta);

                inventory.setItem(31, prize);
            }
        } catch (SQLException e) {
        }

        inventory.setItem(29, wheelInfo);

        return inventory;
    }
}

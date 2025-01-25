package me.lega.casino.casinoWheelMine.Utils;

import me.lega.casino.casinoWheelMine.Database.WheelDatabase;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;

public class ActionBarTask extends BukkitRunnable {
    private final WheelDatabase wheelDatabase;

    public ActionBarTask(WheelDatabase wheelDatabase) {
        this.wheelDatabase = wheelDatabase;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                // Получаем текущие координаты игрока
                int x = player.getLocation().getBlockX();
                int y = player.getLocation().getBlockY();
                int z = player.getLocation().getBlockZ();

                // Проверяем, находится ли игрок в заданной области
                if (isInRegion(x, y, z, -142, 69, -53, -136, 74, -47)) {
                    // Получаем деньги игрока из базы данных
                    int playerMoney = wheelDatabase.getPlayerMoney(player.getUniqueId().toString());

                    // Формируем сообщение для Action Bar
                    TextComponent actionBarMessagePlayer = new TextComponent("§f" + playerMoney + "ꐽ");

                    // Отправляем сообщение
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, actionBarMessagePlayer);
                }
            } catch (SQLException e) {
                Bukkit.getLogger().severe("Ошибка при получении денег игрока из базы данных: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                Bukkit.getLogger().severe("Произошла непредвиденная ошибка: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Проверяет, находится ли точка в пределах указанного прямоугольного объема.
     */
    private boolean isInRegion(int x, int y, int z, int x1, int y1, int z1, int x2, int y2, int z2) {
        return x >= Math.min(x1, x2) && x <= Math.max(x1, x2)
                && y >= Math.min(y1, y2) && y <= Math.max(y1, y2)
                && z >= Math.min(z1, z2) && z <= Math.max(z1, z2);
    }
}

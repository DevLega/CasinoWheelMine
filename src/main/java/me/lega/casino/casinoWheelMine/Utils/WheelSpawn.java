package me.lega.casino.casinoWheelMine.Utils;

import me.lega.casino.casinoWheelMine.CasinoWheelMine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Transformation;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class WheelSpawn {
    private CasinoWheelMine plugin;
    private ItemDisplay wheel;
    private ItemDisplay pimpochka;

    public WheelSpawn(CasinoWheelMine plugin) {
        this.plugin = plugin;
    }

    public void spawnItem() {
        // Создание объекта в указанной локации
        Location location = new Location(Bukkit.getWorld("world"), -138, 71.1, -50.500);
        wheel = location.getWorld().spawn(location, ItemDisplay.class);

        // Настройка ItemStack с моделью
        ItemStack itemStack = new ItemStack(Material.PAPER);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setCustomModelData(52);
        itemStack.setItemMeta(meta);
        wheel.setItemStack(itemStack);

        // Получение текущей трансформации
        Transformation transformation = wheel.getTransformation();

        // Установка масштаба
        transformation.getScale().set(1.7f);

        // Применение новой трансформации
        wheel.setTransformation(transformation);

        // Запуск задачи вращения


    }

    public void spawnPimpochka() {
        Location location = new Location(Bukkit.getWorld("world"), -138, 73.500, -50.500);
        pimpochka = (ItemDisplay) location.getWorld().spawnEntity(location, EntityType.ITEM_DISPLAY);

        ItemStack itemStack = new ItemStack(Material.PAPER);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setCustomModelData(53);
        itemStack.setItemMeta(meta);
        pimpochka.setItemStack(itemStack);

        Matrix4f scaleMatrix = new Matrix4f().scaling(2f, 2f, 2.5f);
        Matrix4f rotationMatrix = new Matrix4f().rotateY((float) Math.toRadians(0));
        Matrix4f finalMatrix = new Matrix4f().mul(scaleMatrix).mul(rotationMatrix);
        pimpochka.setTransformationMatrix(finalMatrix);
    }

    public void removeItem() {
        if (wheel != null && !wheel.isDead()) {
            wheel.remove();
        }
    }

    public void removePimpochka() {
        if (pimpochka != null && !pimpochka.isDead()) {
            pimpochka.remove();
        }
    }

    public ItemDisplay getWheel() {
        return wheel;
    }
}

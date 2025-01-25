package me.lega.casino.casinoWheelMine.Utils;

import me.lega.casino.casinoWheelMine.CasinoWheelMine;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;

import java.util.Random;

public class WheelRotationTask {
    private final ItemDisplay wheel;
    private double angle = 0; // Начальный угол
    private final long duration; // Длительность анимации
    private long elapsedTime = 0; // Время, прошедшее с начала анимации
    private double totalRotation = 0; // Общий угол поворота
    private double currentAngle = 0; // Глобальный текущий угол
    private String sectorColor;

    public WheelRotationTask(ItemDisplay wheel, long duration) {
        this.wheel = wheel;
        Random random = new Random();
        long randomDuration = 1000 + random.nextInt(3000);
        this.duration = duration + randomDuration;
        this.currentAngle = CasinoWheelMine.getInstance().getCurrentAngle();
    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                double progress = (double) elapsedTime / duration;

                if (progress >= 1.0) {
                    currentAngle %= 360;
                    if (currentAngle < 0) {
                        currentAngle += 360;
                    }

                    double initialOffset = 0;
                    double adjustedAngle = (currentAngle + initialOffset) % 360;

                    int totalSectors = 16;
                    double sectorAngle = 360.0 / totalSectors;
                    int sector = (int) ((adjustedAngle + (sectorAngle / 2)) % 360 / sectorAngle);

                    // Определяем цвет сектора
                    String[] colors = {"red", "white", "green", "white", "blue", "white", "green", "white",
                            "violet", "white", "green", "white", "blue", "white", "green", "white"};
                    sectorColor = colors[sector];
                    CasinoWheelMine.getInstance().saveCurrentAngle(currentAngle);
                    CasinoWheelMine.getInstance().saveCurrentColor(sectorColor);
                    System.out.println(sectorColor);
                    System.out.println(currentAngle);
                    this.cancel();
                    return;
                }

                double easeInOut = -0.5 * (Math.cos(Math.PI * progress) - 1);

                if (progress > 0.7) {
                    easeInOut *= Math.pow(1 - (progress - 0.7) / 0.4, 2); // Замедление
                }

                double rotationSpeed = 20 * easeInOut;
                angle += rotationSpeed;
                totalRotation += rotationSpeed;

                currentAngle += rotationSpeed;
                if (currentAngle >= 360) {
                    currentAngle %= 360;
                }

                Transformation transformation = wheel.getTransformation();
                transformation.getLeftRotation().rotateLocalX((float) Math.toRadians(rotationSpeed));
                wheel.setTransformation(transformation);

                elapsedTime += 50;
            }
        }.runTaskTimer(CasinoWheelMine.getInstance(), 0, 1); // Шаг 1 тик
    }

    public String getSectorColor() {
        return sectorColor;
    }

    public long getDuration() {
        return duration;
    }
}


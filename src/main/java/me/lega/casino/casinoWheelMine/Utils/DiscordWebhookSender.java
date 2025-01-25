package me.lega.casino.casinoWheelMine.Utils;

import org.bukkit.Bukkit;

import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.property.MojangSkinDataResult;
import net.skinsrestorer.api.property.SkinIdentifier;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.property.SkinType;
import net.skinsrestorer.api.storage.PlayerStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class DiscordWebhookSender {
    private static final String WEBHOOK_URL = "https://discord.com/api/webhooks/1329867569297096744/umM-OjQXKotL4YRkwSepV1K59wnTgSpQ4ESsnWDLdVYaUuQWT_M1I80-dBvbHpQN4nuE";

    public static String escapeJson(String input) {
        return input.replace("\"", "\\\"")
                .replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static void sendWebhook(String title, String description, String thumbnailUrl, int color, String content) {
        try {
            URL url = new URL(WEBHOOK_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            title = escapeJson(title);
            description = escapeJson(description);
            thumbnailUrl = escapeJson(thumbnailUrl);
            if (content != null) {
                content = escapeJson(content);
            }

            StringBuilder jsonPayload = new StringBuilder("{");
            if (content != null && !content.isEmpty()) {
                jsonPayload.append("\"content\": \"").append(content).append("\",");
            }
            jsonPayload.append("\"embeds\": [{")
                    .append("\"title\": \"").append(title).append("\",")
                    .append("\"description\": \"").append(description).append("\",")
                    .append("\"thumbnail\": {\"url\": \"").append(thumbnailUrl).append("\"},")
                    .append("\"color\": ").append(color)
                    .append("}]")
                    .append("}");

            System.out.println("Отправляем JSON: " + jsonPayload);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 204) {
                System.out.println("Ошибка при отправке вебхука: " + connection.getResponseMessage());
            } else {
                System.out.println("Вебхук успешно отправлен!");
            }

        } catch (Exception e) {
            System.err.println("Ошибка при отправке вебхука: " + e.getMessage());
        }
    }

    private static String getSkinUrl(String username) {

        UUID userPlayer = Bukkit.getOfflinePlayer(username).getUniqueId();
        String skinRestorerName = getSkinRestorerSkin(userPlayer, username);
        if (skinRestorerName != null) {
            return "https://vzge.me/bust/256/" + skinRestorerName + ".png?y=-40";
        }

        String apiUrl = "https://api.mojang.com/users/profiles/minecraft/" + username;
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(apiUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return "https://vzge.me/bust/256/" + username + ".png?y=-40";
            } else {
                return getRandomDefaultSkin();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "https://example.com/default-skin.png";
        }
    }

    private static final SkinsRestorer skinsRestorerAPI = SkinsRestorerProvider.get();

    private static String getSkinRestorerSkin(UUID playerUUID, String username) {
        if (playerUUID == null || username == null) {
            throw new IllegalArgumentException("playerUUID and username cannot be null");
        }

        PlayerStorage playerStorage = skinsRestorerAPI.getPlayerStorage();
        try {
            Optional<SkinProperty> property = playerStorage.getSkinForPlayer(playerUUID, username);

            if (property.isPresent()) {
                SkinProperty skinProperty = property.get();
                String skinName = PropertyUtils.getSkinProfileData(skinProperty).getProfileName();
                return skinName;
            } else {
                return null;
            }
        } catch (DataRequestException e) {
            e.printStackTrace();
            // Логируем ошибку
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String getRandomDefaultSkin() {
        String[] defaultSkins = {
                "https://vzge.me/bust/256/Steve3.png?y=-40",
                "https://vzge.me/bust/256/Steve6.png?y=-40",
                "https://vzge.me/bust/256/X-Noor.png?y=-40",
                "https://vzge.me/bust/256/X-Ari.png?y=-40",
                "https://vzge.me/bust/256/X-Kai.png?y=-40",
                "https://vzge.me/bust/256/X-Sunny.png?y=-40"
        };
        Random random = new Random();
        int index = random.nextInt(defaultSkins.length);
        return defaultSkins[index];
    }

    public static void sendCasinoWin(String playerName, int prizeAmount, int betAmount, String betColor, int playerBalance) {
        String skinUrl = getSkinUrl(playerName);
        String title = ":ferris_wheel: Выигрыш в рулетке";
        String description = "`" + playerName + "`" + " выиграл в рулетке " + prizeAmount + " монет\nПоставив " + betAmount + " на цвет " + betColor + "\nТеперь баланс игрока составляет - " + playerBalance;
        int color = 5620992;
        String content = "";

        sendWebhook(title, description, skinUrl, color, content);
    }

    public static void sendCasinoLoose(String playerName, int betAmount, int playerBalance) {
        String skinUrl = getSkinUrl(playerName);
        String title = ":ferris_wheel: Проигрыш в рулетке";
        String description = "`" + playerName + "`" + " проиграл в рулетке " + betAmount + " монет\nТеперь баланс игрока составляет - " + playerBalance;
        int color = 8388608;
        String content = "";

        sendWebhook(title, description, skinUrl, color, content);
    }

}

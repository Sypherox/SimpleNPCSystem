package Sypherox.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

public class SkinFetcher {

    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/%s";
    private static final String TEXTURE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";

    public static CompletableFuture<SkinData> fetchSkin(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String uuid = getUUID(playerName);
                if (uuid == null) {
                    return null;
                }

                return getTextures(uuid);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    private static String getUUID(String playerName) throws IOException {
        URL url = new URL(String.format(UUID_URL, playerName));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        if (connection.getResponseCode() != 200) {
            return null;
        }

        JsonObject json = JsonParser.parseReader(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
        return json.get("id").getAsString();
    }

    private static SkinData getTextures(String uuid) throws IOException {
        URL url = new URL(String.format(TEXTURE_URL, uuid));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        if (connection.getResponseCode() != 200) {
            return null;
        }

        JsonObject json = JsonParser.parseReader(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
        JsonObject properties = json.getAsJsonArray("properties").get(0).getAsJsonObject();

        String texture = properties.get("value").getAsString();
        String signature = properties.get("signature").getAsString();

        return new SkinData(texture, signature);
    }

    public static class SkinData {
        private final String texture;
        private final String signature;

        public SkinData(String texture, String signature) {
            this.texture = texture;
            this.signature = signature;
        }

        public String getTexture() {
            return texture;
        }

        public String getSignature() {
            return signature;
        }
    }
}

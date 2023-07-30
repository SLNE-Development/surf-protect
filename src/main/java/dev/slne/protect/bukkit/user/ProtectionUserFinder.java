package dev.slne.protect.bukkit.user;

import com.google.gson.JsonObject;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import dev.slne.data.core.gson.GsonConverter;
import dev.slne.data.core.web.WebRequest;
import dev.slne.protect.bukkit.instance.BukkitApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Utils for {@link LocalPlayer}s
 */
public class ProtectionUserFinder {

    /**
     * Utility class
     */
    private ProtectionUserFinder() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Find a user by their UUID
     *
     * @param uuid The UUID of the user
     *
     * @return The user
     */
    public static LocalPlayer findLocalPlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            return WorldGuardPlugin.inst().wrapPlayer(player);
        }

        return WorldGuardPlugin.inst().wrapOfflinePlayer(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Find a user by their playerName
     *
     * @param playerName The playerName of the user
     *
     * @return The user
     */
    public static LocalPlayer findLocalPlayer(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player != null && player.isOnline()) {
            return WorldGuardPlugin.inst().wrapPlayer(player);
        }

        return WorldGuardPlugin.inst().wrapOfflinePlayer(Bukkit.getOfflinePlayer(playerName));
    }

    /**
     * Gets the UUID of a player by their minecraftName
     *
     * @param playerName the minecraftName of the player
     *
     * @return the UUID of the player
     */
    public static CompletableFuture<UUID> getUuidByPlayerName(String playerName) {
        CompletableFuture<UUID> future = new CompletableFuture<>();

        getUuidMinecraftName(playerName).thenAcceptAsync(uuidMinecraftName -> {
            if (uuidMinecraftName == null) {
                future.completeExceptionally(new NullPointerException("uuidMinecraftName is null"));
                return;
            }

            future.complete(uuidMinecraftName.uuid());
        }).exceptionally(throwable -> {
            future.completeExceptionally(throwable);

            return null;
        });

        return future;
    }

    /**
     * Gets the minecraftName of a player by their uuid
     *
     * @param uuid the uuid of the player
     *
     * @return the minecraftName of the player
     */
    public static CompletableFuture<String> getPlayerNameByUuid(UUID uuid) {
        CompletableFuture<String> future = new CompletableFuture<>();

        getUuidMinecraftName(uuid).thenAcceptAsync(uuidMinecraftName -> {
            if (uuidMinecraftName == null) {
                future.completeExceptionally(new NullPointerException("uuidMinecraftName is null"));
                return;
            }

            future.complete(uuidMinecraftName.minecraftName());
        }).exceptionally(throwable -> {
            future.completeExceptionally(throwable);

            return null;
        });

        return future;
    }

    /**
     * Returns the uuidMinecraftName of a player by their uuid or minecraft minecraftName
     *
     * @param uuidOrMinecraftName the uuid or minecraft minecraftName
     *
     * @return the uuidMinecraftName of a player by their uuid or minecraft minecraftName
     */
    @SuppressWarnings("java:S3776")
    private static CompletableFuture<UuidMinecraftName> getUuidMinecraftName(Object uuidOrMinecraftName) {
        CompletableFuture<UuidMinecraftName> future = new CompletableFuture<>();

        UuidMinecraftName cacheHit =
                BukkitApi.getInstance().getUuidMinecraftNameCache().hitCache(uuidOrMinecraftName);

        if (cacheHit != null) {
            return CompletableFuture.completedFuture(cacheHit);
        }

        String queryString = null;

        if (uuidOrMinecraftName instanceof UUID uuid) {
            queryString = uuid.toString();
        } else if (uuidOrMinecraftName instanceof String minecraftName) {
            queryString = minecraftName.trim().replace(" ", "");
        }

        if (queryString == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("uuidOrMinecraftName is not a UUID or " +
                    "minecraftName"));
        }

        WebRequest request = WebRequest.builder().url("https://api.minetools.eu/uuid/" + queryString).json(true)
                .build();

        request.executeGet().thenAcceptAsync(response -> {
            int statusCode = response.statusCode();

            if (!(statusCode >= 200 && statusCode < 300)) {
                future.completeExceptionally(new IllegalStateException("statusCode is not 2xx"));
                return;
            }

            Object responseBody = response.body();
            String responseString = responseBody != null ? responseBody.toString() : null;

            if (responseString == null) {
                future.completeExceptionally(new NullPointerException("responseString is null"));
                return;
            }

            GsonConverter gson = new GsonConverter();
            JsonObject bodyObject = gson.fromJson(responseString, JsonObject.class);

            if (!bodyObject.has("id") || bodyObject.get("id").isJsonNull()
                    || bodyObject.get("id").getAsString().isEmpty()) {
                future.completeExceptionally(new NullPointerException("id is null"));
                return;
            }

            if (!bodyObject.has("name") || bodyObject.get("name").isJsonNull()
                    || bodyObject.get("name").getAsString().isEmpty()) {
                future.completeExceptionally(new NullPointerException("name is null"));
                return;
            }

            String uuidString = bodyObject.get("id").getAsString();
            UUID uuid = getDashedUuidNonDashedUuid(uuidString);
            String name = bodyObject.get("name").getAsString();

            future.complete(BukkitApi.getInstance().getUuidMinecraftNameCache().setCache(uuid, name));
        }).exceptionally(throwable -> {
            future.completeExceptionally(throwable);

            return null;
        });

        return future;
    }

    /**
     * Converts a short UUID to a dashed UUID
     *
     * @param shortUuid the short UUID
     *
     * @return the dashed UUID
     */
    private static UUID getDashedUuidNonDashedUuid(String shortUuid) {
        return UUID.fromString(shortUuid.substring(0, 8) + "-" + shortUuid.substring(8, 12) + "-"
                + shortUuid.substring(12, 16) + "-" + shortUuid.substring(16, 20) + "-" + shortUuid.substring(20, 32));
    }

}

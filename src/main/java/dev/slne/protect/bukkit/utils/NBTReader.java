package dev.slne.protect.bukkit.utils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.message.MessageManager;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.text.Component;

public class NBTReader {

    // Location

    public static void getLocation(String name, NBTCallback<Location> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(BukkitMain.getInstance(), () -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(name);
            try {
                Location location = getLocation(player);
                if (location == null) {
                    callback.onFail(Component.text("Der Spieler ", MessageManager.ERROR)
                            .append(Component.text(player.getName(), MessageManager.VARIABLE_VALUE))
                            .append(Component.text(" wurde nicht gefunden!", MessageManager.ERROR)));
                    return;
                }
                callback.onSuccess(location);
            } catch (IOException e) {
                callback.onFail(
                        Component.text("Bei der Teleportation ist ein Fehler aufgetreten!", MessageManager.ERROR));
                e.printStackTrace();
            }
        });
    }

    public static void setLocation(String name, Location newLoc, NBTCallback<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(BukkitMain.getInstance(), () -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(name);
            try {
                if (!setLocation(player, newLoc)) {
                    callback.onFail(Component.text("Der Spieler ", MessageManager.ERROR)
                            .append(Component.text(player.getName(), MessageManager.VARIABLE_VALUE))
                            .append(Component.text(" wurde nicht gefunden!", MessageManager.ERROR)));
                    return;
                }
                callback.onSuccess(true);
            } catch (IOException e) {
                e.printStackTrace();
                callback.onFail(
                        Component.text("Bei der Teleportation ist ein Fehler aufgetreten!", MessageManager.ERROR));
            }
        });
    }

    private static Location getLocation(OfflinePlayer player) throws IOException {
        UUID uuid = player.getUniqueId();
        File dataFile = getPlayerFile(uuid);

        if (dataFile == null)
            return null;
        CompoundBinaryTag tag = BinaryTagIO.unlimitedReader().read(dataFile.toPath(), BinaryTagIO.Compression.GZIP);
        ListBinaryTag posTag = tag.getList("Pos");
        ListBinaryTag rotTag = tag.getList("Rotation");
        long worldUUIDMost = tag.getLong("WorldUUIDMost");
        long worldUUIDLeast = tag.getLong("WorldUUIDLeast");

        World world = Bukkit.getWorld(new UUID(worldUUIDMost, worldUUIDLeast));

        return new Location(world, posTag.getDouble(0), posTag.getDouble(1), posTag.getDouble(2),
                rotTag.getFloat(0), rotTag.getFloat(1));
    }

    private static boolean setLocation(OfflinePlayer player, Location location) throws IOException {
        UUID uuid = player.getUniqueId();
        File dataFile = getPlayerFile(uuid);

        if (dataFile == null)
            return false;
        CompoundBinaryTag rawTag = BinaryTagIO.unlimitedReader().read(dataFile.toPath(), BinaryTagIO.Compression.GZIP);
        CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder().put(rawTag);

        ListBinaryTag.Builder<BinaryTag> posTag = ListBinaryTag.builder();
        posTag.add(DoubleBinaryTag.of(location.getX()));
        posTag.add(DoubleBinaryTag.of(location.getY()));
        posTag.add(DoubleBinaryTag.of(location.getZ()));

        ListBinaryTag.Builder<BinaryTag> rotTag = ListBinaryTag.builder();
        rotTag.add(FloatBinaryTag.of(location.getYaw()));
        rotTag.add(FloatBinaryTag.of(location.getPitch()));

        builder.put("Pos", posTag.build());
        builder.put("Rotation", rotTag.build());

        BinaryTagIO.writer().write(builder.build(), dataFile.toPath(), BinaryTagIO.Compression.GZIP);
        return true;
    }

    // GameMode

    public static void setGameMode(String name, GameMode gameMode, NBTCallback<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(BukkitMain.getInstance(), () -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(name);
            try {
                if (!setGameMode(player, gameMode)) {
                    callback.onFail(Component.text("Der Spieler ", MessageManager.ERROR)
                            .append(Component.text(player.getName(), MessageManager.VARIABLE_VALUE))
                            .append(Component.text(" wurde nicht gefunden!", MessageManager.ERROR)));
                    return;
                }
                callback.onSuccess(true);
            } catch (IOException e) {
                e.printStackTrace();
                callback.onFail(Component.text("Bei der Abfrage ist ein Fehler aufgetreten!", MessageManager.ERROR));
            }
        });
    }

    @SuppressWarnings("deprecation")
    private static boolean setGameMode(OfflinePlayer player, GameMode gameMode) throws IOException {
        UUID uuid = player.getUniqueId();
        File dataFile = getPlayerFile(uuid);

        if (dataFile == null)
            return false;
        CompoundBinaryTag rawTag = BinaryTagIO.unlimitedReader().read(dataFile.toPath(), BinaryTagIO.Compression.GZIP);
        CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder().put(rawTag);

        builder.put("playerGameType", IntBinaryTag.of(gameMode.getValue()));

        BinaryTagIO.writer().write(builder.build(), dataFile.toPath(), BinaryTagIO.Compression.GZIP);
        return true;
    }

    // GodMode

    public static void switchInvulnerable(String name, NBTCallback<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(BukkitMain.getInstance(), () -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(name);
            try {
                Boolean invulnerable = isInvulnerable(player);

                if (invulnerable == null) {
                    callback.onFail(Component.text("Der Spieler ", MessageManager.ERROR)
                            .append(Component.text(player.getName(), MessageManager.VARIABLE_VALUE))
                            .append(Component.text(" wurde nicht gefunden!", MessageManager.ERROR)));
                    return;
                }

                setInvulnerable(player, (byte) (invulnerable ? 0 : 1));
                callback.onSuccess(!invulnerable);
            } catch (IOException e) {
                e.printStackTrace();
                callback.onFail(
                        Component.text("Der Godmode Status konnte nicht gesetzt werden!", MessageManager.ERROR));
            }
        });
    }

    private static Boolean isInvulnerable(OfflinePlayer player) throws IOException {
        UUID uuid = player.getUniqueId();
        File dataFile = getPlayerFile(uuid);

        if (dataFile == null)
            return null;
        CompoundBinaryTag tag = BinaryTagIO.unlimitedReader().read(dataFile.toPath(), BinaryTagIO.Compression.GZIP);
        return tag.getBoolean("Invulnerable");
    }

    private static void setInvulnerable(OfflinePlayer player, Byte invulnerable) throws IOException {
        UUID uuid = player.getUniqueId();
        File dataFile = getPlayerFile(uuid);

        if (dataFile == null)
            return;
        CompoundBinaryTag rawTag = BinaryTagIO.unlimitedReader().read(dataFile.toPath(), BinaryTagIO.Compression.GZIP);
        CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder().put(rawTag);

        builder.put("Invulnerable", ByteBinaryTag.of(invulnerable));

        BinaryTagIO.writer().write(builder.build(), dataFile.toPath(), BinaryTagIO.Compression.GZIP);
    }

    // Utils

    private static File getPlayerFile(UUID uuid) {
        for (World world : Bukkit.getWorlds()) {
            File worldFolder = world.getWorldFolder();
            if (!worldFolder.isDirectory())
                continue;
            File[] children = worldFolder.listFiles();
            if (children == null)
                continue;
            for (File file : children) {
                if (!file.isDirectory() || !file.getName().equals("playerdata"))
                    continue;
                return getPlayerFile(file, uuid);
            }
        }
        return null;
    }

    private static File getPlayerFile(File playerDataFolder, UUID uuid) {
        File[] files = playerDataFolder.listFiles();
        if (files == null)
            return null;
        for (File file : files) {
            if (file.getName().equals(uuid.toString() + ".dat"))
                return file;
        }
        return null;
    }

    public interface NBTCallback<D> {

        void onSuccess(D data);

        default void onFail(Component message) {
        }
    }
}
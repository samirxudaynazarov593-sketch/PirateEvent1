package com.articraft.pirate;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.EditSession;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class PirateManager {

    private final PirateEventPlugin plugin;
    private PirateEvent currentEvent = null;
    private final Random random = new Random();

    public PirateManager(PirateEventPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean hasActiveEvent() {
        return currentEvent != null && currentEvent.isActive();
    }

    public PirateEvent getCurrentEvent() {
        return currentEvent;
    }

    public void startEvent() {
        if (hasActiveEvent()) {
            stopEvent(false);
        }

        String worldName = plugin.getConfig().getString("settings.world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().severe("Configdagi dunyo topilmadi: " + worldName);
            return;
        }

        int minX = plugin.getConfig().getInt("settings.min-x", -2000);
        int maxX = plugin.getConfig().getInt("settings.max-x", 2000);
        int minZ = plugin.getConfig().getInt("settings.min-z", -2000);
        int maxZ = plugin.getConfig().getInt("settings.max-z", 2000);

        int x = random.nextInt(maxX - minX + 1) + minX;
        int z = random.nextInt(maxZ - minZ + 1) + minZ;
        int groundY = world.getHighestBlockYAt(x, z);
        int heightOffset = plugin.getConfig().getInt("settings.height-above-ground", 100);
        int y = Math.min(groundY + heightOffset, world.getMaxHeight() - 30);

        Location shipOrigin = new Location(world, x, y, z);
        Set<Location> shipBlocks = new HashSet<>();

        // WorldEdit orqali schem paste qilish va bloklarni ro'yxatga olish
        File schemFile = new File(plugin.getDataFolder(), plugin.getConfig().getString("settings.schematic-file", "ship.schem"));
        if (!schemFile.exists()) {
            plugin.getLogger().severe("ship.schem fayli mavjud emas, event bekor qilindi!");
            return;
        }

        ClipboardFormat format = ClipboardFormats.findByFile(schemFile);
        if (format == null) return;

        try (ClipboardReader reader = format.getReader(new FileInputStream(schemFile))) {
            Clipboard clipboard = reader.read();
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
                Operation operation = new com.sk89q.worldedit.schematic.ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(x, y, z))
                        .ignoreAirBlocks(true)
                        .build();
                Operations.complete(operation);

                BlockVector3 min = clipboard.getMinimumPoint();
                BlockVector3 max = clipboard.getMaximumPoint();
                BlockVector3 offset = BlockVector3.at(x, y, z).subtract(clipboard.getOrigin());

                for (int bx = min.getBlockX(); bx <= max.getBlockX(); bx++) {
                    for (int by = min.getBlockY(); by <= max.getBlockY(); by++) {
                        for (int bz = min.getBlockZ(); bz <= max.getBlockZ(); bz++) {
                            int realX = bx + offset.getBlockX();
                            int realY = by + offset.getBlockY();
                            int realZ = bz + offset.getBlockZ();
                            Block b = world.getBlockAt(realX, realY, realZ);
                            if (b.getType() != Material.AIR) {
                                shipBlocks.add(b.getLocation());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Suv liftini kema tubidan yer sathigacha tushirish
        Set<Location> waterLift = new HashSet<>();
        for (int curY = y - 1; curY >= groundY; curY--) {
            Block b = world.getBlockAt(x, curY, z);
            b.setType(Material.WATER);
            waterLift.add(b.getLocation());
        }

        this.currentEvent = new PirateEvent(plugin, shipOrigin, shipBlocks, waterLift);
        this.currentEvent.spawnTreasureAndCleanUp();

        // E'lon berish
        Bukkit.broadcastMessage(plugin.color(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.event-started")));
    }

    public void stopEvent(boolean notify) {
        if (currentEvent != null) {
            currentEvent.cleanup();
            currentEvent = null;
            if (notify) {
                Bukkit.broadcastMessage(plugin.color(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.event-stopped")));
            }
        }
    }
}

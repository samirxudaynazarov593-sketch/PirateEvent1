package com.articraft.pirate;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

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

        // 🏴‍☠️ HAGIQIY QAROQCHILAR KEMASI KORPUSINI YASASH (OVAL / BURUNLI QAYIQ SHAKLI)
        int length = 16; // Kema uzunligi
        int width = 5;   // Kema eni

        for (int l = -length / 2; l <= length / 2; l++) {
            // Kema eni oval shaklda bo'lishi uchun (burun va quyruq qismi torayadi)
            int currentWidth = (int) (width * Math.sin(((double) (l + length / 2) / length) * Math.PI));
            if (currentWidth < 1) currentWidth = 1;

            for (int w = -currentWidth; w <= currentWidth; w++) {
                // Paluba (Taglik)
                Block deck = world.getBlockAt(x + w, y, z + l);
                deck.setType(Material.DARK_OAK_PLANKS);
                shipBlocks.add(deck.getLocation());

                // Tagidagi qavat (Qalinligi uchun)
                Block subDeck = world.getBlockAt(x + w, y - 1, z + l);
                subDeck.setType(Material.SPRUCE_WOOD);
                shipBlocks.add(subDeck.getLocation());

                // Bortlar (Fens / Devorlar - faqat chetlariga)
                if (Math.abs(w) == currentWidth || l == -length / 2 || l == length / 2) {
                    Block fence = world.getBlockAt(x + w, y + 1, z + l);
                    fence.setType(Material.NETHER_BRICK_FENCE);
                    shipBlocks.add(fence.getLocation());
                }
            }
        }

        // Kema Machtasi (Dastasi) va Yelka qismi
        for (int mY = 1; mY <= 7; mY++) {
            Block mast = world.getBlockAt(x, y + mY, z);
            mast.setType(Material.OAK_LOG);
            shipBlocks.add(mast.getLocation());
        }
        // Yelka (Jun qismi)
        for (int mY = 3; mY <= 6; mY++) {
            for (int mW = -2; mW <= 2; mW++) {
                Block sail = world.getBlockAt(x + mW, y + mY, z);
                sail.setType(Material.BLACK_WOOL);
                shipBlocks.add(sail.getLocation());
            }
        }

        // Suv liftini kema tubidan yer sathigacha tushirish
        Set<Location> waterLift = new HashSet<>();
        for (int curY = y - 1; curY >= groundY; curY--) {
            Block waterBlock = world.getBlockAt(x, curY, z);
            waterBlock.setType(Material.WATER);
            waterLift.add(waterBlock.getLocation());
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

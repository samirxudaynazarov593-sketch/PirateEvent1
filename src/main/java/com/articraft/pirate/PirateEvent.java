package com.articraft.pirate;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class PirateEvent {

    private final PirateEventPlugin plugin;
    private final Location origin;
    private final Set<Location> shipBlocks;
    private final Set<Location> waterLiftBlocks;
    private Location treasureLocation;

    private Shulker shulkerEntity;
    private boolean isTimerRunning = false;
    private boolean isUnlocked = false;
    private final List<UUID> spawnedRaiders = new ArrayList<>();
    private BukkitTask timerTask = null;

    public PirateEvent(PirateEventPlugin plugin, Location origin, Set<Location> shipBlocks, Set<Location> waterLiftBlocks) {
        this.plugin = plugin;
        this.origin = origin;
        this.shipBlocks = shipBlocks;
        this.waterLiftBlocks = waterLiftBlocks;
    }

    public boolean isActive() {
        return true;
    }

    public Location getOrigin() {
        return origin;
    }

    public Location getTreasureLocation() {
        return treasureLocation;
    }

    public boolean isShipBlock(Location loc) {
        return shipBlocks.contains(loc) || waterLiftBlocks.contains(loc);
    }

    public Shulker getShulkerEntity() {
        return shulkerEntity;
    }

    public boolean isTimerRunning() {
        return isTimerRunning;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void spawnTreasureAndCleanUp() {
        World world = origin.getWorld();
        if (world == null) return;

        int offX = plugin.getConfig().getInt("settings.treasure-offset.x", 0);
        int offY = plugin.getConfig().getInt("settings.treasure-offset.y", 1);
        int offZ = plugin.getConfig().getInt("settings.treasure-offset.z", 3);
        this.treasureLocation = origin.clone().add(offX, offY, offZ);

        // Kema ichidagi keraksiz bloklar va narsalarni tozalash (Chest, Elytra, Brewing Stand)
        for (Location loc : shipBlocks) {
            Block b = loc.getBlock();
            if (b.getType() == Material.CHEST || b.getType() == Material.BREWING_STAND || b.getType() == Material.TRAPPED_CHEST) {
                b.setType(Material.AIR);
            }
        }

        // Kema atrofidagi Item Frame va adashib qolgan yovvoyi shulkerlarni tozalash
        for (Entity e : world.getNearbyEntities(origin, 35, 35, 35)) {
            if (e instanceof ItemFrame || (e instanceof Shulker && !e.equals(shulkerEntity))) {
                e.remove();
            }
        }

        // Qimirlamaydigan, zararsiz va o'lmaydigan maxsus Shulker yaratish
        this.shulkerEntity = (Shulker) world.spawnEntity(treasureLocation, EntityType.SHULKER);
        this.shulkerEntity.setAI(false);
        this.shulkerEntity.setInvulnerable(false); // Urish mumkin bo'lishi kerak
        this.shulkerEntity.setSilent(true);
        this.shulkerEntity.setCustomName(plugin.color("&c&lQaroqchilar Xazinasi &7(Uring!)"));
        this.shulkerEntity.setCustomNameVisible(true);
    }

    public void startTreasureUnlock(Player triggerPlayer) {
        if (isTimerRunning || isUnlocked) return;
        this.isTimerRunning = true;

        Bukkit.broadcastMessage(plugin.color(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.timer-start")));

        // 4 ta kuchli Reyder moblarni chaqirish
        World world = treasureLocation.getWorld();
        EntityType[] raiders = {EntityType.VINDICATOR, EntityType.PILLAGER, EntityType.EVOKER, EntityType.WITCH};
        for (EntityType type : raiders) {
            Entity raider = world.spawnEntity(treasureLocation.clone().add(1.5, 0, 1.5), type);
            spawnedRaiders.add(raider.getUniqueId());
            if (raider instanceof Mob mob) {
                mob.setTarget(triggerPlayer);
            }
        }

        // TextDisplay Hologramni yaratish
        plugin.getHologramManager().createTimerHologram(treasureLocation);

        int totalSeconds = plugin.getConfig().getInt("settings.treasure-timer", 180);
        this.timerTask = new BukkitRunnable() {
            int left = totalSeconds;

            @Override
            public void run() {
                if (left <= 0) {
                    finishUnlock();
                    cancel();
                    return;
                }
                plugin.getHologramManager().updateTimer(left);
                left--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void finishUnlock() {
        this.isTimerRunning = false;
        this.isUnlocked = true;

        // Hologramni o'chirish
        plugin.getHologramManager().removeHologram();

        // Shulker entityni o'chirib, o'rniga haqiqiy Shulker Box blokini qo'yish
        Location boxLoc = shulkerEntity.getLocation().getBlock().getLocation();
        shulkerEntity.remove();

        Block block = boxLoc.getBlock();
        block.setType(Material.RED_SHULKER_BOX);

        if (block.getState() instanceof ShulkerBox shulkerBox) {
            plugin.getLootManager().fillInventory(shulkerBox.getInventory());
        }

        Bukkit.broadcastMessage(plugin.color(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.unlocked")));
    }

    public void cleanup() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        plugin.getHologramManager().removeHologram();

        if (shulkerEntity != null && !shulkerEntity.isDead()) {
            shulkerEntity.remove();
        }

        // Qolgan reyderlarni yo'q qilish
        World world = origin.getWorld();
        if (world != null) {
            for (UUID id : spawnedRaiders) {
                Entity e = Bukkit.getEntity(id);
                if (e != null && !e.isDead()) {
                    e.remove();
                }
            }
        }

        // Kema bloklarini havoga aylantirish (Rollback)
        for (Location loc : shipBlocks) {
            loc.getBlock().setType(Material.AIR);
        }
        // Suv liftini tozalash
        for (Location loc : waterLiftBlocks) {
            loc.getBlock().setType(Material.AIR);
        }

        if (treasureLocation != null) {
            treasureLocation.getBlock().setType(Material.AIR);
        }
    }
}

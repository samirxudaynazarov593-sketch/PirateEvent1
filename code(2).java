package com.articraft.pirate;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

public class PirateListener implements Listener {

    private final PirateEventPlugin plugin;

    public PirateListener(PirateEventPlugin plugin) {
        this.plugin = plugin;
    }

    // Kema bloklarini va suv liftini buzishdan himoya qilish
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        PirateEvent current = plugin.getPirateManager().getCurrentEvent();
        if (current == null) return;

        if (current.isShipBlock(event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.color(plugin.getConfig().getString("messages.no-break")));
        }
    }

    // Shulkerga o'yinchi urganda xazina ochilishini boshlash
    @EventHandler
    public void onShulkerDamage(EntityDamageByEntityEvent event) {
        PirateEvent current = plugin.getPirateManager().getCurrentEvent();
        if (current == null) return;

        if (event.getEntity() instanceof Shulker shulker && shulker.equals(current.getShulkerEntity())) {
            event.setCancelled(true); // O'lib ketmasligi uchun zararni bekor qilamiz

            if (event.getDamager() instanceof Player player) {
                handleShulkerInteraction(player, current);
            }
        }
    }

    // Shulkerga o'ng tugma bilan bosganda ham ishlashi
    @EventHandler
    public void onShulkerRightClick(PlayerInteractEntityEvent event) {
        PirateEvent current = plugin.getPirateManager().getCurrentEvent();
        if (current == null) return;

        if (event.getRightClicked() instanceof Shulker shulker && shulker.equals(current.getShulkerEntity())) {
            event.setCancelled(true);
            handleShulkerInteraction(event.getPlayer(), current);
        }
    }

    private void handleShulkerInteraction(Player player, PirateEvent event) {
        if (!event.isTimerRunning() && !event.isUnlocked()) {
            event.startTreasureUnlock(player);
        } else if (event.isTimerRunning()) {
            player.sendMessage(plugin.color(plugin.getConfig().getString("messages.timer-waiting").replace("%time%", "" + 10)));
        }
    }

    // Kompas GUI bosilishini nazorat qilish
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(plugin.color("&8Qaroqchilar Kemasi Navigatsiyasi"))) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() != Material.COMPASS) return;

            Player player = (Player) event.getWhoClicked();
            PirateEvent current = plugin.getPirateManager().getCurrentEvent();

            if (current != null) {
                giveCompass(player, current.getOrigin());
                player.sendMessage(plugin.color(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.compass-given")));
            } else {
                player.sendMessage(plugin.color(plugin.getConfig().getString("messages.no-active-event")));
            }
            player.closeInventory();
        }
    }

    private void giveCompass(Player player, Location target) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        CompassMeta meta = (CompassMeta) compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.color("&c&lQaroqchilar Kemasini Izlovchi Kompas"));
            meta.setLodestoneTracked(false);
            meta.setLodestone(target);
            compass.setItemMeta(meta);
        }
        player.getInventory().addItem(compass);
    }
}
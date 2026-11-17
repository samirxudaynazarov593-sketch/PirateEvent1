package com.articraft.pirate;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class PirateCommand implements CommandExecutor {

    private final PirateEventPlugin plugin;

    public PirateCommand(PirateEventPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("pirate")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("start")) {
                plugin.getPirateManager().startEvent();
                sender.sendMessage(plugin.color("&aQaroqchilar kemasi eventi boshlandi!"));
                return true;
            }
            if (args.length > 0 && args[0].equalsIgnoreCase("stop")) {
                plugin.getPirateManager().stopEvent(true);
                sender.sendMessage(plugin.color("&cQaroqchilar kemasi eventi to'xtatildi!"));
                return true;
            }
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfig();
                plugin.getLootManager().loadLoot();
                sender.sendMessage(plugin.color("&aPirateEvent sozlamalari yangilandi!"));
                return true;
            }

            sender.sendMessage(plugin.color("&e&l================ [ PIRATE EVENT ] ================"));
            sender.sendMessage(plugin.color("&b/pirate start &7- Yangi uchar kemani RTP bilan chiqarish"));
            sender.sendMessage(plugin.color("&b/pirate stop  &7- Faol kemani tozalash va to'xtatish"));
            sender.sendMessage(plugin.color("&b/pirate reload&7- Config sozlamalarini yangilash"));
            sender.sendMessage(plugin.color("&e&l================================================="));
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("piratecompass")) {
            if (sender instanceof Player player) {
                openCompassMenu(player);
            }
            return true;
        }

        return false;
    }

    private void openCompassMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, plugin.color("&8Qaroqchilar Kemasi Navigatsiyasi"));

        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.color("&c&lKema Kompasini Olish"));
            meta.setLore(Collections.singletonList(plugin.color("&7Bosing va kema tomon olib boruvchi kompasni qo'lga kiriting!")));
            compass.setItemMeta(meta);
        }

        inv.setItem(4, compass);
        player.openInventory(inv);
    }
}

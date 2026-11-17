package com.articraft.pirate;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class LootManager {

    private final PirateEventPlugin plugin;
    private final List<ItemStack> lootItems = new ArrayList<>();

    public LootManager(PirateEventPlugin plugin) {
        this.plugin = plugin;
        loadLoot();
    }

    public void loadLoot() {
        lootItems.clear();
        List<String> list = plugin.getConfig().getStringList("loot");
        for (String entry : list) {
            try {
                String[] parts = entry.split(":");
                Material mat = Material.valueOf(parts[0].toUpperCase());
                int amount = Integer.parseInt(parts[1]);
                lootItems.add(new ItemStack(mat, amount));
            } catch (Exception e) {
                plugin.getLogger().warning("Lootni o'qishda xatolik: " + entry);
            }
        }
    }

    public void fillInventory(Inventory inv) {
        inv.clear();
        for (ItemStack item : lootItems) {
            inv.addItem(item.clone());
        }
    }
}

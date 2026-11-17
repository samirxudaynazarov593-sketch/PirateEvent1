package com.articraft.pirate;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class PirateEventPlugin extends JavaPlugin {

    private static PirateEventPlugin instance;
    private PirateManager pirateManager;
    private LootManager lootManager;
    private HologramManager hologramManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // plugins/PirateEvent/ papkasida ship.schem borligini tekshirish
        File schemFile = new File(getDataFolder(), getConfig().getString("settings.schematic-file", "ship.schem"));
        if (!schemFile.exists()) {
            getLogger().warning("======================================================");
            getLogger().warning("[PirateEvent] " + schemFile.getName() + " topilmadi!");
            getLogger().warning("[PirateEvent] Iltimos, o'zingizning schematic faylingizni");
            getLogger().warning("[PirateEvent] " + getDataFolder().getPath() + " ichiga tashlang!");
            getLogger().warning("======================================================");
        }

        // Modullarni ishga tushirish
        this.lootManager = new LootManager(this);
        this.hologramManager = new HologramManager();
        this.pirateManager = new PirateManager(this);

        // Komandalar va hodisalarni ro'yxatdan o'tkazish
        getCommand("pirate").setExecutor(new PirateCommand(this));
        getCommand("piratecompass").setExecutor(new PirateCommand(this));
        getServer().getPluginManager().registerEvents(new PirateListener(this), this);

        getLogger().info("PirateEvent muvaffaqiyatli ishga tushdi!");
    }

    @Override
    public void onDisable() {
        if (pirateManager != null && pirateManager.hasActiveEvent()) {
            pirateManager.stopEvent(false);
        }
        getLogger().info("PirateEvent o'chirildi!");
    }

    public static PirateEventPlugin getInstance() {
        return instance;
    }

    public PirateManager getPirateManager() {
        return pirateManager;
    }

    public LootManager getLootManager() {
        return lootManager;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}

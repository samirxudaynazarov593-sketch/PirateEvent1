package com.articraft.pirate;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;

public class HologramManager {

    private TextDisplay timerDisplay = null;

    public void createTimerHologram(Location location) {
        removeHologram(); // Eskisi bo'lsa o'chiramiz

        Location holoLoc = location.clone().add(0.5, 1.8, 0.5);
        this.timerDisplay = (TextDisplay) holoLoc.getWorld().spawnEntity(holoLoc, EntityType.TEXT_DISPLAY);
        this.timerDisplay.setBillboard(Display.Billboard.CENTER);
        this.timerDisplay.setDefaultBackground(false);
        this.timerDisplay.setShadowed(true);
        this.timerDisplay.setText(ChatColor.RED + "" + ChatColor.BOLD + "XAZINA OCHILMOQDA...\n" + ChatColor.YELLOW + "Iltimos kuting!");
    }

    public void updateTimer(int secondsLeft) {
        if (timerDisplay != null && !timerDisplay.isDead()) {
            int minutes = secondsLeft / 60;
            int seconds = secondsLeft % 60;
            String timeFormat = String.format("%02d:%02d", minutes, seconds);

            String text = ChatColor.RED + "" + ChatColor.BOLD + "QAROQCHILAR XAZINASI\n" +
                          ChatColor.GOLD + "Ochilishiga: " + ChatColor.WHITE + timeFormat + "\n" +
                          ChatColor.GRAY + "Reyderlarni mag'lub eting!";
            this.timerDisplay.setText(text);
        }
    }

    public void removeHologram() {
        if (timerDisplay != null && !timerDisplay.isDead()) {
            timerDisplay.remove();
            timerDisplay = null;
        }
    }
}

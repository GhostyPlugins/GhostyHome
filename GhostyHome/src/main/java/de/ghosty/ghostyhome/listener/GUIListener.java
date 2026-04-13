package de.ghosty.ghostyhome.listener;

import de.ghosty.ghostyhome.gui.AdminHomeGUI;
import de.ghosty.ghostyhome.gui.HomeGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class GUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof HomeGUI homeGUI) {
            event.setCancelled(true);
            if (event.getClickedInventory() == null) return;
            if (!event.getClickedInventory().equals(event.getInventory())) return;
            homeGUI.handleClick(event.getRawSlot(), event.isShiftClick());
        } else if (holder instanceof AdminHomeGUI adminGUI) {
            event.setCancelled(true);
            if (event.getClickedInventory() == null) return;
            if (!event.getClickedInventory().equals(event.getInventory())) return;
            adminGUI.handleClick(event.getRawSlot());
        }
    }
}

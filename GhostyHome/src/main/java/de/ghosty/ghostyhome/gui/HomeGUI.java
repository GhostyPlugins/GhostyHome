package de.ghosty.ghostyhome.gui;

import de.ghosty.ghostyhome.GhostyHome;
import de.ghosty.ghostyhome.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class HomeGUI implements InventoryHolder {

    private final GhostyHome  plugin;
    private final Player      viewer;
    private final UUID        targetUUID;
    private final String      targetName;
    private Inventory         inventory;
    private int               page;
    private final List<String> homeNames;

    private static final int[] HOME_SLOTS = {
            10,11,12,13,14,15,16,
            19,20,21,22,23,24,25,
            28,29,30,31,32,33,34
    };
    private static final int HOMES_PER_PAGE = HOME_SLOTS.length;
    private static final int SLOT_PREV = 45, SLOT_INFO = 49, SLOT_NEXT = 53;

    public HomeGUI(GhostyHome plugin, Player viewer, UUID targetUUID, String targetName) {
        this.plugin     = plugin;
        this.viewer     = viewer;
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        this.page       = 0;
        this.homeNames  = new ArrayList<>(plugin.getHomeManager().getHomes(targetUUID).keySet());
        Collections.sort(homeNames);
    }

    public void open() { buildInventory(); viewer.openInventory(inventory); }

    private void buildInventory() {
        boolean isAdmin = !viewer.getUniqueId().equals(targetUUID);
        String title = isAdmin
                ? ColorUtil.colorize(plugin.getLangManager().get("gui.admin-title", targetName))
                : ColorUtil.colorize(plugin.getLangManager().get("gui.title"));

        inventory = Bukkit.createInventory(this, 54, title);
        ItemStack filler = item(Material.GRAY_STAINED_GLASS_PANE, plugin.getLangManager().get("gui.filler-name"));
        for (int i = 0; i < 54; i++) inventory.setItem(i, filler);

        Map<String, Location> homes = plugin.getHomeManager().getHomes(targetUUID);
        int start = page * HOMES_PER_PAGE;
        for (int i = 0; i < HOMES_PER_PAGE; i++) {
            int idx = start + i;
            if (idx >= homeNames.size()) break;
            String name = homeNames.get(idx);
            inventory.setItem(HOME_SLOTS[i], homeItem(name, homes.get(name), isAdmin));
        }

        int total = Math.max(1, (int) Math.ceil((double) homeNames.size() / HOMES_PER_PAGE));
        inventory.setItem(SLOT_PREV, page > 0
                ? item(Material.ARROW, ColorUtil.colorize(plugin.getLangManager().get("gui.prev-page"))) : filler);
        inventory.setItem(SLOT_INFO, item(Material.BOOK,
                ColorUtil.colorize(plugin.getLangManager().get("gui.page-info", page + 1, total))));
        inventory.setItem(SLOT_NEXT, page < total - 1
                ? item(Material.ARROW, ColorUtil.colorize(plugin.getLangManager().get("gui.next-page"))) : filler);
    }

    private ItemStack homeItem(String name, Location loc, boolean isAdmin) {
        ItemStack is = new ItemStack(Material.RED_BED);
        ItemMeta  m  = is.getItemMeta(); if (m == null) return is;
        m.setDisplayName(ColorUtil.colorize(plugin.getLangManager().get("gui.home-item-name", cap(name))));
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.colorize(plugin.getLangManager().get("gui.home-item-world",
                loc.getWorld() != null ? loc.getWorld().getName() : "?")));
        lore.add(ColorUtil.colorize(plugin.getLangManager().get("gui.home-item-coords",
                round(loc.getX()), round(loc.getY()), round(loc.getZ()))));
        lore.add("");
        if (isAdmin)  lore.add(ColorUtil.colorize(plugin.getLangManager().get("gui.admin-click-to-tp")));
        else {
            lore.add(ColorUtil.colorize(plugin.getLangManager().get("gui.click-to-tp")));
            lore.add(ColorUtil.colorize(plugin.getLangManager().get("gui.shift-click-to-delete")));
        }
        m.setLore(lore); is.setItemMeta(m); return is;
    }

    private ItemStack item(Material mat, String name) {
        ItemStack is = new ItemStack(mat); ItemMeta m = is.getItemMeta();
        if (m != null) { m.setDisplayName(name); m.setLore(Collections.emptyList()); is.setItemMeta(m); }
        return is;
    }

    public void handleClick(int slot, boolean isShift) {
        if (slot == SLOT_PREV && page > 0) { page--; buildInventory(); viewer.openInventory(inventory); return; }
        int total = Math.max(1, (int) Math.ceil((double) homeNames.size() / HOMES_PER_PAGE));
        if (slot == SLOT_NEXT && page < total - 1) { page++; buildInventory(); viewer.openInventory(inventory); return; }

        for (int i = 0; i < HOME_SLOTS.length; i++) {
            if (HOME_SLOTS[i] != slot) continue;
            int idx = page * HOMES_PER_PAGE + i;
            if (idx >= homeNames.size()) return;
            String  homeName = homeNames.get(idx);
            boolean isAdmin  = !viewer.getUniqueId().equals(targetUUID);
            if (isAdmin) {
                Location loc = plugin.getHomeManager().getHome(targetUUID, homeName);
                if (loc != null) { viewer.closeInventory(); plugin.getTeleportManager().requestTeleport(viewer, loc, homeName, true); }
            } else if (isShift) {
                viewer.closeInventory();
                plugin.getHomeManager().deleteHome(viewer, homeName);
                viewer.sendMessage(plugin.getLangManager().getPrefixed("cmd.delhome.success", cap(homeName)));
                homeNames.remove(homeName);
            } else {
                Location loc = plugin.getHomeManager().getHome(viewer, homeName);
                if (loc != null) { viewer.closeInventory(); plugin.getTeleportManager().requestTeleport(viewer, loc, homeName, false); }
            }
            return;
        }
    }

    @Override public Inventory getInventory() { return inventory; }
    public Player getViewer() { return viewer; }
    private double round(double v) { return Math.round(v * 10.0) / 10.0; }
    private String cap(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}

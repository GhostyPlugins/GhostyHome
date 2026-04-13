package de.ghosty.ghostyhome.gui;

import de.ghosty.ghostyhome.GhostyHome;
import de.ghosty.ghostyhome.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class AdminHomeGUI implements InventoryHolder {

    private final GhostyHome   plugin;
    private final Player       admin;
    private Inventory          inventory;
    private int                page;
    private final List<Player> onlinePlayers;

    private static final int[] PLAYER_SLOTS = {
            10,11,12,13,14,15,16,
            19,20,21,22,23,24,25,
            28,29,30,31,32,33,34
    };
    private static final int PER_PAGE = PLAYER_SLOTS.length;
    private static final int SLOT_PREV = 45, SLOT_INFO = 49, SLOT_NEXT = 53;

    public AdminHomeGUI(GhostyHome plugin, Player admin) {
        this.plugin        = plugin;
        this.admin         = admin;
        this.page          = 0;
        this.onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    public void open() { buildInventory(); admin.openInventory(inventory); }

    private void buildInventory() {
        String title = ColorUtil.colorize(plugin.getLangManager().get("gui.admin-player-select-title"));
        inventory = Bukkit.createInventory(this, 54, title);
        ItemStack filler = item(Material.GRAY_STAINED_GLASS_PANE, plugin.getLangManager().get("gui.filler-name"));
        for (int i = 0; i < 54; i++) inventory.setItem(i, filler);

        int start = page * PER_PAGE;
        for (int i = 0; i < PER_PAGE; i++) {
            int pi = start + i; if (pi >= onlinePlayers.size()) break;
            inventory.setItem(PLAYER_SLOTS[i], skull(onlinePlayers.get(pi)));
        }

        int total = Math.max(1, (int) Math.ceil((double) onlinePlayers.size() / PER_PAGE));
        inventory.setItem(SLOT_PREV, page > 0
                ? item(Material.ARROW, ColorUtil.colorize(plugin.getLangManager().get("gui.prev-page"))) : filler);
        inventory.setItem(SLOT_INFO, item(Material.BOOK,
                ColorUtil.colorize(plugin.getLangManager().get("gui.page-info", page + 1, total))));
        inventory.setItem(SLOT_NEXT, page < total - 1
                ? item(Material.ARROW, ColorUtil.colorize(plugin.getLangManager().get("gui.next-page"))) : filler);
    }

    private ItemStack skull(Player target) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta(); if (meta == null) return head;
        meta.setOwningPlayer(target);
        meta.setDisplayName(ColorUtil.colorize(plugin.getLangManager().get("gui.admin-player-name", target.getName())));
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.colorize(plugin.getLangManager().get("gui.admin-player-homes",
                plugin.getHomeManager().getHomeCount(target))));
        lore.add("");
        lore.add(ColorUtil.colorize(plugin.getLangManager().get("gui.admin-click-to-view")));
        meta.setLore(lore); head.setItemMeta(meta); return head;
    }

    private ItemStack item(Material mat, String name) {
        ItemStack is = new ItemStack(mat); ItemMeta m = is.getItemMeta();
        if (m != null) { m.setDisplayName(name); m.setLore(Collections.emptyList()); is.setItemMeta(m); }
        return is;
    }

    public void handleClick(int slot) {
        if (slot == SLOT_PREV && page > 0) { page--; buildInventory(); admin.openInventory(inventory); return; }
        int total = Math.max(1, (int) Math.ceil((double) onlinePlayers.size() / PER_PAGE));
        if (slot == SLOT_NEXT && page < total - 1) { page++; buildInventory(); admin.openInventory(inventory); return; }
        for (int i = 0; i < PLAYER_SLOTS.length; i++) {
            if (PLAYER_SLOTS[i] != slot) continue;
            int pi = page * PER_PAGE + i; if (pi >= onlinePlayers.size()) return;
            new HomeGUI(plugin, admin, onlinePlayers.get(pi).getUniqueId(), onlinePlayers.get(pi).getName()).open();
            return;
        }
    }

    @Override public Inventory getInventory() { return inventory; }
}

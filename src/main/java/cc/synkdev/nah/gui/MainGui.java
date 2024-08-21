package cc.synkdev.nah.gui;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.components.BINAuction;
import cc.synkdev.nah.components.SortingTypes;
import cc.synkdev.nah.manager.Lang;
import cc.synkdev.nah.manager.Util;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainGui {
    NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    int max = (core.runningBINs.size()/10)+1;
    public Gui gui(Player p, int page, String search) {
        SortingTypes sort = core.playerSortingTypes.getOrDefault(p, SortingTypes.PRICEMIN);
        Gui gui = Gui.gui()
                .disableAllInteractions()
                .title(Component.text(ChatColor.YELLOW+ Lang.translate("ah")))
                .rows(6)
                .create();
        if (page > 1) gui.setItem(6, 4, arrowLeft(page));
        if (page < max) gui.setItem(6, 6, arrowRight(page));
        gui.setItem(6, 8, sorter(p, page, search));
        if (search == null) fillGui(gui, p, page, sort);
        else fillGui(gui, p, page, search);
        gui.setItem(6, 2, search());
        return gui;
    }
    GuiItem arrowLeft(int page) {
        GuiItem item = ItemBuilder.from(Material.ARROW)
                .name(Component.text(ChatColor.translateAlternateColorCodes('&', "&r&e&l"+Lang.translate("prevPage"))))
                .asGuiItem(inventoryClickEvent -> {
                    Player p = (Player) inventoryClickEvent.getWhoClicked();
                    gui(p, page-1, null).open(p);
                });
        return item;
    }
    GuiItem arrowRight(int page) {
        GuiItem item = ItemBuilder.from(Material.ARROW)
                .name(Component.text(ChatColor.translateAlternateColorCodes('&', "&r&e&l"+Lang.translate("nextPage"))))
                .asGuiItem(inventoryClickEvent -> {
                    Player p = (Player) inventoryClickEvent.getWhoClicked();
                    gui(p, page+1, null).open(p);
                });
        return item;
    }
    private void fillGui(Gui gui, Player p, int page, SortingTypes sort) {
        int min = 45*(page-1);
        int max = 45*page;

        for (int i = min; i < max; i++) {
            if (sort.list.size() > i) gui.setItem(i-min, buyableItem(sort.list.get(i), p.hasPermission("nah.menu.manage")));
        }
    }
    private void fillGui(Gui gui, Player p, int page, String research) {
        int min = 45*(page-1);
        int max = 45*page;

        List<BINAuction> list = new ArrayList<>(Util.searchList(research, core.playerSortingTypes.getOrDefault(p, SortingTypes.PRICEMIN)));
        for (int i = min; i < max; i++) {
            if (list.size() > i) gui.setItem(i-min, buyableItem(list.get(i), p.hasPermission("nah.menu.manage")));
        }
    }
    GuiItem buyableItem(BINAuction bA, Boolean staff) {
        ItemStack copy = bA.getItem().clone();
        List<Component> lore = new ArrayList<>();
        lore.addAll(Util.loreToComps(bA.getItem()));
        lore.addAll(Arrays.asList(Component.text(""), Component.text(ChatColor.YELLOW+"---------------"), Component.text(""), Component.text(ChatColor.YELLOW+ Lang.translate("buyNow", Integer.toString(bA.getPrice()))), Component.text(""), Component.text(ChatColor.YELLOW+Lang.translate("expires")+" "+Util.convertSecondsToTime(bA.getExpiry())), Component.text(""), Component.text(ChatColor.translateAlternateColorCodes('&', "&r&e"+Lang.translate("seller", bA.getSeller().getName())))));
        if (staff) lore.addAll(Arrays.asList(Component.text(""), Component.text(ChatColor.GREEN+"---------------"), Component.text(""), Component.text(ChatColor.GREEN+ Lang.translate("staffMenu"))));
        GuiItem item = ItemBuilder.from(copy)
                .lore(lore)
                .asGuiItem(event -> {
                    Player p = (Player) event.getWhoClicked();
                    if (event.isShiftClick() && p.hasPermission("nah.menu.manage")) {
                        new ManageMenu().gui(bA).open(p);
                        return;
                    }

                    new ConfirmBuyGui().gui(p, bA).open(p);
                });
        return item;
    }
    GuiItem sorter(Player p, int page, String search) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text(ChatColor.RESET+""+ChatColor.YELLOW+Lang.translate("clickScroll")));
        lore.add(Component.text(""));
        for (SortingTypes sT : core.sortingTypes) {
            String arrow = ChatColor.RESET+""+ChatColor.YELLOW+"-> "+ChatColor.BOLD;
            if (core.playerSortingTypes.getOrDefault(p, SortingTypes.PRICEMIN) == sT) lore.add(Component.text(arrow+sT.string));
            else lore.add(Component.text(ChatColor.RESET+""+ChatColor.YELLOW+sT.string));
        }

        GuiItem gui = ItemBuilder.from(Material.HOPPER)
                .name(Component.text(ChatColor.translateAlternateColorCodes('&', "&r&8&lSort")))
                .lore(lore)
                .asGuiItem(event -> {
                    Player pl = (Player) event.getWhoClicked();
                    switch (core.playerSortingTypes.getOrDefault(pl, SortingTypes.PRICEMIN)) {
                        case PRICEMAX:
                            if (!core.playerSortingTypes.containsKey(pl)) core.playerSortingTypes.put(pl, SortingTypes.LATESTPOSTED);
                            else core.playerSortingTypes.replace(pl, SortingTypes.LATESTPOSTED);
                            break;
                        case LATESTPOSTED:
                            if (!core.playerSortingTypes.containsKey(pl)) core.playerSortingTypes.put(pl, SortingTypes.EXPIRESSOON);
                            else core.playerSortingTypes.replace(pl, SortingTypes.EXPIRESSOON);
                            break;
                        case EXPIRESSOON:
                            if (!core.playerSortingTypes.containsKey(pl)) core.playerSortingTypes.put(pl, SortingTypes.PRICEMIN);
                            else core.playerSortingTypes.replace(pl, SortingTypes.PRICEMIN);
                            break;
                        case PRICEMIN:
                            if (!core.playerSortingTypes.containsKey(pl)) core.playerSortingTypes.put(pl, SortingTypes.PRICEMAX);
                            else core.playerSortingTypes.replace(pl, SortingTypes.PRICEMAX);
                            break;
                    }
                    gui(p, page, search).open(p);
                });
        return gui;
    }
    GuiItem search() {
        GuiItem item = ItemBuilder.from(Material.SIGN)
                .name(Component.text(ChatColor.translateAlternateColorCodes('&', "&r&e"+Lang.translate("clickSearch"))))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .asGuiItem(event -> {
                    Player p = (Player) event.getWhoClicked();

                    TextComponent comp = new TextComponent("["+Lang.translate("clickSearch")+"]");
                    comp.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
                    comp.setBold(true);
                    comp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ah search "));

                    p.spigot().sendMessage(comp);
                    p.closeInventory();
                });
        return item;
    }
}

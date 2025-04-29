package cc.synkdev.nah.gui;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.api.NAHUtil;
import cc.synkdev.nah.manager.Util;
import cc.synkdev.nah.objects.BINAuction;
import cc.synkdev.nah.objects.SortingTypes;
import cc.synkdev.synkLibs.bukkit.Lang;
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
    int max = (core.runningBINs.size()+44)/45;
    int page;
    String searchS;
    public Gui gui(Player p, int page, String search) {
        this.page = page;
        this.searchS = search;
        SortingTypes sort = core.playerSortingTypes.getOrDefault(p, SortingTypes.PRICEMIN);
        Gui gui = Gui.gui()
                .disableAllInteractions()
                .title(Component.text(ChatColor.YELLOW+ Lang.translate("ah", core)))
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
        return ItemBuilder.from(Material.ARROW)
                .name(Component.text(ChatColor.translateAlternateColorCodes('&', "&r&e&l"+Lang.translate("prevPage", core))))
                .asGuiItem(inventoryClickEvent -> {
                    Player p = (Player) inventoryClickEvent.getWhoClicked();
                    gui(p, page-1, null).open(p);
                });
    }
    GuiItem arrowRight(int page) {
        return ItemBuilder.from(Material.ARROW)
                .name(Component.text(ChatColor.translateAlternateColorCodes('&', "&r&e&l"+Lang.translate("nextPage", core))))
                .asGuiItem(inventoryClickEvent -> {
                    Player p = (Player) inventoryClickEvent.getWhoClicked();
                    gui(p, page+1, null).open(p);
                });
    }
    private void fillGui(Gui gui, Player p, int page, SortingTypes sort) {
        int min = 45*(page-1);
        int max = 45*page;

        for (int i = min; i < max; i++) {
            if (sort.list.size() > i) {
                BINAuction bA = sort.list.get(i);
                gui.setItem(i - min, buyableItem(bA, p.hasPermission("nah.menu.manage"), bA.getSeller().getUniqueId().equals(p.getUniqueId())));
            }
        }
    }
    private void fillGui(Gui gui, Player p, int page, String research) {
        int min = 45*(page-1);
        int max = 45*page;

        List<BINAuction> list = new ArrayList<>(Util.searchList(research, core.playerSortingTypes.getOrDefault(p, SortingTypes.PRICEMIN)));
        for (int i = min; i < max; i++) {
            if (list.size() > i) {
                BINAuction bA = list.get(i);
                gui.setItem(i-min, buyableItem(bA, p.hasPermission("nah.menu.manage"), bA.getSeller().getUniqueId().equals(p.getUniqueId())));
            }
        }
    }
    GuiItem buyableItem(BINAuction bA, Boolean staff, Boolean self) {
        ItemStack copy = bA.getItem().clone();
        List<Component> lore = new ArrayList<>();
        lore.addAll(Util.loreToComps(bA.getItem()));
        lore.addAll(Arrays.asList(Component.text(""), Component.text(ChatColor.YELLOW+"---------------"), Component.text(""), Component.text(ChatColor.YELLOW+ Lang.translate("buyNow", core, Integer.toString(bA.getPrice()))), Component.text(""), Component.text(ChatColor.YELLOW+Lang.translate("expires", core)+" "+Util.convertSecondsToTime(bA.getExpiry())), Component.text(""), Component.text(ChatColor.translateAlternateColorCodes('&', "&r&e"+Lang.translate("seller", core, bA.getSeller().getName())))));
        if (staff) lore.addAll(Arrays.asList(Component.text(""), Component.text(ChatColor.GREEN+"---------------"), Component.text(""), Component.text(ChatColor.GREEN+ Lang.translate("staffMenu", core))));
        if (self) lore.addAll(Arrays.asList(Component.text(""), Component.text(ChatColor.GREEN+"---------------"), Component.text(""), Component.text(ChatColor.GREEN+ Lang.translate("own-lore", core))));
        return ItemBuilder.from(copy)
                .lore(lore)
                .asGuiItem(event -> {
                    Player p = (Player) event.getWhoClicked();
                    BINAuction bAa = NAHUtil.getAuction(bA.getUuid());
                    if (!bAa.getBuyable()) {
                        p.sendMessage(core.prefix()+Lang.translate("already-bought", core));
                        NAHUtil.open(p, false, searchS, page);
                        return;
                    }

                    if (event.isRightClick() && self) {
                        Gui gui = new ConfirmUnlistGui().gui(p, bAa);
                        gui.open(p);
                        return;
                    }

                    if (event.isShiftClick() && p.hasPermission("nah.menu.manage")) {
                        Gui gui = new ManageMenu().gui(bAa);
                        gui.open(p);
                        return;
                    }

                    Gui gui = new ConfirmBuyGui().gui(bAa);
                    gui.open(p);
                });
    }
    GuiItem sorter(Player p, int page, String search) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text(ChatColor.RESET+""+ChatColor.YELLOW+Lang.translate("clickScroll", core)));
        lore.add(Component.text(""));
        for (SortingTypes sT : core.sortingTypes) {
            String arrow = ChatColor.RESET+""+ChatColor.YELLOW+"-> "+ChatColor.BOLD;
            if (core.playerSortingTypes.getOrDefault(p, SortingTypes.PRICEMIN) == sT) lore.add(Component.text(arrow+sT.string));
            else lore.add(Component.text(ChatColor.RESET+""+ChatColor.YELLOW+sT.string));
        }

        return ItemBuilder.from(Material.HOPPER)
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
    }
    GuiItem search() {
        return ItemBuilder.from(Material.SIGN)
                .name(Component.text(ChatColor.translateAlternateColorCodes('&', "&r&e"+Lang.translate("clickSearch", core))))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .asGuiItem(event -> {
                    Player p = (Player) event.getWhoClicked();

                    TextComponent comp = new TextComponent("["+Lang.translate("clickSearch", core)+"]");
                    comp.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
                    comp.setBold(true);
                    comp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ah search "));

                    p.spigot().sendMessage(comp);
                    p.closeInventory();
                });
    }
}

package cc.synkdev.nah.gui;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.api.NAHUtil;
import cc.synkdev.nah.manager.Util;
import cc.synkdev.nah.objects.BINAuction;
import cc.synkdev.nah.objects.ItemSort;
import cc.synkdev.nah.objects.SortingTypes;
import cc.synkdev.nexusCore.bukkit.Lang;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.util.GuiFiller;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainGui {
    NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    int max = (core.runningBINs.size()+39)/40;
    int page;
    String searchS;
    int firstSort;
    ItemSort itSort;
    Player p;
    MainGuiSnapshot snapshot;
    public Gui gui(Player p, int page, String search, int firstSort, ItemSort itSort) {
        snapshot = new MainGuiSnapshot(page, search, firstSort, itSort);
        this.p = p;
        core.checkExpiry.run();
        this.page = page;
        this.searchS = search;
        this.firstSort = firstSort;
        this.itSort = itSort;
        SortingTypes sort = core.playerSortingTypes.getOrDefault(p.getUniqueId(), SortingTypes.PRICEMIN);
        Gui gui = Gui.gui()
                .disableAllInteractions()
                .title(Component.text(ChatColor.YELLOW+Lang.translate("ah", core)))
                .rows(6)
                .create();

        gui.getFiller().fillBottom(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).asGuiItem());
        if (!core.itemSorts.isEmpty()) gui.getFiller().fillSide(GuiFiller.Side.LEFT, List.of(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).asGuiItem()));
        if (page > 1) {
            gui.setItem(6, 4, arrowLeft(page));
        }
        if (page < max) {
            gui.setItem(6, 6, arrowRight(page));
        }
        gui.setItem(6, 8, sorter(p, page, search));

        if (firstSort > 0) {
            gui.setItem(1, 1, ItemBuilder.from(Material.ARROW).name(Component.text(Lang.translate("sorts", core))).lore(Component.empty(), Component.text(Lang.translate("scrollSorts", core))).asGuiItem(event -> gui(p, page, search, firstSort-1, itSort).open(p)));
        }

        boolean useFirst = firstSort<=0;
        if (core.itemSorts.size() > 5 && core.itemSorts.size() >= firstSort+6) {
            gui.setItem(6, 1, ItemBuilder.from(Material.ARROW).name(Component.text(Lang.translate("sorts", core))).lore(Component.empty(), Component.text(Lang.translate("scrollSorts", core))).asGuiItem(event -> gui(p, page, search, firstSort+1, itSort).open(p)));
        }
        int index = firstSort;
        for (int i = useFirst ? 1 : 2; i < 6; i++) {
            if (core.itemSorts.size() <= index) break;

            ItemSort iSort = core.itemSorts.entrySet().stream().toList().get(index).getValue();
            boolean same = iSort == itSort;
            gui.setItem(i, 1, ItemBuilder.from(iSort.getIcon()).glow(same).flags(ItemFlag.HIDE_ATTRIBUTES).name(Component.text(ChatColor.YELLOW+iSort.getName())).lore(Component.empty(), Component.text(Lang.translate((same ? "clickUnsort" : "clickSort"), core))).asGuiItem(event -> {
                if (same) {
                    gui(p, page, search, firstSort, null).open(p);
                } else {
                    gui(p, page, search, firstSort, iSort).open(p);
                }

            }));
            index++;
        }

        if (search != null) {
            fillGui(gui, p, page, search);
        } else if (!core.itemSorts.isEmpty()) {
            fillGui(gui, p, page, sort, itSort);
        } else {
            fillGui(gui, p, page, sort);
        }

        gui.setItem(6, 2, search());
        gui.setItem(6, 9, ItemBuilder.from(Material.CHEST)
                .name(Component.text(ChatColor.GOLD+Lang.translate("titleRetrieve", core)))
                        .lore(Component.text(""), Component.text("  "+Lang.translate("retrieveCount", core, core.retrieveMap.getOrDefault(p.getUniqueId(), new ArrayList<>()).size()+"")), Component.text(""), Component.text(Lang.translate("clickBrowse", core)))
                .asGuiItem(event -> {
                    NAHUtil.openExpiredGui(p);
                }));
        if (p.hasPermission("nah.menu.player.own")) {
                gui.setItem(6, 5, ItemBuilder.skull().owner(p)
                        .name(Component.text(Lang.translate("viewOwn", core)))
                        .lore(Component.empty(), Component.text(Lang.translate("clickBrowse", core)))
                        .asGuiItem(event -> {
                            if (event.getWhoClicked().hasPermission("nah.menu.player.own")) {
                                NAHUtil.openPlayerListings(p, p, new MainGuiSnapshot(page, search, firstSort, itSort));
                            } else {
                                event.getWhoClicked().sendMessage(core.prefix()+ChatColor.RED+Lang.translate("noPerm", core));
                            }
                        })
                );
            }
        return gui;
    }
    GuiItem arrowLeft(int page) {
        return ItemBuilder.from(Material.ARROW)
                .name(Component.text(ChatColor.translateAlternateColorCodes('&', "&r&e&l"+Lang.translate("prevPage", core))))
                .asGuiItem(inventoryClickEvent -> {
                    Player p = (Player) inventoryClickEvent.getWhoClicked();
                    gui(p, page-1, null, 0, itSort).open(p);
                });
    }
    GuiItem arrowRight(int page) {
        return ItemBuilder.from(Material.ARROW)
                .name(Component.text(ChatColor.translateAlternateColorCodes('&', "&r&e&l"+Lang.translate("nextPage", core))))
                .asGuiItem(inventoryClickEvent -> {
                    Player p = (Player) inventoryClickEvent.getWhoClicked();
                    gui(p, page+1, null, 0, itSort).open(p);
                });
    }
    private void fillGui(Gui gui, Player p, int page, SortingTypes sort, ItemSort itemSort) {
        int min = 40 * (page - 1);
        int max = 40 * page;

        int guiSlot = 0;

        for (int i = min; i < max; i++) {
            if (sort.list.size() <= i) break;

            BINAuction bA = sort.list.get(i);
            if (itemSort != null && !itemSort.getContents().contains(bA.getItem().getType())) {
                continue;
            }

            while (guiSlot < 54 && guiSlot % 9 == 0) {
                guiSlot++;
            }

            if (guiSlot >= 54) break;

            gui.setItem(guiSlot, buyableItem(bA, p.hasPermission("nah.menu.manage"), bA.getSeller().equals(p.getUniqueId())));
            guiSlot++;
        }
    }
    private void fillGui(Gui gui, Player p, int page, SortingTypes sort) {
        int min = 45*(page-1);
        int max = 45*page;

        for (int i = min; i < max; i++) {
            if (sort.list.size() > i) {
                BINAuction bA = sort.list.get(i);
                gui.setItem(i - min, buyableItem(bA, p.hasPermission("nah.menu.manage"), bA.getSeller().equals(p.getUniqueId())));
            }
        }
    }

    private void fillGui(Gui gui, Player p, int page, String research) {
        int min = 40*(page-1);
        int max = 40*page;

        List<BINAuction> list = new ArrayList<>(Util.searchList(research, core.playerSortingTypes.getOrDefault(p.getUniqueId(), SortingTypes.PRICEMIN)));
        for (int i = min; i < max; i++) {
            int slot = i-min;
            if (i % 9 == 0) slot = slot+1;
            if (list.size() > i) {
                BINAuction bA = list.get(i);
                gui.setItem(slot, buyableItem(bA, p.hasPermission("nah.menu.manage"), bA.getSeller().equals(p.getUniqueId())));
            }
        }
    }
    GuiItem buyableItem(BINAuction bA, Boolean staff, Boolean self) {
        ItemStack copy = bA.getItem().clone();
        boolean shulker = copy.getType().name().contains("SHULKER_BOX");
        List<Component> lore = new ArrayList<>();
        if (!shulker) {
            lore.addAll(Util.loreToComps(bA.getItem()));
        }
        lore.addAll(Arrays.asList(Component.text(""), Component.text("  "+Lang.translate("price", core, Long.toString(bA.getPrice()))), Component.text("  "+Lang.translate("seller", core, Util.getName(bA.getSeller()))), Component.text("  "+Lang.translate("expiry", core, Util.convertSecondsToTime(bA.getExpiry()))), Component.text(""), Component.text(Lang.translate("buyNow", core))));
        if (shulker) {
            lore.add(Component.text(Lang.translate("shulkerMenu", core)));
        }
        if (staff) {
            lore.add(Component.text(Lang.translate("staffMenu", core)));
        }
        if (p.hasPermission("nah.manage.unlist.own") && self) {
            lore.add(Component.text(Lang.translate("own-lore", core)));
        }
        return ItemBuilder.from(copy)
                .lore(lore)
                .asGuiItem(event -> {
                    Player p = (Player) event.getWhoClicked();
                    BINAuction bAa = NAHUtil.getAuction(bA.getId());
                    boolean right = event.isRightClick();
                    boolean shift = event.isShiftClick();
                    boolean rss = right&&shift&&shulker;

                    if (event.isShiftClick() && p.hasPermission("nah.menu.manage")) {
                        Gui gui = new ManageMenu().gui(bAa);
                        gui.open(p);
                        return;
                    }
                    if (!bAa.getBuyable()) {
                        p.sendMessage(core.prefix()+Lang.translate("already-bought", core));
                        NAHUtil.open(p, false, searchS, page);
                        return;
                    }

                    if (rss) {
                        new ShulkerViewerGui().gui(bAa).open(p);
                        return;
                    }

                    if (event.isRightClick() && self && p.hasPermission("nah.manage.unlist.own")) {
                        Gui gui = new ConfirmUnlistGui().gui(p, bAa);
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
        for (SortingTypes sT : core.sortingTypes) {
            String arrow = ChatColor.RESET+"  "+ChatColor.YELLOW+"-> "+ChatColor.BOLD;
            if (core.playerSortingTypes.getOrDefault(p.getUniqueId(), SortingTypes.PRICEMIN) == sT) {
                lore.add(Component.text(arrow+ChatColor.GOLD+ChatColor.BOLD+sT.string));
            } else {
                lore.add(Component.text(ChatColor.RESET+"  "+ChatColor.YELLOW+sT.string));
            }
        }
        lore.add(Component.text(""));
        lore.add(Component.text(Lang.translate("clickScroll", core)));

        return ItemBuilder.from(Material.HOPPER)
                .name(Component.text(ChatColor.translateAlternateColorCodes('&', "&r&e&l"+Lang.translate("sort", core))))
                .lore(lore)
                .asGuiItem(event -> {
                    Player pl = (Player) event.getWhoClicked();
                    switch (core.playerSortingTypes.getOrDefault(pl.getUniqueId(), SortingTypes.PRICEMIN)) {
                        case PRICEMAX:
                            if (!core.playerSortingTypes.containsKey(pl.getUniqueId())) {
                                core.playerSortingTypes.put(pl.getUniqueId(), SortingTypes.LATESTPOSTED);
                            } else {
                                core.playerSortingTypes.replace(pl.getUniqueId(), SortingTypes.LATESTPOSTED);
                            }
                            break;
                        case LATESTPOSTED:
                            if (!core.playerSortingTypes.containsKey(pl.getUniqueId())) {
                                core.playerSortingTypes.put(pl.getUniqueId(), SortingTypes.EXPIRESSOON);
                            } else {
                                core.playerSortingTypes.replace(pl.getUniqueId(), SortingTypes.EXPIRESSOON);
                            }
                            break;
                        case EXPIRESSOON:
                            if (!core.playerSortingTypes.containsKey(pl.getUniqueId())) {
                                core.playerSortingTypes.put(pl.getUniqueId(), SortingTypes.PRICEMIN);
                            } else {
                                core.playerSortingTypes.replace(pl.getUniqueId(), SortingTypes.PRICEMIN);
                            }
                            break;
                        case PRICEMIN:
                            if (!core.playerSortingTypes.containsKey(pl.getUniqueId())) {
                                core.playerSortingTypes.put(pl.getUniqueId(), SortingTypes.PRICEMAX);
                            } else {
                                core.playerSortingTypes.replace(pl.getUniqueId(), SortingTypes.PRICEMAX);
                            }
                            break;
                    }
                    gui(p, page, search, 0, itSort).open(p);
                });
    }
    GuiItem search() {
        List<Component> lore = new ArrayList<>();
        if (searchS != null) {
            lore.add(Component.text(""));
            lore.add(Component.text("  "+Lang.translate("currSearch", core, searchS)));
            lore.add(Component.text("  "+Lang.translate("searchReset", core)));
        }
        lore.add(Component.text(""));
        lore.add(Component.text(Lang.translate("clickSearch", core)));
        return ItemBuilder.from(Material.SIGN)
                .name(Component.text(ChatColor.translateAlternateColorCodes('&', "&r&e"+Lang.translate("search", core))))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .lore(lore)
                .asGuiItem(event -> {
                    Player p = (Player) event.getWhoClicked();
                    if (event.isRightClick() && searchS != null) {
                        NAHUtil.open(p, false, null, 1);
                        return;
                    }


                    AnvilGUI.Builder anvil = new AnvilGUI.Builder();
                    anvil.plugin(core);
                    anvil.itemLeft(new ItemStack(Material.PAPER));

                    ItemStack out = new ItemStack(Material.PAPER);
                    ItemMeta meta = out.getItemMeta();
                    meta.setDisplayName(ChatColor.GOLD+Lang.translate("search", core));
                    out.setItemMeta(meta);
                    anvil.itemOutput(out);

                    anvil.text(Lang.translate("enterSearch", core));
                    anvil.onClick((integer, stateSnapshot) -> {
                        if (integer != 2) {
                            return List.of();
                        }
                        p.closeInventory();
                        NAHUtil.open(p, false, stateSnapshot.getText(), 1);
                        return List.of();
                    });
                    anvil.open(p);
                });
    }
}

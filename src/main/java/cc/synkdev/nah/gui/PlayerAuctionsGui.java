package cc.synkdev.nah.gui;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.api.NAHUtil;
import cc.synkdev.nah.manager.Util;
import cc.synkdev.nah.objects.BINAuction;
import cc.synkdev.nexusCore.bukkit.Lang;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerAuctionsGui {
    NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    int page;
    int max;
    MainGuiSnapshot snapshot;
    OfflinePlayer target;
    List<BINAuction> contents;
    public Gui gui(Player p, OfflinePlayer target, int page, MainGuiSnapshot snapshot) {
        this.snapshot = snapshot;
        this.contents = Util.getPlayerListings(target);
        core.checkExpiry.run();
        max = (contents.size()+39)/40;
        this.target = target;
        this.page = page;
        Gui gui = Gui.gui()
                .disableAllInteractions()
                .title(Component.text(ChatColor.YELLOW+Lang.translate("playerTitle", core, target.getName())))
                .rows(6)
                .create();

        gui.getFiller().fillBottom(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).asGuiItem());
        if (page > 1) {
            gui.setItem(6, 4, arrowLeft(page));
        }
        if (page < max) {
            gui.setItem(6, 6, arrowRight(page));
        }

        fillGui(gui, p, page);

        if (snapshot != null) {
            gui.setItem(6, 5, ItemBuilder.from(Material.BARRIER)
                    .name(snapshot == null ? Component.text(ChatColor.RED + Lang.translate("close", core)) : Component.text(ChatColor.RED + Lang.translate("back", core)))
                    .asGuiItem(event -> {
                        new MainGui().gui(p, snapshot.getPage(), snapshot.getSearch(), snapshot.getFirstSort(), snapshot.getItSort()).open(p);
                    }));
        }
        return gui;
    }
    GuiItem arrowLeft(int page) {
        return ItemBuilder.from(Material.ARROW)
                .name(Component.text(ChatColor.translateAlternateColorCodes('&', "&r&e&l"+Lang.translate("prevPage", core))))
                .asGuiItem(inventoryClickEvent -> {
                    Player p = (Player) inventoryClickEvent.getWhoClicked();
                    gui(p, target, page-1, snapshot).open(p);
                });
    }
    GuiItem arrowRight(int page) {
        return ItemBuilder.from(Material.ARROW)
                .name(Component.text(ChatColor.translateAlternateColorCodes('&', "&r&e&l"+Lang.translate("nextPage", core))))
                .asGuiItem(inventoryClickEvent -> {
                    Player p = (Player) inventoryClickEvent.getWhoClicked();
                    gui(p, target, page+1, snapshot).open(p);
                });
    }
    private void fillGui(Gui gui, Player p, int page) {
        int min = 45*(page-1);
        int max = 45*page;

        for (int i = min; i < max; i++) {
            if (contents.size() > i) {
                BINAuction bA = contents.get(i);
                gui.setItem(i - min, buyableItem(bA, p.hasPermission("nah.menu.manage"), bA.getSeller().equals(p.getUniqueId())));
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
        if (self) {
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
                        gui(p, target, page, snapshot).open(p);
                        return;
                    }

                    if (rss) {
                        new ShulkerViewerGui().gui(bAa).open(p);
                        return;
                    }

                    if (event.isRightClick() && self) {
                        Gui gui = new ConfirmUnlistGui().gui(p, bAa);
                        gui.open(p);
                        return;
                    }

                    Gui gui = new ConfirmBuyGui().gui(bAa);
                    gui.open(p);
                });
    }
}

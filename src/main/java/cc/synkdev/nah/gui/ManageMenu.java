package cc.synkdev.nah.gui;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.gui.expiry.PlusMinusExpiryGui;
import cc.synkdev.nah.manager.DataFileManager;
import cc.synkdev.nah.manager.Util;
import cc.synkdev.nah.objects.BINAuction;
import cc.synkdev.synkLibs.bukkit.Lang;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.Arrays;

public class ManageMenu {
    private final NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    public Gui gui(BINAuction bA) {
        Gui gui = Gui.gui()
                .rows(6)
                .title(Component.text(ChatColor.YELLOW+ Lang.translate("manageAuction", core)))
                .disableAllInteractions()
                .create();

        gui.getFiller().fill(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).asGuiItem());
        gui.setItem(1, 5, item(bA));
        gui.setItem(3, 4, price(bA));
        gui.setItem(3, 5, expiry(bA));
        gui.setItem(3, 6, delete(bA));
        gui.setItem(4, 5, give(bA));

        return gui;
    }
    GuiItem item(BINAuction bA) {
        return ItemBuilder.from(bA.getItem()).asGuiItem();
    }
    GuiItem price(BINAuction bA) {
        return ItemBuilder.from(Material.GOLD_INGOT)
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .name(Component.text(Lang.translate("changePrice", core, bA.getPrice()+"")))
                .lore(Arrays.asList(Component.text(""), Component.text("  "+Lang.translate("currPrice", core, bA.getPrice()+"")), Component.text(Lang.translate("clickPrice", core))))
                .asGuiItem(event -> {
                    Player p = (Player) event.getWhoClicked();
                    if (!p.hasPermission("nah.manage.changeprice")) {
                        p.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("noPerm", core));
                        return;
                    }

                    p.closeInventory();

                    TextComponent comp = new TextComponent("["+Lang.translate("clickPrice", core)+"]");
                    comp.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
                    comp.setBold(true);
                    comp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ah setprice "+bA.getId()+" <price>"));

                    p.spigot().sendMessage(comp);
                });
    }
    GuiItem expiry(BINAuction bA) {
        return ItemBuilder.from(Material.CLOCK)
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .name(Component.text(ChatColor.YELLOW+Lang.translate("changeExpiry", core)))
                .lore(Component.empty(), Component.text("  "+Lang.translate("currExpiry", core, Util.formatTimestamp(bA.getExpiry()))), Component.empty(), Component.text(Lang.translate("clickExpiry", core)))
                .asGuiItem(event -> {
                    Player p = (Player) event.getWhoClicked();
                    if (!p.hasPermission("nah.manage.changeexpiry")) {
                        p.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("noPerm", core));
                        return;
                    }

                    new PlusMinusExpiryGui().gui(bA).open(p);
                });
    }
    GuiItem delete(BINAuction bA) {
        return ItemBuilder.from(Material.BARRIER)
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .name(Component.text(Lang.translate("delete", core)))
                .lore(Component.empty(), Component.text(Lang.translate("loreDelete", core)))
                .asGuiItem(event -> {
                    Player p = (Player) event.getWhoClicked();
                    if (!p.hasPermission("nah.manage.delete")) {
                        p.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("noPerm", core));
                        return;
                    }

                    core.runningBINs.remove(bA);
                    Player seller = Bukkit.getPlayer(bA.getSeller());
                    if (seller.isOnline()) seller.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("auctionDeleted", core, p.getDisplayName()));
                    p.getInventory().addItem(bA.getItem());
                    DataFileManager.sort();
                    p.closeInventory();
                });
    }
    GuiItem give(BINAuction bA) {
        return ItemBuilder.from(Material.CHEST).name(Component.text(Lang.translate("getItem", core))).lore(Component.empty(), Component.text(Lang.translate("loreGive", core))).asGuiItem(event -> {
            Player p = (Player) event.getWhoClicked();
            if (!p.hasPermission("nah.manage.give")) {
                p.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("noPerm", core));
                return;
            }
            p.getInventory().addItem(bA.getItem());
        });
    }
}

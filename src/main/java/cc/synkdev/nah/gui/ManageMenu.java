package cc.synkdev.nah.gui;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.components.BINAuction;
import cc.synkdev.nah.manager.DataFileManager;
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

public class ManageMenu {
    NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    Gui gui(BINAuction bA) {
        Gui gui = Gui.gui()
                .rows(4)
                .title(Component.text(ChatColor.YELLOW+ Lang.translate("manageAuction", core)))
                .disableAllInteractions()
                .create();

        gui.setItem(1, 5, item(bA));
        gui.setItem(3, 4, price(bA));
        gui.setItem(3, 5, expiry(bA));
        gui.setItem(3, 6, delete(bA));
        return gui;
    }
    GuiItem item(BINAuction bA) {
        return ItemBuilder.from(bA.getItem()).asGuiItem();
    }
    GuiItem price(BINAuction bA) {
        return ItemBuilder.from(Material.GOLD_INGOT)
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .name(Component.text(ChatColor.YELLOW+Lang.translate("changePrice", core, bA.getPrice()+"")))
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
                    comp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ah setprice "+bA.getUuid().toString()+" <price>"));

                    p.spigot().sendMessage(comp);
                });
    }
    GuiItem expiry(BINAuction bA) {
        return ItemBuilder.from(Material.CLOCK)
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .name(Component.text(ChatColor.YELLOW+Lang.translate("changeExpiry", core, bA.getExpiry()+"")))
                .lore(Component.text(ChatColor.DARK_GRAY+Lang.translate("loreExpiry", core)))
                .asGuiItem(event -> {
                    Player p = (Player) event.getWhoClicked();
                    if (!p.hasPermission("nah.manage.changeexpiry")) {
                        p.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("noPerm", core));
                        return;
                    }

                    p.closeInventory();

                    TextComponent comp = new TextComponent("["+Lang.translate("clickExpiry", core)+"]");
                    comp.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
                    comp.setBold(true);
                    comp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ah setexpiry "+bA.getUuid().toString()+" <timestamp>"));

                    p.spigot().sendMessage(comp);
                });
    }
    GuiItem delete(BINAuction bA) {
        return ItemBuilder.from(Material.BARRIER)
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .name(Component.text(ChatColor.RED+Lang.translate("delete", core)))
                .lore(Component.text(ChatColor.DARK_GRAY+Lang.translate("loreDelete", core)))
                .asGuiItem(event -> {
                    Player p = (Player) event.getWhoClicked();
                    if (!p.hasPermission("nah.manage.delete")) {
                        p.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("noPerm", core));
                        return;
                    }

                    core.runningBINs.remove(bA);
                    if (bA.getSeller().isOnline()) bA.getSeller().getPlayer().sendMessage(core.prefix()+ChatColor.RED+Lang.translate("auctionDeleted", core, p.getDisplayName()));
                    p.getInventory().addItem(bA.getItem());
                    DataFileManager.sort();
                    p.closeInventory();
                });
    }
}

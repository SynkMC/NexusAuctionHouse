package cc.synkdev.nah.gui;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.components.BINAuction;
import cc.synkdev.nah.manager.DataFileManager;
import cc.synkdev.synkLibs.bukkit.Lang;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class ConfirmSellGui {
    NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    public Gui gui(Player p, int price) {
        Gui gui = Gui.gui()
                .title(Component.text(ChatColor.YELLOW+ Lang.translate("confirmSell", core)))
                .rows(4)
                .disableAllInteractions()
                .create();
        gui.setItem(2, 5, item(p));
        gui.setItem(3, 3, confirm(price));
        gui.setItem(3, 7, cancel());

        return gui;
    }
    GuiItem item(Player p) {
        return ItemBuilder.from(p.getInventory().getItemInMainHand()).asGuiItem();
    }
    GuiItem confirm(int price) {
        double tax = price*((double) core.getTaxPercent() /100);
        ItemStack item = new ItemStack(Material.GREEN_WOOL);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setLore(new ArrayList<>(Arrays.asList("", ChatColor.translateAlternateColorCodes('&', "&r&e&l"+Lang.translate("taxes", core, core.getTaxPercent()+"", tax+"")))));
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&r&c&l"+Lang.translate("confirm", core)));
        item.setItemMeta(meta);
        return ItemBuilder.from(item).asGuiItem(event -> {
            Player pl = (Player) event.getWhoClicked();
            ItemStack itemStack = pl.getInventory().getItemInMainHand();
            if (core.getEcon().has(pl, tax)) {
                core.getEcon().withdrawPlayer(pl, tax);
                int expire = Math.toIntExact(System.currentTimeMillis() / 1000) + core.getExpireTime();
                BINAuction bA = new BINAuction(UUID.randomUUID(), pl, itemStack, price, expire);
                pl.getInventory().setItemInMainHand(null);
                core.runningBINs.put(bA, expire);
                DataFileManager.sort();
                pl.sendMessage(core.prefix() + ChatColor.GREEN + Lang.translate("successSell", core, price + ""));
                pl.closeInventory();
            } else pl.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("notEnoughTaxes", core));
            pl.closeInventory();
        });
    }
    GuiItem cancel() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&r&c&l"+Lang.translate("cancel", core)));
        item.setItemMeta(meta);
        return ItemBuilder.from(item).asGuiItem(event -> {
            Player pl = (Player) event.getWhoClicked();
            pl.closeInventory();
        });
    }
}

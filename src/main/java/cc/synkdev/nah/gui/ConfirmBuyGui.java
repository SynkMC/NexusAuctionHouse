package cc.synkdev.nah.gui;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.manager.WebhookManager;
import cc.synkdev.nah.objects.BINAuction;
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

public class ConfirmBuyGui {
    NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    public Gui gui(BINAuction bA) {
        Gui gui = Gui.gui()
                .title(Component.text(ChatColor.YELLOW+ Lang.translate("confirmBuy", core)))
                .rows(4)
                .disableAllInteractions()
                .create();
        gui.setItem(2, 5, item(bA));
        gui.setItem(3, 3, confirm(bA));
        gui.setItem(3, 7, cancel());

        return gui;
    }
    GuiItem item(BINAuction bA) {
        return ItemBuilder.from(bA.getItem()).asGuiItem();
    }
    GuiItem confirm(BINAuction bA) {
        ItemStack item = new ItemStack(Material.GREEN_WOOL);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&r&c&l"+Lang.translate("confirm", core)));
        item.setItemMeta(meta);
        return ItemBuilder.from(item).asGuiItem(event -> {
            Player pl = (Player) event.getWhoClicked();
            if (!bA.getSeller().getUniqueId().toString().equalsIgnoreCase(pl.getUniqueId().toString())) {
                if (core.getEcon().has(pl, bA.getPrice())) {
                    core.getEcon().withdrawPlayer(pl, bA.getPrice());
                    core.getEcon().depositPlayer(bA.getSeller(), bA.getPrice());
                    if (bA.getSeller().isOnline()) bA.getSeller().getPlayer().sendMessage(core.prefix()+ChatColor.GOLD+ pl.getName()+" "+Lang.translate("smnBought", core, bA.getPrice()+""));
                    pl.getInventory().addItem(bA.getItem());
                    core.runningBINs.remove(bA);
                    bA.setBuyable(false);
                    bA.setBuyer(pl);
                    core.expiredBINs.add(bA);
                    DataFileManager.sort();
                    pl.closeInventory();
                    WebhookManager.sendWebhook("listing-bought", null, pl.getName(), bA.getSeller().getName(), bA.getPrice()+"");
                    pl.sendMessage(core.prefix() + ChatColor.GREEN + Lang.translate("successBuy", core, bA.getSeller().getName(), bA.getPrice()+""));
                } else pl.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("notEnoughBuy", core));
            } else pl.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("buyFromYou", core));
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
            new MainGui().gui(pl, 1, null).open(pl);
        });
    }
}

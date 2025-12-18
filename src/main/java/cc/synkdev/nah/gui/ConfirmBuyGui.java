package cc.synkdev.nah.gui;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.api.NAHUtil;
import cc.synkdev.nah.manager.DataFileManager;
import cc.synkdev.nah.manager.Util;
import cc.synkdev.nah.manager.WebhookManager;
import cc.synkdev.nah.objects.BINAuction;
import cc.synkdev.nexusCore.bukkit.Lang;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfirmBuyGui {
    NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    public Gui gui(BINAuction bA) {
        Gui gui = Gui.gui()
                .title(Component.text(ChatColor.YELLOW+ Lang.translate("confirmBuy", core)))
                .rows(4)
                .disableAllInteractions()
                .create();

        gui.getFiller().fill(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).asGuiItem());
        gui.setItem(2, 5, item(bA));
        gui.setItem(3, 3, confirm(bA));
        gui.setItem(3, 7, cancel());

        return gui;
    }
    GuiItem item(BINAuction bA) {
        return ItemBuilder.from(bA.getItem()).asGuiItem();
    }
    GuiItem confirm(BINAuction bAa) {
        int tax = Math.toIntExact(Math.round(bAa.getPrice()*((double) core.getBuyTaxPercent()/100)));
        ItemStack item = new ItemStack(Material.GREEN_WOOL);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&r&c&l"+Lang.translate("confirm", core)));
        if (core.getBuyTaxPercent() > 0) meta.setLore(new ArrayList<>(Arrays.asList("", ChatColor.translateAlternateColorCodes('&', "&r&e&l"+Lang.translate("taxes", core, core.getBuyTaxPercent()+"", tax+"")))));
        item.setItemMeta(meta);
        return ItemBuilder.from(item).asGuiItem(event -> {
            Player pl = (Player) event.getWhoClicked();
            BINAuction bA = NAHUtil.getAuction(bAa.getId());
            if (!bA.getBuyable()) {
                pl.sendMessage(core.prefix()+Lang.translate("already-bought", core));
                pl.closeInventory();
                return;
            }
            if (!bA.getSeller().toString().equalsIgnoreCase(pl.getUniqueId().toString())) {
                if (core.getEcon().has(pl, bA.getPrice()+tax)) {
                    core.getEcon().withdrawPlayer(pl, bA.getPrice()+tax);
                    core.getEcon().depositPlayer(Bukkit.getOfflinePlayer(bA.getSeller()), bA.getPrice());
                    if (Util.isOnline(bA.getSeller())) Bukkit.getPlayer(bA.getSeller()).sendMessage(core.prefix()+ChatColor.GOLD+ pl.getName()+" "+Lang.translate("smnBought", core, bA.getPrice()+""));

                    if (pl.getInventory().firstEmpty() == -1) {
                        List<ItemStack> list = core.retrieveMap.getOrDefault(pl.getUniqueId(), new ArrayList<>());
                        list.add(bA.getItem());
                        core.retrieveMap.put(pl.getUniqueId(), list);
                        pl.sendMessage(core.prefix()+Lang.translate("buyInvFull", core));
                    } else {
                        pl.getInventory().addItem(bA.getItem());
                    }

                    core.runningBINs.remove(bA);
                    bA.setBuyer(pl.getUniqueId());
                    core.expiredBINs.add(bA);
                    DataFileManager.sort();
                    pl.closeInventory();
                    WebhookManager.sendWebhook("listing-bought", bA, pl.getName(), Util.getName(bA.getSeller()), bA.getPrice()+"");
                    core.money = core.money+Math.toIntExact(bA.getPrice());
                    pl.sendMessage(core.prefix() + ChatColor.GREEN + Lang.translate("successBuy", core, Util.getName(bA.getSeller()), bA.getPrice()+""));
                } else {
                    pl.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("notEnoughBuy", core));
                }
            } else {
                pl.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("buyFromYou", core));
            }
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
            NAHUtil.open(pl, false, null, 1);
        });
    }
}

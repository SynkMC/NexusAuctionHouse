package cc.synkdev.nah.gui;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.api.NAHUtil;
import cc.synkdev.nah.api.events.ItemUnlistEvent;
import cc.synkdev.nah.manager.DataFileManager;
import cc.synkdev.nah.objects.BINAuction;
import cc.synkdev.synkLibs.bukkit.Lang;
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

public class ConfirmUnlistGui {
    private final NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    private Player p;
    private BINAuction bA;
    private ItemStack item;
    public Gui gui(Player p, BINAuction bA) {
        this.p = p;
        this.bA = bA;
        item = bA.getItem();
        Gui gui = Gui.gui()
                .title(Component.text(ChatColor.YELLOW+ Lang.translate("confirmUnlist", core)))
                .rows(4)
                .disableAllInteractions()
                .create();
        gui.getFiller().fill(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).asGuiItem());
        gui.setItem(2, 5, item());
        gui.setItem(3, 3, confirm());
        gui.setItem(3, 7, cancel());
        return gui;
    }

    GuiItem item() {
        return ItemBuilder.from(bA.getItem().clone()).lore(Component.text(""), Component.text(Lang.translate("lore-unlist-item", core))).asGuiItem();
    }
    GuiItem confirm() {
        ItemStack item = new ItemStack(Material.GREEN_WOOL);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&r&c&l"+Lang.translate("confirm", core)));
        item.setItemMeta(meta);
        return ItemBuilder.from(item).asGuiItem(event -> {
            Player pl = (Player) event.getWhoClicked();
            BINAuction bAa = NAHUtil.getAuction(bA.getId());
            if (bAa.getBuyable()) {
                bAa.setItem(this.item);
                ItemUnlistEvent unlistEvent = new ItemUnlistEvent(pl, bAa);
                Bukkit.getPluginManager().callEvent(unlistEvent);

                if (unlistEvent.isCancelled()) return;

                core.runningBINs.remove(bAa);
                core.expiredBINs.add(bAa);
                DataFileManager.sort();
                pl.getInventory().addItem(this.item);
            }
            NAHUtil.open(pl, false, null, 1);
        });
    }
    GuiItem cancel() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&r&c&l"+Lang.translate("cancel", core)));
        item.setItemMeta(meta);
        return ItemBuilder.from(item).asGuiItem(event -> NAHUtil.open(p, false, null, 1));
    }
}

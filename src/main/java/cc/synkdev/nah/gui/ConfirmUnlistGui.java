package cc.synkdev.nah.gui;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.api.NAHUtil;
import cc.synkdev.nah.api.events.ItemUnlistEvent;
import cc.synkdev.nah.manager.DataFileManager;
import cc.synkdev.nah.manager.Util;
import cc.synkdev.nah.objects.BINAuction;
import cc.synkdev.nexusCore.bukkit.Lang;
import cc.synkdev.triumph.builder.item.ItemBuilder;
import cc.synkdev.triumph.guis.Gui;
import cc.synkdev.triumph.guis.GuiItem;
import cc.synkdev.kyori.adventure.text.Component;
import cc.synkdev.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

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
                .title(LegacyComponentSerializer.legacyAmpersand().deserialize(ChatColor.YELLOW+ Lang.translate("confirmUnlist", core)))
                .rows(4)
                .disableAllInteractions()
                .create();
        gui.getFiller().fill(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(LegacyComponentSerializer.legacyAmpersand().deserialize(" ")).asGuiItem());
        gui.setItem(2, 5, item());
        gui.setItem(3, 3, confirm());
        gui.setItem(3, 7, cancel());
        return gui;
    }

    GuiItem item() {
        return ItemBuilder.from(bA.getItem().clone()).lore(LegacyComponentSerializer.legacyAmpersand().deserialize(""), LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.translate("lore-unlist-item", core))).asGuiItem();
    }
    GuiItem confirm() {
        ItemStack item = new ItemStack(Material.GREEN_WOOL);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(Util.color("&r&c&l"+Lang.translate("confirm", core)));
        item.setItemMeta(meta);
        return ItemBuilder.from(item).asGuiItem(event -> {
            Player pl = (Player) event.getWhoClicked();
            BINAuction bAa = NAHUtil.getAuction(bA.getId());
            if (bAa.getBuyable()) {
                bAa.setItem(this.item);
                bAa.setBuyer(bAa.getSeller());
                ItemUnlistEvent unlistEvent = new ItemUnlistEvent(pl, bAa);
                Bukkit.getPluginManager().callEvent(unlistEvent);

                if (unlistEvent.isCancelled()) return;

                core.runningBINs.remove(bAa);
                core.expiredBINs.add(bAa);
                DataFileManager.sort();
                if (pl.getInventory().firstEmpty() != -1) pl.getInventory().addItem(this.item);
                else {
                    List<ItemStack> retrieveList = new ArrayList<>(core.retrieveMap.getOrDefault(pl.getUniqueId(), new ArrayList<>()));
                    retrieveList.add(this.item);
                    core.retrieveMap.put(pl.getUniqueId(), retrieveList);
                    pl.sendMessage(Lang.translate("retrieveFull", core));
                }
            }
            NAHUtil.open(pl, false, null, 1);
        });
    }
    GuiItem cancel() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(Util.color("&r&c&l"+Lang.translate("cancel", core)));
        item.setItemMeta(meta);
        return ItemBuilder.from(item).asGuiItem(event -> NAHUtil.open(p, false, null, 1));
    }
}

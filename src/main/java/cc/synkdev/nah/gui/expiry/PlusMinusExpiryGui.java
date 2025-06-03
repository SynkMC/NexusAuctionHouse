package cc.synkdev.nah.gui.expiry;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.api.NAHUtil;
import cc.synkdev.nah.manager.Util;
import cc.synkdev.nah.objects.BINAuction;
import cc.synkdev.synkLibs.bukkit.Lang;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PlusMinusExpiryGui {
    NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    public Gui gui(BINAuction bA) {
        Gui gui = Gui.gui()
                .disableAllInteractions()
                .title(Component.text(Lang.translate("expiryEditGui", core)))
                .rows(3)
                .create();
        gui.setItem(2, 5, ItemBuilder.from(Material.PAPER)
                .name(Component.text(ChatColor.YELLOW+Lang.translate("editExpiryGuiInfo", core, Util.formatTimestamp(bA.getExpiry()))))
                .asGuiItem());

        GuiItem calendar = ItemBuilder.from(Material.WHITE_STAINED_GLASS_PANE)
                .name(Component.text(ChatColor.YELLOW+Lang.translate("editExpiryCalendar", core)))
                .asGuiItem(event -> new ExpiryCalendarGuis((Player) event.getWhoClicked(), bA));
        gui.getFiller().fillBetweenPoints(1, 4, 1, 6, calendar);
        gui.setItem(2, 4, calendar);
        gui.setItem(2, 6, calendar);
        gui.getFiller().fillBetweenPoints(3, 4, 3, 6, calendar);

        GuiItem plusMins = ItemBuilder.from(Material.YELLOW_STAINED_GLASS_PANE)
                .name(Component.text(ChatColor.YELLOW+"+10min"))
                .asGuiItem(event -> {
                    NAHUtil.setExpiry(bA, bA.getExpiry() + 600, event.getWhoClicked().getName());
                    ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), "entity.experience_orb.pickup", 1, 1);
                    this.gui(NAHUtil.getAuction(bA.getId())).open((Player) event.getWhoClicked());
                });
        gui.setItem(1, 7, plusMins);
        gui.setItem(2, 7, plusMins);
        gui.setItem(3, 7, plusMins);

        GuiItem plusHr = ItemBuilder.from(Material.LIME_STAINED_GLASS_PANE)
                .name(Component.text(ChatColor.YELLOW+"+1h"))
                .asGuiItem(event -> {
                    NAHUtil.setExpiry(bA, bA.getExpiry() + 3600, event.getWhoClicked().getName());
                    ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), "entity.experience_orb.pickup", 1, 1);
                    this.gui(NAHUtil.getAuction(bA.getId())).open((Player) event.getWhoClicked());
                });
        gui.setItem(1, 8, plusHr);
        gui.setItem(2, 8, plusHr);
        gui.setItem(3, 8, plusHr);

        GuiItem plusDay = ItemBuilder.from(Material.GREEN_STAINED_GLASS_PANE)
                .name(Component.text(ChatColor.YELLOW+"+24h"))
                .asGuiItem(event -> {
                    NAHUtil.setExpiry(bA, bA.getExpiry() + (3600*24), event.getWhoClicked().getName());
                    ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), "entity.experience_orb.pickup", 1, 1);
                    this.gui(NAHUtil.getAuction(bA.getId())).open((Player) event.getWhoClicked());
                });
        gui.setItem(1, 9, plusDay);
        gui.setItem(2, 9, plusDay);
        gui.setItem(3, 9, plusDay);

        GuiItem minMins = ItemBuilder.from(Material.ORANGE_STAINED_GLASS_PANE)
                .name(Component.text(ChatColor.YELLOW+"-10min"))
                .asGuiItem(event -> {
                    NAHUtil.setExpiry(bA, bA.getExpiry() - 600, event.getWhoClicked().getName());
                    ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), "entity.experience_orb.pickup", 1, 1);
                    this.gui(NAHUtil.getAuction(bA.getId())).open((Player) event.getWhoClicked());
                });
        gui.setItem(1, 3, minMins);
        gui.setItem(2, 3, minMins);
        gui.setItem(3, 3, minMins);

        GuiItem minHr = ItemBuilder.from(Material.RED_STAINED_GLASS_PANE)
                .name(Component.text(ChatColor.YELLOW+"-1h"))
                .asGuiItem(event -> {
                    NAHUtil.setExpiry(bA, bA.getExpiry() - 3600, event.getWhoClicked().getName());
                    ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), "entity.experience_orb.pickup", 1, 1);
                    this.gui(NAHUtil.getAuction(bA.getId())).open((Player) event.getWhoClicked());
                });
        gui.setItem(1, 2, minHr);
        gui.setItem(2, 2, minHr);
        gui.setItem(3, 2, minHr);

        GuiItem minDay = ItemBuilder.from(Material.BROWN_STAINED_GLASS_PANE)
                .name(Component.text(ChatColor.YELLOW+"-24h"))
                .asGuiItem(event -> {
                    NAHUtil.setExpiry(bA, bA.getExpiry() - (3600*24), event.getWhoClicked().getName());
                    ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), "entity.experience_orb.pickup", 1, 1);
                    this.gui(NAHUtil.getAuction(bA.getId())).open((Player) event.getWhoClicked());
                });
        gui.setItem(1, 1, minDay);
        gui.setItem(2, 1, minDay);
        gui.setItem(3, 1, minDay);
        return gui;
    }


}

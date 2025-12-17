package cc.synkdev.nah.gui;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.manager.Util;
import cc.synkdev.nah.objects.BINAuction;
import cc.synkdev.nexusCore.bukkit.Lang;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogsGui {
    NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    int max = (core.expiredBINs.size()/10)+1;
    public Gui gui(int page) {
        Gui gui = Gui.gui()
                .disableAllInteractions()
                .title(Component.text(ChatColor.YELLOW+ Lang.translate("logTitle", core)))
                .rows(6)
                .create();
        gui.getFiller().fillBottom(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).asGuiItem());
        if (page > 1) gui.setItem(6, 4, arrowLeft(page));
        if (page < max) gui.setItem(6, 6, arrowRight(page));
        fillGui(gui, page);

        return gui;
    }
    GuiItem arrowLeft(int page) {
        return ItemBuilder.from(Material.ARROW)
                .name(Component.text(ChatColor.translateAlternateColorCodes('&', "&r&e&l"+Lang.translate("prevPage", core))))
                .asGuiItem(inventoryClickEvent -> {
                    Player p = (Player) inventoryClickEvent.getWhoClicked();
                    gui(page-1).open(p);
                });
    }
    GuiItem arrowRight(int page) {
        return ItemBuilder.from(Material.ARROW)
                .name(Component.text(ChatColor.translateAlternateColorCodes('&', "&r&e&l"+Lang.translate("nextPage", core))))
                .asGuiItem(inventoryClickEvent -> {
                    Player p = (Player) inventoryClickEvent.getWhoClicked();
                    gui(page+1).open(p);
                });
    }
    private void fillGui(Gui gui, int page) {
        int min = 45*(page-1);
        int max = 45*page;


        List<BINAuction> list = new ArrayList<>(core.expiredBINs);

        for (int i = min; i < max; i++) {
            if (list.size() > i) gui.setItem(i-min, expiredItem(list.get(i)));
        }
    }
    GuiItem expiredItem(BINAuction bA) {
        ItemStack copy = bA.getItem().clone();
        List<Component> lore = new ArrayList<>();
        lore.addAll(Util.loreToComps(bA.getItem()));
        if (bA.getBuyer() == null) {
            lore.addAll(Arrays.asList(Component.text(""), Component.text("  "+ChatColor.YELLOW+ Lang.translate("expiredWord", core))));
        } else {
            lore.addAll(Arrays.asList(Component.text(""), Component.text("  "+ChatColor.YELLOW+ Lang.translate("soldTo", core, Util.getName(bA.getBuyer()), Util.getName(bA.getSeller())))));
        }
        lore.addAll(Arrays.asList(Component.text(""), Component.text(ChatColor.GOLD+Lang.translate("giveLog", core))));
        return ItemBuilder.from(copy)
                .lore(lore)
                .asGuiItem(event -> {
                    Player p = (Player) event.getWhoClicked();
                    p.getInventory().addItem(bA.getItem());
                });
    }
}

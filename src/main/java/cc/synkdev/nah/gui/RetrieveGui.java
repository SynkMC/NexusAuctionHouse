package cc.synkdev.nah.gui;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.manager.Lang;
import cc.synkdev.nah.manager.Util;
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

public class RetrieveGui {
    NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    int max = 0;
    public Gui gui(Player p, int page) {
        max = (core.retrieveMap.getOrDefault(p, new ArrayList<>()).size()/10)+1;
        Gui gui = Gui.gui()
                .disableAllInteractions()
                .title(Component.text(ChatColor.YELLOW+ Lang.translate("titleRetrieve")))
                .rows(6)
                .create();
        if (page > 1) gui.setItem(6, 4, arrowLeft(page));
        if (page < max) gui.setItem(6, 6, arrowRight(page));
        fillGui(gui, p, page);
        return gui;
    }
    GuiItem arrowLeft(int page) {
        GuiItem item = ItemBuilder.from(Material.ARROW)
                .name(Component.text(ChatColor.translateAlternateColorCodes('&', "&r&e&l"+Lang.translate("prevPage"))))
                .asGuiItem(inventoryClickEvent -> {
                    Player p = (Player) inventoryClickEvent.getWhoClicked();
                    gui(p, page-1).open(p);
                });
        return item;
    }
    GuiItem arrowRight(int page) {
        GuiItem item = ItemBuilder.from(Material.ARROW)
                .name(Component.text(ChatColor.translateAlternateColorCodes('&', "&r&e&l"+Lang.translate("nextPage"))))
                .asGuiItem(inventoryClickEvent -> {
                    Player p = (Player) inventoryClickEvent.getWhoClicked();
                    gui(p, page+1).open(p);
                });
        return item;
    }
    private void fillGui(Gui gui, Player p, int page) {
        int min = 45*(page-1);
        int max = 45*page;

        List<ItemStack> list = new ArrayList<>(core.retrieveMap.getOrDefault(p, new ArrayList<>()));
        for (int i = min; i < max; i++) {
            if (list.size() > i) gui.setItem(i-min, item(list.get(i), page));
        }
    }
    GuiItem item(ItemStack item, int page) {
        List<Component> lore = new ArrayList<>();
        lore.addAll(Util.loreToComps(item));
        lore.addAll(Arrays.asList(Component.text(""), Component.text(ChatColor.YELLOW+"---------------"), Component.text(""), Component.text(ChatColor.YELLOW+Lang.translate("clickRetrieve"))));
        GuiItem gitem = ItemBuilder.from(item.clone())
                .lore(lore)
                .asGuiItem(event -> {
                    Player p = (Player) event.getWhoClicked();
                    List<ItemStack> list = core.retrieveMap.getOrDefault(p, new ArrayList<>());
                    if (!list.isEmpty() && list.contains(item)) {
                        p.getInventory().addItem(item);
                        list.remove(item);
                        if (!list.isEmpty()) {
                            core.retrieveMap.replace(p, list);
                            gui(p, page).open(p);
                        }
                        else {
                            core.retrieveMap.remove(p);
                            p.closeInventory();
                        }
                    }
                });
        return gitem;
    }
}

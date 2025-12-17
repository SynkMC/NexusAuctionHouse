package cc.synkdev.nah.gui.sort;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.objects.ItemSort;
import cc.synkdev.nexusCore.bukkit.Lang;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class SortsManagementGui {
    private final NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    public Gui gui(int page) {
        Gui gui = Gui.gui()
                .disableAllInteractions()
                .rows(6)
                .title(Component.text(Lang.translate("sortsManagement", core)))
                .create();

        gui.getFiller().fillBottom(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).asGuiItem());

        gui.setItem(6, 4, ItemBuilder.from(Material.GREEN_WOOL).name(Component.text(Lang.translate("createSort", core)))
                .lore(Component.text(""),
                        Component.text(Lang.translate("clickCreateSort", core)))
                .asGuiItem(event -> {
                    Player p = (Player) event.getWhoClicked();
                    if (!p.hasPermission("nah.sorts.create")) {
                        p.sendMessage(core.prefix()+Lang.translate("noPerm", core));
                        return;
                    }

                    AnvilGUI.Builder builder = new AnvilGUI.Builder();
                    builder.plugin(core);

                    ItemStack item = new ItemStack(Material.PAPER);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(Lang.translate("enterName", core));
                    item.setItemMeta(meta);
                    builder.itemLeft(item);
                    builder.text(Lang.translate("enterName", core));
                    builder.onClick((integer, stateSnapshot) -> {
                        if (integer != 2) {
                            return List.of();
                        }

                        stateSnapshot.getPlayer().closeInventory();
                        ItemSort sort = new ItemSort(stateSnapshot.getText());
                        core.itemSorts.put(stateSnapshot.getText(), sort);
                        new EditSortGui().gui(sort).open(stateSnapshot.getPlayer());
                        return List.of();
                    });
                    builder.open(p);
                }));

        int min = 45*(page-1);
        int max = 45*page;

        for (int i = min; i < max; i++) {
            if (core.itemSorts.size() > i) {
                ItemSort sort = core.itemSorts.entrySet().stream().toList().get(i).getValue();
                gui.setItem(i - min, ItemBuilder.from(sort.getIcon()).name(Component.text(ChatColor.YELLOW+sort.getName())).asGuiItem(event -> new EditSortGui().gui(sort).open((Player) event.getWhoClicked())));
            }
        }

        gui.setItem(6, 6, ItemBuilder.from(Material.BARRIER)
                .name(Component.text(Lang.translate("back", core)))
                .asGuiItem(event -> event.getWhoClicked().closeInventory()));

        if (page > 1) {
            gui.setItem(6, 3, ItemBuilder.from(Material.ARROW)
                    .name(Component.text(ChatColor.translateAlternateColorCodes('&', "&r&e&l"+Lang.translate("prevPage", core))))
                    .asGuiItem(inventoryClickEvent -> {
                        Player p = (Player) inventoryClickEvent.getWhoClicked();
                        gui(page-1).open(p);
                    }));
        }
        if (page < max) {
            gui.setItem(6, 7, ItemBuilder.from(Material.ARROW)
                    .name(Component.text(ChatColor.translateAlternateColorCodes('&', "&r&e&l"+Lang.translate("nextPage", core))))
                    .asGuiItem(inventoryClickEvent -> {
                        Player p = (Player) inventoryClickEvent.getWhoClicked();
                        gui(page+1).open(p);
                    }));
        }
        return gui;
    }
}

package cc.synkdev.nah.gui.sort;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.manager.Util;
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

import java.util.List;

public class EditSortGui {
    private final NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    public Gui gui(ItemSort sort) {
        Gui gui = Gui.gui()
                .disableAllInteractions()
                .rows(5)
                .title(Component.text(Lang.translate("editSort", core, sort.getName())))
                .create();

        gui.getFiller().fill(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).asGuiItem());
        gui.setItem(1, 5, ItemBuilder.from(sort.getIcon())
                .name(Component.text(ChatColor.GOLD+sort.getName()))
                .lore(Component.empty(),
                        Component.text(Lang.translate("editSortInfo", core)))
                .asGuiItem());

        gui.setItem(3, 3, ItemBuilder.from(Material.NAME_TAG)
                .name(Component.text(Lang.translate("renameSort", core)))
                .asGuiItem(event -> {
                    Player p = (Player) event.getWhoClicked();
                    if (!p.hasPermission("nah.sorts.rename")) {
                        p.sendMessage(core.prefix()+Lang.translate("noPerm", core));
                        return;
                    }

                    AnvilGUI.Builder builder = new AnvilGUI.Builder();
                    builder.plugin(core);
                    builder.itemLeft(new ItemStack(Material.NAME_TAG));
                    builder.text(sort.getName());
                    builder.onClick((integer, stateSnapshot) -> {
                        if (integer != 2) {
                            return List.of();
                        }

                        String name = sort.getName();
                        sort.setName(Util.color(stateSnapshot.getText()));
                        core.itemSorts.remove(name);
                        core.itemSorts.put(Util.color(stateSnapshot.getText()), sort);

                        gui(sort).open(stateSnapshot.getPlayer());
                        return List.of();
                    });
                    builder.open(p);
                }));

        gui.setItem(3, 4, ItemBuilder.from(sort.getIcon())
                .name(Component.text(Lang.translate("changeSortIcon", core)))
                .asGuiItem(event -> {
                    Player p = (Player) event.getWhoClicked();
                    if (!p.hasPermission("nah.sorts.icon")) {
                        p.sendMessage(core.prefix()+Lang.translate("noPerm", core));
                        return;
                    }
                    new IconPickerGui().gui(sort, 1, null).open((Player) event.getWhoClicked());
                }));

        gui.setItem(3, 6, ItemBuilder.from(Material.CHEST)
                .name(Component.text(Lang.translate("setSortContents", core)))
                .asGuiItem(event -> {
                    Player p = (Player) event.getWhoClicked();
                    if (!p.hasPermission("nah.sorts.contents")) {
                        p.sendMessage(core.prefix()+Lang.translate("noPerm", core));
                        return;
                    }

                    new ContentsGui().gui(sort, 1, null).open(p);
                }));

        gui.setItem(3, 7, ItemBuilder.from(Material.BARRIER)
                .name(Component.text(Lang.translate("delete", core)))
                .asGuiItem(event -> {
                    Player p = (Player) event.getWhoClicked();
                    if (!p.hasPermission("nah.sorts.delete")) {
                        p.sendMessage(core.prefix()+Lang.translate("noPerm", core));
                        return;
                    }

                    core.itemSorts.remove(sort.getName());
                    p.closeInventory();
                    p.sendMessage(core.prefix()+Lang.translate("sortDeleteSuccess", core, sort.getName()));
                }));


        gui.setItem(5, 5, ItemBuilder.from(Material.BARRIER)
                .name(Component.text(Lang.translate("back", core)))
                .asGuiItem(event -> new SortsManagementGui().gui(1).open((Player) event.getWhoClicked())));
        return gui;
    }
}

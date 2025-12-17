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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class IconPickerGui {
    private final NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    public Gui gui(ItemSort sort, int page, String search) {
        Gui gui = Gui.gui()
                .rows(6)
                .disableAllInteractions()
                .title(Component.text(Lang.translate("pickIconTitle", core)))
                .create();
        List<Material> materials = new ArrayList<>(Util.getFilteredMaterials(search));
        int max = (materials.size()+44)/45;
        gui.getFiller().fillBottom(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).asGuiItem());

        if (page > 1) {
            gui.setItem(6, 4, ItemBuilder.from(Material.ARROW)
                    .name(Component.text(Lang.translate("prevPage", core)))
                    .asGuiItem(event -> gui(sort, page-1, search).open((Player) event.getWhoClicked())));
        }

        if (page < max) {
            gui.setItem(6, 6, ItemBuilder.from(Material.ARROW)
                    .name(Component.text(Lang.translate("nextPage", core)))
                    .asGuiItem(event -> gui(sort, page+1, search).open((Player) event.getWhoClicked())));
        }

        gui.setItem(6, 5, ItemBuilder.from(Material.BARRIER)
                .name(Component.text(Lang.translate("back", core)))
                .asGuiItem(event -> new EditSortGui().gui(sort).open((Player) event.getWhoClicked())));

        List<Component> lore = new ArrayList<>();
        if (search != null) {
            lore.add(Component.text(""));
            lore.add(Component.text("  "+Lang.translate("currSearch", core, search)));
            lore.add(Component.text("  "+Lang.translate("searchReset", core)));
        }
        lore.add(Component.text(""));
        lore.add(Component.text(Lang.translate("clickSearch", core)));
        gui.setItem(6, 2, ItemBuilder.from(Material.SIGN)
                .name(Component.text(ChatColor.translateAlternateColorCodes('&', "&r&e"+Lang.translate("search", core))))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .lore(lore)
                .asGuiItem(event -> {
                    Player p = (Player) event.getWhoClicked();
                    if (event.isRightClick() && search != null) {
                        gui(sort, 1, null).open(p);
                        return;
                    }


                    AnvilGUI.Builder anvil = new AnvilGUI.Builder();
                    anvil.plugin(core);
                    anvil.itemLeft(new ItemStack(Material.PAPER));

                    ItemStack out = new ItemStack(Material.PAPER);
                    ItemMeta meta = out.getItemMeta();
                    meta.setDisplayName(ChatColor.GOLD+Lang.translate("search", core));
                    out.setItemMeta(meta);
                    anvil.itemOutput(out);

                    anvil.text(Lang.translate("enterSearch", core));
                    anvil.onClick((integer, stateSnapshot) -> {
                        if (integer != 2) {
                            return List.of();
                        }
                        p.closeInventory();
                        gui(sort, 1, stateSnapshot.getText()).open(p);
                        return List.of();
                    });

                    anvil.onClose(stateSnapshot -> gui(sort, 1, stateSnapshot.getText()).open(p));
                    anvil.open(p);
                }));

        int minSlot = 45*(page-1);
        int maxSlot = 45*page;

        for (int i = minSlot; i < maxSlot; i++) {
            if (materials.size() <= i) break;
            Material m = materials.get(i);
            try {
                gui.setItem(i - minSlot, ItemBuilder.from(m)
                        .name(Component.text(ChatColor.GOLD+m.name()))
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .lore(Component.text(""),
                                Component.text(Lang.translate("clickSelect", core)))
                        .asGuiItem(event -> {
                            sort.setIcon(m);
                            core.itemSorts.replace(sort.getName(), sort);

                            new EditSortGui().gui(sort).open((Player) event.getWhoClicked());
                        }));
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
        return gui;
    }
}

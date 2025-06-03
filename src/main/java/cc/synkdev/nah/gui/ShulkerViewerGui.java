package cc.synkdev.nah.gui;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.objects.BINAuction;
import cc.synkdev.synkLibs.bukkit.Lang;
import cc.synkdev.synkLibs.bukkit.Utils;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ShulkerViewerGui {
    NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    public Gui gui(BINAuction bA) {
        ItemStack item = bA.getItem();
        Utils.log("Type: "+item.getType().name());
        if (!item.getType().name().contains("SHULKER_BOX")) return null;
        Gui gui = Gui.gui().rows(4).title(Component.text(Lang.translate("shulkerViewer", core))).disableAllInteractions().create();
        if (item.getItemMeta() instanceof BlockStateMeta) {
            BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
            if (meta.getBlockState() instanceof ShulkerBox) {
                ShulkerBox box = (ShulkerBox) meta.getBlockState();
                for (ItemStack itemStack : box.getInventory().getContents()) {
                    if (itemStack != null) gui.addItem(ItemBuilder.from(itemStack).asGuiItem());
                }
            }
        }
        gui.getFiller().fillBottom(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).asGuiItem());
        gui.setItem(4, 5, ItemBuilder.from(Material.BARRIER).name(Component.text(Lang.translate("back", core))).asGuiItem(event -> new ConfirmBuyGui().gui(bA).open((Player) event.getWhoClicked())));
        return gui;
    }
}

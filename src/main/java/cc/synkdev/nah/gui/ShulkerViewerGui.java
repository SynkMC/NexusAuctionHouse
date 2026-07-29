package cc.synkdev.nah.gui;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.objects.BINAuction;
import cc.synkdev.nexusCore.bukkit.Lang;
import cc.synkdev.triumph.builder.item.ItemBuilder;
import cc.synkdev.triumph.guis.Gui;
import cc.synkdev.kyori.adventure.text.Component;
import cc.synkdev.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ShulkerViewerGui {
    NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    public Gui gui(BINAuction bA) {
        ItemStack item = bA.getItem();
        if (!item.getType().name().contains("SHULKER_BOX")) return null;
        Gui gui = Gui.gui().rows(4).title(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.translate("shulkerViewer", core))).disableAllInteractions().create();
        if (item.getItemMeta() instanceof BlockStateMeta) {
            BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
            if (meta.getBlockState() instanceof ShulkerBox) {
                ShulkerBox box = (ShulkerBox) meta.getBlockState();
                for (ItemStack itemStack : box.getInventory().getContents()) {
                    if (itemStack != null) gui.addItem(ItemBuilder.from(itemStack).asGuiItem());
                }
            }
        }
        gui.getFiller().fillBottom(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(LegacyComponentSerializer.legacyAmpersand().deserialize(" ")).asGuiItem());
        gui.setItem(4, 5, ItemBuilder.from(Material.BARRIER).name(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.translate("back", core))).asGuiItem(event -> new ConfirmBuyGui().gui(bA).open((Player) event.getWhoClicked())));
        return gui;
    }
}

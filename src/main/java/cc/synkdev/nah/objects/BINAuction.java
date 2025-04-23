package cc.synkdev.nah.objects;

import cc.synkdev.nah.manager.Util;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Getter @Setter
public class BINAuction {
    UUID uuid;
    OfflinePlayer seller;
    ItemStack item;
    int price;
    int expiry;
    OfflinePlayer buyer = null;
    Boolean buyable;
    public BINAuction(String uuid, String seller, String item, String price, String expiry, @Nullable String buyer, Boolean buyable) {
        this.uuid = UUID.fromString(uuid);
        this.seller = Bukkit.getOfflinePlayer(UUID.fromString(seller));
        this.item = Util.deserializeItemstack(item);
        this.price = Integer.parseInt(price);
        this.expiry = Integer.parseInt(expiry);
        if (!buyer.equalsIgnoreCase("")) this.buyer = Bukkit.getOfflinePlayer(UUID.fromString(buyer));
        this.buyable = buyable;
    }
    public BINAuction(UUID uuid, OfflinePlayer seller, ItemStack item, int price, int expiry) {
        this.uuid = uuid;
        this.seller = seller;
        this.item = item;
        this.price = price;
        this.expiry = expiry;
        this.buyer = null;
        this.buyable = true;
    }
    public String saveToString() {
        //uuid;seller;itemstack;price;expiry;buyer;buyable
        StringBuilder sb = new StringBuilder();
        sb.append(getUuid().toString()).append(";");
        sb.append(getSeller().getUniqueId().toString()).append(";");
        sb.append(Util.serializeItemstack(getItem())).append(";");
        sb.append(getPrice()).append(";");
        sb.append(getExpiry()).append(";");
        String buyerS = "";
        if (getBuyer() != null) buyerS = getBuyer().getUniqueId().toString();
        sb.append(buyerS).append(";");
        sb.append(buyable);
        return sb.toString();
    }

}

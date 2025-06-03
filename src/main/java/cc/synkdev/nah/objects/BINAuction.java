package cc.synkdev.nah.objects;

import cc.synkdev.nah.manager.Util;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.UUID;

@Getter @Setter
public class BINAuction {
    private int id;
    private UUID seller;
    private ItemStack item;
    private int price;
    private long expiry;
    private UUID buyer = null;
    private Boolean buyable;
    public BINAuction(int id, String seller, String item, int price, long expiry, @Nullable String buyer, Boolean buyable) {
        this.id = id;
        this.seller = UUID.fromString(seller);
        this.item = Util.deserializeItemstack(item);
        this.price = price;
        this.expiry = expiry;

        assert buyer != null;
        if (!buyer.equalsIgnoreCase("")) this.buyer = UUID.fromString(buyer);

        this.buyable = buyable;
    }
    public BINAuction(int id, UUID seller, ItemStack item, int price, long expiry) {
        this.id = id;
        this.seller = seller;
        this.item = item;
        this.price = price;
        this.expiry = expiry;
        this.buyer = null;
        this.buyable = true;
    }
    public JSONObject export() {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("seller", seller.toString());
        obj.put("item", Util.serializeItemstack(item));
        obj.put("price", price);
        obj.put("expiry", expiry);
        obj.put("buyer", buyer == null ? "" : buyer.toString());
        obj.put("buyable", buyable);
        return obj;
    }

}

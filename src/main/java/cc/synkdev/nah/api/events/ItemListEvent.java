package cc.synkdev.nah.api.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.Date;

@Getter @Setter
public class ItemListEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private Player player;
    private ItemStack item;
    private long price;
    private Date expiry;
    private Boolean cancel;
    public ItemListEvent(Player player, ItemStack item, long price, Date expiry) {
        this.player = player;
        this.item = item;
        this.price = price;
        this.expiry = expiry;
        this.cancel = false;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getPrice() {
        return price;
    }

    public void setExpiry(Date expiry) {
        this.expiry = expiry;
    }

    public Date getExpiry() {
        return expiry;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancel = b;
    }
}

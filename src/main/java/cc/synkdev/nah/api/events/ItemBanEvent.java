package cc.synkdev.nah.api.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter @Setter
public class ItemBanEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private Material item;
    private Boolean cancel;
    public ItemBanEvent (Material item) {
        this.cancel = false;
        this.item = item;
    }

    public void setItem(Material item) {
        this.item = item;
    }

    public Material getItem() {
        return item;
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

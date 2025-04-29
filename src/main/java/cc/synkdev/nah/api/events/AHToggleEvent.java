package cc.synkdev.nah.api.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AHToggleEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private Boolean status;
    private Boolean cancel;
    public AHToggleEvent(Boolean status) {
        this.status = status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Boolean getStatus() {
        return status;
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

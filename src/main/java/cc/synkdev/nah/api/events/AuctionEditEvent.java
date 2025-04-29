package cc.synkdev.nah.api.events;

import cc.synkdev.nah.objects.BINAuction;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AuctionEditEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private BINAuction auction;
    private Boolean cancel;
    public AuctionEditEvent(BINAuction auction) {
        this.auction = auction;
    }

    public void setAuction(BINAuction auction) {
        this.auction = auction;
    }

    public BINAuction getAuction() {
        return auction;
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

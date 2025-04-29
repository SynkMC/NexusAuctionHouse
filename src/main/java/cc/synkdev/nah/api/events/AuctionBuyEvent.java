package cc.synkdev.nah.api.events;

import cc.synkdev.nah.objects.BINAuction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AuctionBuyEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private Player buyer;
    private BINAuction auction;
    private OfflinePlayer seller;
    private Boolean cancel;
    public AuctionBuyEvent(Player buyer, BINAuction auction, OfflinePlayer seller) {
        this.buyer = buyer;
        this.auction = auction;
        this.seller = seller;
    }

    public Player getBuyer() {
        return buyer;
    }

    public void setBuyer(Player buyer) {
        this.buyer = buyer;
    }

    public BINAuction getAuction() {
        return auction;
    }

    public void setAuction(BINAuction auction) {
        this.auction = auction;
    }

    public OfflinePlayer getSeller() {
        return seller;
    }

    public void setSeller(OfflinePlayer seller) {
        this.seller = seller;
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

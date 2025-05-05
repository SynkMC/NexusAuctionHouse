package cc.synkdev.nah.api.events;

import cc.synkdev.nah.objects.BINAuction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter @Setter
public class ItemUnlistEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private Player player;
    private BINAuction auction;
    private Boolean cancel;
    public ItemUnlistEvent (Player player, BINAuction auction) {
        this.player = player;
        this.auction = auction;
        this.cancel = false;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public BINAuction getAuction() {
        return auction;
    }

    public void setAuction(BINAuction auction) {
        this.auction = auction;
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

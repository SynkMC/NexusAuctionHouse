package cc.synkdev.nah.manager;

import cc.synkdev.nah.NexusAuctionHouse;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventHandler implements Listener {
    NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    @org.bukkit.event.EventHandler
    public void onJoin (PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (core.retrieveMap.containsKey(p)) {
            p.sendMessage(core.prefix()+ ChatColor.GOLD+Lang.translate("notifyExpîred"));
        }
    }
}

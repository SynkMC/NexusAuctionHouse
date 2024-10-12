package cc.synkdev.nah.components;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.synkLibs.bukkit.Lang;

import java.util.List;

public enum SortingTypes {
    PRICEMIN (NexusAuctionHouse.getInstance().sortPrice, Lang.translate("priceMin", NexusAuctionHouse.getInstance())), PRICEMAX(NexusAuctionHouse.getInstance().sortPriceMax, Lang.translate("priceMax", NexusAuctionHouse.getInstance())), EXPIRESSOON(NexusAuctionHouse.getInstance().sortExpiry, Lang.translate("expiresSoon", NexusAuctionHouse.getInstance())), LATESTPOSTED(NexusAuctionHouse.getInstance().sortExpiryMax, Lang.translate("latestPosted", NexusAuctionHouse.getInstance()));
    public final List<BINAuction> list;
    public final String string;
    SortingTypes (List<BINAuction> list, String string) {
        this.list = list;
        this.string = string;
    }
}

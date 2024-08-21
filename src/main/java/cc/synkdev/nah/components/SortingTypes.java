package cc.synkdev.nah.components;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.manager.Lang;

import java.util.List;

public enum SortingTypes {
    PRICEMIN (NexusAuctionHouse.getInstance().sortPrice, Lang.translate("priceMin")), PRICEMAX(NexusAuctionHouse.getInstance().sortPriceMax, Lang.translate("priceMax")), EXPIRESSOON(NexusAuctionHouse.getInstance().sortExpiry, Lang.translate("expiresSoon")), LATESTPOSTED(NexusAuctionHouse.getInstance().sortExpiryMax, Lang.translate("latestPosted"));
    public List<BINAuction> list;
    public String string;
    SortingTypes (List<BINAuction> list, String string) {
        this.list = list;
        this.string = string;
    }
}

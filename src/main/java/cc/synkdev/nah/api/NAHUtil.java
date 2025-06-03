package cc.synkdev.nah.api;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.api.events.AHToggleEvent;
import cc.synkdev.nah.api.events.AuctionEditEvent;
import cc.synkdev.nah.api.events.ItemBanEvent;
import cc.synkdev.nah.api.events.ItemUnbanEvent;
import cc.synkdev.nah.gui.LogsGui;
import cc.synkdev.nah.gui.MainGui;
import cc.synkdev.nah.gui.RetrieveGui;
import cc.synkdev.nah.gui.sort.SortsManagementGui;
import cc.synkdev.nah.manager.*;
import cc.synkdev.nah.objects.BINAuction;
import cc.synkdev.synkLibs.bukkit.Lang;
import dev.triumphteam.gui.guis.Gui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Main class used by the Developer API
 */
public class NAHUtil {
    private static final NexusAuctionHouse core = NexusAuctionHouse.getInstance();

    /**
     * Toggle the Auction House to the opposite state
     */
    public static void toggle() {
        toggle(!core.getToggle());
    }

    /**
     * Toggle the Auction House to the specified state
     * @param state the state of the Auction House
     */
    public static void toggle(Boolean state) {
        AHToggleEvent event = new AHToggleEvent(state);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        ToggleManager.set(event.getStatus());

        String status = core.getToggle() ? Lang.translate("on", core) : Lang.translate("off", core);
        WebhookManager.sendWebhook("ah-toggle", null, status.substring(2));
        Bukkit.broadcastMessage(core.prefix()+ ChatColor.GREEN+Lang.translate("broadcast-toggle", core, status));
    }

    /**
     * Open the Auction House GUI for a player
     * @param p the player
     * @param search The search query (if null, displays all items)
     * @param page The page (anything below 1 will display the first page)
     * @param force whether to force opening the gui, ignoring permissions or toggle
     */
    public static void open(Player p, Boolean force, String search, int page) {
        if (page < 1) {
            page = 1;
        }
        if (force) {
            Gui gui = new MainGui().gui(p, page, search, 0, null);
            gui.open(p);
            return;
        }

        if (p.hasPermission("nah.gui.open") || !p.isPermissionSet("nah.gui.open") || p.isOp()) {
            if (!core.getToggle() && !p.hasPermission("nah.toggle.bypass")) {
                p.sendMessage(core.prefix()+Lang.translate("error-disabled", core));
            } else {
                Gui gui = new MainGui().gui(p, page, search, 0, null);
                gui.open(p);
            }
        }
    }

    /**
     * Reload all the plugin configuration
     * @return The time it took to reload (for informational purposes)
     */
    public static Long reload() {
        long time = System.currentTimeMillis();
        core.save();
        core.reloadLang();
        core.reloadConfig();
        ItemSortsManager.read();
        WebhookManager.read();
        ToggleManager.read();
        BannedItemsManager.read();
        time = System.currentTimeMillis()-time;
        return time;
    }

    /**
     * Open the GUI for expired items for a player
     * @param p The player
     */
    public static void openExpiredGui(Player p) {
        if (!core.retrieveMap.containsKey(p.getUniqueId())) {
            p.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("noRetrieve", core));
            return;
        }

        Gui gui = new RetrieveGui().gui(p, 1);
        gui.open(p);
    }

    /**
     * Open the logs GUI
     * @param p The player
     */
    public static void openLogs(Player p) {
        Gui gui = new LogsGui().gui(1);
        gui.open(p);
    }

    /**
     * Set the price of an auction
     * @param bA The auction
     * @param price The new price
     * @param executor The person who sets the price (can be anything)
     */
    public static void setPrice(BINAuction bA, int price, String executor) {
        if (core.expiredBINs.contains(bA)) {
            core.expiredBINs.remove(bA);
            AuctionEditEvent event = new AuctionEditEvent(bA);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }

            bA.setPrice(event.getAuction().getPrice());
            core.expiredBINs.add(bA);
        }

        if (core.runningBINs.contains(bA)) {
            core.runningBINs.remove(bA);
            bA.setPrice(price);
            AuctionEditEvent event = new AuctionEditEvent(bA);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }

            core.runningBINs.add(bA);
        }

        DataFileManager.sort();
        WebhookManager.sendWebhook("listing-edited", bA, executor);
    }

    /**
     * Set the price of an auction
     * @param bA The auction
     * @param expiry The new expiry timestamp
     * @param executor The person who sets the price (can be anything)
     */
    public static void setExpiry(BINAuction bA, long expiry, String executor) {
        if (core.expiredBINs.contains(bA)) {
            core.expiredBINs.remove(bA);
            bA.setExpiry(expiry);
            AuctionEditEvent event = new AuctionEditEvent(bA);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }
            core.expiredBINs.add(bA);
        }

        if (core.runningBINs.contains(bA)) {
            core.runningBINs.remove(bA);
            bA.setExpiry(expiry);
            AuctionEditEvent event = new AuctionEditEvent(bA);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }
            core.runningBINs.add(bA);
        }

        DataFileManager.sort();
        WebhookManager.sendWebhook("listing-edited", bA, executor);
    }

    public static void ban(Material m) {
        ItemBanEvent event = new ItemBanEvent(m);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        BannedItemsManager.add(event.getItem());
    }

    public static void unban(Material m) {
        ItemUnbanEvent event = new ItemUnbanEvent(m);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        BannedItemsManager.remove(event.getItem());
    }

    /**
     * Get an auction from its UUID
     * @param id The auction's ID
     * @return The auction
     */
    public static BINAuction getAuction(int id) {
        AtomicReference<BINAuction> bA = new AtomicReference<>();
        for (BINAuction binAuction : core.runningBINs) {
            if (binAuction.getId() == id) {
                bA.set(binAuction);
            }
        }

        for (BINAuction binAuction : core.expiredBINs) {
            if (binAuction.getId() == id) {
                bA.set(binAuction);
            }
        }
        return bA.get();
    }

    /**
     * Read how many auctions a player can list
     * @param p The player
     * @return The slots cap
     */
    public static int getSlotsLimit(Player p) {
        int slots = -1;
        for (PermissionAttachmentInfo perm : p.getEffectivePermissions()) {
            String s = perm.getPermission();
            if (s.startsWith("nah.slots.")) {
                try {
                    slots = Integer.parseInt(s.replace("nah.slots.", ""));
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return slots;
    }

    /**
     * Read how many auctions a player has currently listed
     * @param p The player
     * @return The used slots count
     */
    public static int getUsedSlots (Player p) {
        return (int) core.runningBINs.stream().filter(bA -> bA.getSeller().equals(p.getUniqueId())).count();
    }

    /**
     * Open the sorts management GUI for a player
     * @param p the player
     */
    public static void openSorts(Player p) {
        new SortsManagementGui().gui(1).open(p);
    }
}

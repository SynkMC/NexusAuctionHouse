package cc.synkdev.nah.commands;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.api.NAHUtil;
import cc.synkdev.nah.gui.ConfirmSellGui;
import cc.synkdev.nah.objects.BINAuction;
import cc.synkdev.synkLibs.bukkit.Lang;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import dev.triumphteam.gui.guis.Gui;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("ah|nah|auctionhouse|nexusauctionhouse")
public class AhCommand extends BaseCommand {
    NexusAuctionHouse core = NexusAuctionHouse.getInstance();

    @Default
    public void onDefault(Player p) {
        NAHUtil.open(p, false, null, 1);
    }

    @Subcommand("search")
    @Syntax("/ah search [query]")
    public void onSearch (Player p, String[] args) {
        if (args.length == 0) {
            NAHUtil.open(p, false, null, 1);
        } else {
            NAHUtil.open(p, false, StringUtils.join(args), 1);
        }
    }

    @Subcommand("reload")
    @CommandPermission("nah.command.reload")
    public void onReload(CommandSender sender) {
        long time = NAHUtil.reload();
        sender.sendMessage(core.prefix()+ChatColor.GREEN+Lang.translate("reloaded", core, time+""));
    }

    @Subcommand("expired")
    public void onExpired(Player p) {
        NAHUtil.openExpiredGui(p);
    }

    @Subcommand("sell")
    @Syntax("/ah sell <price>")
    public void onSell(Player p, String[] args) {
        if (args.length == 0) p.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("sellUsage", core));
        else if (args.length == 1) {
            if (p.getInventory().getItemInMainHand() == null || p.getInventory().getItemInMainHand().getType() == Material.AIR || p.getInventory().getItemInMainHand().getAmount() == 0) {
                p.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("emptyHand", core));
                return;
            }

            if (core.banned.contains(p.getInventory().getItemInMainHand().getType())) {
                p.sendMessage(core.prefix()+Lang.translate("sellBanned", core));
                return;
            }

            if (NAHUtil.getSlotsLimit(p) != -1 && (NAHUtil.getUsedSlots(p)+1) >= NAHUtil.getSlotsLimit(p)) {
                p.sendMessage(Lang.translate("slots-cap", core, NAHUtil.getSlotsLimit(p)+""));
                return;
            }

            int price;
            try {
                price = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                p.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("invalidNumber", core));
                return;
            }

            Gui gui = new ConfirmSellGui().gui(p, price);
            gui.open(p);
        }
    }

    @Subcommand("logs")
    @CommandPermission("nah.command.logs")
    public void onLogs(Player p) {
        NAHUtil.openLogs(p);
    }

    @Subcommand("setprice")
    @CommandPermission("nah.manage.changeprice")
    public void onSetprice(CommandSender sender, String[] args) {
        String uuid = args[0];
        BINAuction bA = NAHUtil.getAuction(uuid);
        if (bA == null) {
            sender.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("doesntExist", core));
            return;
        }

        int price;
        try {
            price = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("invalidNumber", core));
            return;
        }
        String sendr = "Console";
        if (sender instanceof Player) sendr = ((Player) sender).getName();
        NAHUtil.setPrice(bA, price, sendr);
        sender.sendMessage(core.prefix()+ChatColor.GREEN+Lang.translate("successChangePrice", core, price+""));

    }

    @Subcommand("setexpiry")
    @CommandPermission("nah.manage.changeexpiry")
    public void onSetexpiry(CommandSender sender, String[] args) {
        String uuid = args[0];
        BINAuction bA = NAHUtil.getAuction(uuid);
        if (bA == null) {
            sender.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("doesntExist", core));
            return;
        }

        int expiry;
        try {
            expiry = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("invalidNumber", core));
            return;
        }


        String sendr = "Console";
        if (sender instanceof Player) sendr = ((Player) sender).getName();
        NAHUtil.setExpiry(bA, expiry, sendr);
        sender.sendMessage(core.prefix()+ChatColor.GREEN+Lang.translate("successChangeExpiry", core, expiry+""));
    }

    @Subcommand("ban")
    @CommandPermission("nah.command.ban")
    public void onBan(Player p) {
        if (p.getInventory().getItemInMainHand() == null) {
            p.sendMessage(core.prefix()+ChatColor.RED+"Couldn't ban the item in your hand since it is empty!");
            return;
        }

        Material m = p.getInventory().getItemInMainHand().getType();
        NAHUtil.ban(m);
        p.sendMessage(core.prefix()+ChatColor.GREEN+"Made "+m.name()+" not sellable on the AH!");
    }

    @Subcommand("unban")
    @CommandPermission("nah.command.ban")
    public void onUnban(Player p) {
        if (p.getInventory().getItemInMainHand() == null) {
            p.sendMessage(core.prefix()+ChatColor.RED+"Couldn't unban the item in your hand since it is empty!");
            return;
        }

        Material m = p.getInventory().getItemInMainHand().getType();
        NAHUtil.unban(m);
        p.sendMessage(core.prefix()+ChatColor.GREEN+"Made "+m.name()+" sellable on the AH!");
    }

    @Subcommand("toggle")
    @CommandPermission("nah.command.toggle")
    @Description("Toggle the auction house")
    public void onToggle () {
        NAHUtil.toggle();
    }
}

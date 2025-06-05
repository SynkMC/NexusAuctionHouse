package cc.synkdev.nah.commands;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.api.NAHUtil;
import cc.synkdev.nah.gui.ConfirmSellGui;
import cc.synkdev.nah.objects.BINAuction;
import cc.synkdev.synkLibs.bukkit.Analytics;
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
    private final NexusAuctionHouse core = NexusAuctionHouse.getInstance();

    @Default
    public void onDefault(Player p) {
        Analytics.addCommandUse(core, "ah");
        NAHUtil.open(p, false, null, 1);
    }

    @Subcommand("search")
    @Syntax("/ah search [query]")
    @CommandPermission("nah.command.search")
    public void onSearch (Player p, String[] args) {
        Analytics.addCommandUse(core, "ah search");
        if (args.length == 0) {
            NAHUtil.open(p, false, null, 1);
        } else {
            NAHUtil.open(p, false, StringUtils.join(args), 1);
        }
    }

    @Subcommand("reload")
    @CommandPermission("nah.command.reload")
    public void onReload(CommandSender sender) {
        Analytics.addCommandUse(core, "ah reload");
        long time = NAHUtil.reload();
        sender.sendMessage(core.prefix()+ChatColor.GREEN+Lang.translate("reloaded", core, time+""));
    }

    @Subcommand("expired")
    @CommandPermission("nah.command.expired")
    public void onExpired(Player p) {
        Analytics.addCommandUse(core, "ah expired");
        NAHUtil.openExpiredGui(p);
    }

    @Subcommand("sell")
    @Syntax("/ah sell <price>")
    @CommandPermission("nah.command.sell")
    public void onSell(Player p, String[] args) {
        Analytics.addCommandUse(core, "ah sell");
        if (args.length == 0) {
            p.sendMessage(core.prefix() + ChatColor.RED + Lang.translate("sellUsage", core));
            return;
        }
        if (p.getInventory().getItemInMainHand() == null || p.getInventory().getItemInMainHand().getType() == Material.AIR || p.getInventory().getItemInMainHand().getAmount() == 0) {
            p.sendMessage(core.prefix() + ChatColor.RED + Lang.translate("emptyHand", core));
            return;
        }

        if (core.banned.contains(p.getInventory().getItemInMainHand().getType())) {
            p.sendMessage(core.prefix() + Lang.translate("sellBanned", core));
            return;
        }

        if (NAHUtil.getSlotsLimit(p) != -1 && (NAHUtil.getUsedSlots(p) + 1) >= NAHUtil.getSlotsLimit(p)) {
            p.sendMessage(Lang.translate("slots-cap", core, NAHUtil.getSlotsLimit(p) + ""));
            return;
        }

        long price;
        try {
            price = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            p.sendMessage(core.prefix() + ChatColor.RED + Lang.translate("invalidNumber", core));
            return;
        }

        if (price <= 0) {
            p.sendMessage(core.prefix() + ChatColor.RED + Lang.translate("invalidNumber", core));
            return;
        }

        Gui gui = new ConfirmSellGui().gui(p, price);
        gui.open(p);

    }

    @Subcommand("logs")
    @CommandPermission("nah.command.logs")
    public void onLogs(Player p) {
        Analytics.addCommandUse(core, "ah sell");
        NAHUtil.openLogs(p);
    }

    @Subcommand("setprice")
    @CommandPermission("nah.manage.changeprice")
    public void onSetprice(CommandSender sender, String[] args) {
        Analytics.addCommandUse(core, "ah setprice");
        int id;
        try {
            id = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("invalidNumber", core));
            return;
        }
        BINAuction bA = NAHUtil.getAuction(id);
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
        if (sender instanceof Player) {
            sendr = sender.getName();
        }
        NAHUtil.setPrice(bA, price, sendr);
        sender.sendMessage(core.prefix()+ChatColor.GREEN+Lang.translate("successChangePrice", core, price+""));

    }

    @Subcommand("ban")
    @CommandPermission("nah.command.ban")
    public void onBan(Player p) {
        Analytics.addCommandUse(core, "ah ban");
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
        Analytics.addCommandUse(core, "ah unban");
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
    public void onToggle() {
        Analytics.addCommandUse(core, "ah toggle");
        NAHUtil.toggle();
    }

    @Subcommand("sorts")
    @CommandPermission("nah.command.sorts")
    @Description("Manage item sorts")
    public void onSorts(Player p) {
        Analytics.addCommandUse(core, "ah sorts");
        NAHUtil.openSorts(p);
    }
}

package cc.synkdev.nah.commands;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.components.BINAuction;
import cc.synkdev.nah.gui.ConfirmSellGui;
import cc.synkdev.nah.gui.LogsGui;
import cc.synkdev.nah.gui.MainGui;
import cc.synkdev.nah.gui.RetrieveGui;
import cc.synkdev.nah.manager.DataFileManager;
import cc.synkdev.nah.manager.Lang;
import cc.synkdev.nah.manager.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Material.AIR;

public class AhCommand implements CommandExecutor, TabExecutor {
    CommandSender sender;
    NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        this.sender = sender;
        if (args.length >= 2 && args[0].equalsIgnoreCase("search")) {
            if (sender instanceof Player) {
                if (args.length == 2) new MainGui().gui((Player) sender, 1, args[1]).open((Player) sender);
                else {
                    StringBuilder sb = new StringBuilder();
                    int index = 0;
                    for (int i = 1; i < args.length-1; i++) {
                        index = i;
                        sb.append(args[i]).append(" ");
                    }
                    sb.append(args[index+1]);
                    new MainGui().gui((Player) sender, 1, sb.toString()).open((Player) sender);
                }
            } else sender.sendMessage(core.prefix()+ ChatColor.RED+Lang.translate("playerOnly"));
        }
        else if (args.length == 0) {
            if (sender instanceof Player) {
                new MainGui().gui((Player) sender, 1, null).open((Player) sender);
            } else sender.sendMessage(core.prefix()+ ChatColor.RED+Lang.translate("playerOnly"));
        } else if (args.length == 1) {
            switch (args[0]) {
                case "reload":
                    if (checkPerm("nah.command.reload", true)) {
                        long time = System.currentTimeMillis();
                        core.save();
                        new Lang().init();
                        core.reloadConfig();
                        time = System.currentTimeMillis()-time;
                        sender.sendMessage(core.prefix()+ChatColor.GREEN+Lang.translate("reloaded", time+""));
                    }
                    break;
                case "expired":
                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        if (!core.retrieveMap.containsKey(p)) {
                            p.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("noRetrieve"));
                            return true;
                        }

                        new RetrieveGui().gui(p, 1).open(p);
                    } else sender.sendMessage(core.prefix()+ ChatColor.RED+Lang.translate("playerOnly"));
                    break;
                case "sell":
                    sender.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("sellUsage"));
                    break;
                case "logs":
                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        if (!p.hasPermission("nah.menu.logs")) {
                            p.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("noPerm"));
                            return true;
                        }

                        new LogsGui().gui(p, 1).open(p);
                    } else sender.sendMessage(core.prefix()+ ChatColor.RED+Lang.translate("playerOnly"));
                    break;
            }
        } else if (args.length == 2) {
            switch (args[0]) {
                case "sell":
                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        if (p.getInventory().getItemInMainHand() == null || p.getInventory().getItemInMainHand().getType() == Material.AIR || p.getInventory().getItemInMainHand().getAmount() == 0) {
                            p.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("emptyHand"));
                            return true;
                        }

                        int price;
                        try {
                            price = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            p.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("invalidNumber"));
                            return true;
                        }

                        new ConfirmSellGui().gui(p, price).open(p);
                    } else sender.sendMessage(core.prefix()+ ChatColor.RED+Lang.translate("playerOnly"));
                    break;
            }
        } else if (args.length == 3) {
            switch (args[0]) {
                case "setprice":
                    if (checkPerm("nah.manage.changeprice", true)) {
                        String uuid = args[1];
                        BINAuction bA = Util.getAuction(uuid);
                        if (bA == null) {
                            sender.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("doesntExist"));
                            return true;
                        }

                        int price;
                        try {
                            price = Integer.parseInt(args[2]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("invalidNumber"));
                            return true;
                        }

                        if (core.expiredBINs.contains(bA)) {
                            core.expiredBINs.remove(bA);
                            bA.setPrice(price);
                            core.expiredBINs.add(bA);
                        }

                        if (core.runningBINs.containsKey(bA)) {
                            core.runningBINs.remove(bA);
                            bA.setPrice(price);
                            core.runningBINs.put(bA, bA.getExpiry());
                        }

                        DataFileManager.sort();
                        sender.sendMessage(core.prefix()+ChatColor.GREEN+Lang.translate("successChangePrice", price+""));
                    }
                    break;
                case "setexpiry":
                    if (checkPerm("nah.manage.changeexpiry", true)) {
                        String uuid = args[1];
                        BINAuction bA = Util.getAuction(uuid);
                        if (bA == null) {
                            sender.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("doesntExist"));
                            return true;
                        }

                        int expiry;
                        try {
                            expiry = Integer.parseInt(args[2]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("invalidNumber"));
                            return true;
                        }

                        if (core.expiredBINs.contains(bA)) {
                            core.expiredBINs.remove(bA);
                            bA.setExpiry(expiry);
                            core.expiredBINs.add(bA);
                        }

                        if (core.runningBINs.containsKey(bA)) {
                            core.runningBINs.remove(bA);
                            bA.setExpiry(expiry);
                            core.runningBINs.put(bA, bA.getExpiry());
                        }

                        DataFileManager.sort();
                        sender.sendMessage(core.prefix()+ChatColor.GREEN+Lang.translate("successChangeExpiry", expiry+""));
                    }
                    break;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        this.sender = sender;
        List<String> comps = new ArrayList<>();
        if (args.length == 1) {
            comps.add("sell");
            comps.add("search");
            if (checkPerm("nah.command.reload", false)) {
                comps.add("reload");
            }
            if (checkPerm("nah.menu.logs", false)) {
                comps.add("logs");
            }
        }
        return comps;
    }
    private Boolean checkPerm(String s, Boolean msg) {
        Boolean has = sender.hasPermission(s);
        if (msg && !has) {
            sender.sendMessage(core.prefix()+ ChatColor.RED+Lang.translate("noPerm"));
        }
        return has;
    }
}

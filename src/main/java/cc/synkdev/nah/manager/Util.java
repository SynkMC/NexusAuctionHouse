package cc.synkdev.nah.manager;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.components.BINAuction;
import cc.synkdev.nah.components.SortingTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Util {
    private static final NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    public static String serializeItemstack(ItemStack item) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BukkitObjectOutputStream bukkitOut = new BukkitObjectOutputStream(out);
            bukkitOut.writeObject(item);
            bukkitOut.close();
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static ItemStack deserializeItemstack(String s) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(s));
            BukkitObjectInputStream bukkitIn = new BukkitObjectInputStream(in);
            ItemStack ret = (ItemStack) bukkitIn.readObject();
            bukkitIn.close();
            return ret;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static String encodeString(String s) {
        return Base64.getEncoder().encodeToString(s.getBytes());
    }
    public static String decodeString(String s) {
        return new String(Base64.getDecoder().decode(s));
    }
    public static List<Component> loreToComps(ItemStack item) {
        List<Component> list = new ArrayList<>();
        if (item.getItemMeta().getLore() == null) return list;

        item.getItemMeta().getLore().forEach(s -> {
            list.add(Component.text(s));
        });
        return list;
    }
    public static String convertSecondsToTime(long seconds) {
        seconds = seconds-System.currentTimeMillis()/1000;
        long weeks = seconds / (7 * 24 * 60 * 60);
        seconds %= (7 * 24 * 60 * 60);
        long days = seconds / (24 * 60 * 60);
        seconds %= (24 * 60 * 60);
        long hours = seconds / (60 * 60);
        seconds %= (60 * 60);
        long minutes = seconds / 60;

        StringBuilder timeString = new StringBuilder();

        if (weeks > 0) {
            timeString.append(weeks).append(" ").append(Lang.translate("week"));
            timeString.append(", ");
        }
        if (days > 0) {
            timeString.append(days).append(" ").append(Lang.translate("day"));
            timeString.append(", ");
        }
        if (hours > 0) {
            timeString.append(hours).append(" ").append(Lang.translate("hour"));
            timeString.append(", ");
        }
        if (minutes > 0) {
            timeString.append(minutes).append(" ").append(Lang.translate("minute"));
            timeString.append(", ");
        }

        return timeString.toString();
    }
    public static void staffBc(String message) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.hasPermission("nah.staffmessages")) {
                player.sendMessage(message);
            }
        });
    }
    public static List<BINAuction> searchList(String message, SortingTypes sort) {
        List<BINAuction> list = new ArrayList<>();
        core.runningBINs.forEach((binAuction, integer) -> {
            if (binAuction.getItem().getItemMeta().getDisplayName().toLowerCase().contains(message.toLowerCase())) {
                if (!list.contains(binAuction)) list.add(binAuction);
            }
            if (binAuction.getSeller().getName().toLowerCase().contains(message.toLowerCase())) {
                if (!list.contains(binAuction)) list.add(binAuction);
            }
            if (binAuction.getItem().getType().name().toLowerCase().contains(message.toLowerCase())) {
                if (!list.contains(binAuction)) list.add(binAuction);
            }
        });
        switch (sort) {
            case PRICEMIN:
                list.sort(Comparator.comparingInt(BINAuction::getPrice));
                break;
            case PRICEMAX:
                list.sort(Comparator.comparingInt(BINAuction::getPrice));
                reverseList(list);
                break;
            case EXPIRESSOON:
                list.sort(Comparator.comparingInt(BINAuction::getExpiry));
                break;
            case LATESTPOSTED:
                list.sort(Comparator.comparingInt(BINAuction::getExpiry));
                reverseList(list);
                break;
        }
        return list;
    }

    public static List<BINAuction> reverseList(List<BINAuction> list) {
        List<BINAuction> listT = new ArrayList<>();
        for (int i = list.size()-1; i >= 0; i--) {
            listT.add(list.get(i));
        }
        return listT;
    }
    public static BINAuction getAuction(String uuid) {
        AtomicReference<BINAuction> bA = new AtomicReference<>();
        core.runningBINs.forEach((binAuction, integer) -> {
            if (binAuction.getUuid().toString().equals(uuid)) bA.set(binAuction);
        });

        for (BINAuction binAuction : core.expiredBINs) {
            if (binAuction.getUuid().toString().equals(uuid)) bA.set(binAuction);
        }
        return bA.get();
    }
    public static BINAuction getAuction(UUID uuid) {
        return getAuction(uuid.toString());
    }
}

package cc.synkdev.nah.manager;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.objects.BINAuction;
import cc.synkdev.nah.objects.SortingTypes;
import cc.synkdev.synkLibs.bukkit.Lang;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("ALL")
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
        if (!item.hasItemMeta() || item.getItemMeta().getLore() == null) return list;

        item.getItemMeta().getLore().forEach(s -> list.add(Component.text(s)));
        return list;
    }
    public static String convertSecondsToTime(long seconds) {
        seconds = seconds-System.currentTimeMillis()/1000;
        int years = Math.toIntExact(seconds / (365 * 24 * 60 * 60));
        seconds %= (365 * 24 * 60 * 60);
        int months = Math.toIntExact(seconds / (30 * 24 * 60 * 60));
        seconds %= (30 * 24 * 60 * 60);
        int weeks = Math.toIntExact(seconds / (7 * 24 * 60 * 60));
        seconds %= (7 * 24 * 60 * 60);
        int days = Math.toIntExact(seconds / (24 * 60 * 60));
        seconds %= (24 * 60 * 60);
        int hours = Math.toIntExact(seconds / (60 * 60));
        seconds %= (60 * 60);
        int minutes = Math.toIntExact(seconds / 60);

        StringBuilder timeString = new StringBuilder();

        if (years > 0) {
            timeString.append(years).append(" ").append(Lang.translate("year", core));
            timeString.append(", ");
        }
        if (months > 0) {
            timeString.append(months).append(" ").append(Lang.translate("month", core));
            timeString.append(", ");
        }
        if (weeks > 0) {
            timeString.append(weeks).append(" ").append(Lang.translate("week", core));
            timeString.append(", ");
        }
        if (days > 0) {
            timeString.append(days).append(" ").append(Lang.translate("day", core));
            timeString.append(", ");
        }
        if (hours > 0) {
            timeString.append(hours).append(" ").append(Lang.translate("hour", core));
            timeString.append(", ");
        }
        if (minutes > 0) {
            timeString.append(minutes).append(" ").append(Lang.translate("minute", core));
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
        for(BINAuction binAuction : core.runningBINs) {
            if (binAuction.getItem().getItemMeta().getDisplayName().toLowerCase().contains(message.toLowerCase())) {
                if (list.contains(binAuction)) list.add(binAuction);
            }
            if (Util.getName(binAuction.getSeller()).toLowerCase().contains(message.toLowerCase())) {
                if (!list.contains(binAuction)) list.add(binAuction);
            }
            if (binAuction.getItem().getType().name().toLowerCase().contains(message.toLowerCase())) {
                if (!list.contains(binAuction)) list.add(binAuction);
            }
        }
        switch (sort) {
            case PRICEMIN:
                list.sort(Comparator.comparingInt(BINAuction::getPrice));
                break;
            case PRICEMAX:
                list.sort(Comparator.comparingInt(BINAuction::getPrice));
                reverseList(list);
                break;
            case EXPIRESSOON:
                list.sort(Comparator.comparingLong(BINAuction::getExpiry));
                break;
            case LATESTPOSTED:
                list.sort(Comparator.comparingLong(BINAuction::getExpiry));
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
    public static String addPlaceholders (String s, String... args) {
        int i = 0;
        for (String arg : args) {
            s = s.replace("%s"+(i+1)+"%", arg);
            i++;
        }
        return s;
    }
    public static String sanitizeDiscordMsg(String input) {
        if (input == null) return "";
        return input
                .replace("\\", "\\\\")
                .replace("*", "\\*")
                .replace("_", "\\_")
                .replace("~", "\\~")
                .replace("`", "\\`");
    }
    public static Boolean isSeller(Player p, BINAuction bA) {
        return p.getUniqueId().equals(bA.getSeller());
    }
    public static long parseDurationToSeconds(String input) {
        Map<String, Long> timeUnits = Map.of(
                "s", 1L,
                "m", 60L,
                "h", 3600L,
                "d", 86400L,
                "w", 604800L,
                "mo", 2592000L,
                "y", 31536000L
        );

        Matcher matcher = Pattern.compile("(?i)(\\d+)([a-z]+)").matcher(input.trim());

        if (matcher.matches()) {
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();

            Long multiplier = timeUnits.get(unit);
            if (multiplier != null) {
                return value * multiplier;
            } else {
                throw new IllegalArgumentException("Unknown time unit: " + unit);
            }
        } else {
            long ret;
            try {
                ret = Long.parseLong(input);
                return ret;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid time string", e);
            }
        }
    }
    public static Boolean isOnline(UUID u) {
        return Bukkit.getOfflinePlayer(u).isOnline();
    }
    public static String getName(UUID u) {
        return Bukkit.getOfflinePlayer(u).getName();
    }
    public static String formatTimestamp(long timestampInSeconds) {
        long timestampInMilliseconds = timestampInSeconds * 1000;
        Date date = new Date(timestampInMilliseconds);

        SimpleDateFormat sdf = new SimpleDateFormat(core.getDateFormat());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(date);
    }
    public static List<Material> getFilteredMaterials() {
        List<Material> result = new ArrayList<>();
        try {
            for (Field field : Material.class.getDeclaredFields()) {
                if (field.getName().contains("AIR")) continue;

                if (field.isEnumConstant() && !field.isAnnotationPresent(Deprecated.class)) {
                    Material value = Material.valueOf(field.getName());
                    result.add(value);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }
    public static List<Material> getFilteredMaterials(String search) {
        if (search == null) return getFilteredMaterials();
        List<Material> result = new ArrayList<>();
        try {
            for (Field field : Material.class.getDeclaredFields()) {
                if (field.getName().contains("AIR")) continue;

                if (field.isEnumConstant() && !field.isAnnotationPresent(Deprecated.class) && field.getName().contains(search.toUpperCase())) {
                    Material value = Material.valueOf(field.getName());
                    result.add(value);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

}

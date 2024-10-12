package cc.synkdev.nah.manager;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.components.BINAuction;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class DataFileManager {
    static NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    static File folder = new File(core.getDataFolder(), "data");
    static File file = new File(folder, "bins.yml");
    static File expired = new File(folder, "expired-retrieve.yml");
    public static void init() {
        try {
            if (!folder.exists()) folder.mkdirs();
            if (!file.exists()) file.createNewFile();
            if (!expired.exists()) expired.createNewFile();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void load() {
        List<BINAuction> expiredBins = new ArrayList<>();
        Map<BINAuction, Integer> running = new HashMap<>();
        Map<OfflinePlayer, List<ItemStack>> retrieveMap = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;

            while ((line = reader.readLine()) != null) {
                String[] split = line.split(";");
                Boolean buyable = split[6].equals("true");
                BINAuction bA = new BINAuction(split[0], split[1], split[2], split[3], split[4], split[5], buyable);
                if (buyable) {
                    running.put(bA, bA.getExpiry());
                }
                else {
                    if (Math.toIntExact(System.currentTimeMillis()/1000)-bA.getExpiry() < core.getKeepLogTime()) expiredBins.add(bA);
                }
            }

            reader = new BufferedReader(new FileReader(expired));

            while ((line = reader.readLine()) != null) {
                String[] split = line.split(";");
                OfflinePlayer op = null;
                List<ItemStack> list = new ArrayList<>();
                if (split.length>1) {
                    op = Bukkit.getOfflinePlayer(UUID.fromString(split[0]));
                    for (int i = 1; i < split.length; i++) {
                        String s = split[i];
                        if (s != null && !s.equals("")) {
                            list.add(Util.deserializeItemstack(s));
                        }
                    }
                }

                if (op != null) {
                    retrieveMap.put(op, list);
                }
            }

            reader.close();
            core.expiredBINs.addAll(expiredBins);
            core.runningBINs.putAll(running);
            core.retrieveMap.putAll(retrieveMap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void sort() {
        core.sortExpiry.clear();
        core.sortExpiryMax.clear();
        core.sortPrice.clear();
        core.sortPriceMax.clear();
        core.runningBINs.forEach((binAuction, integer) -> {
            core.sortExpiry.add(binAuction);
            core.sortPrice.add(binAuction);
        });
        Collections.sort(core.sortExpiry, Comparator.comparingInt(BINAuction::getExpiry));
        Collections.sort(core.sortPrice, Comparator.comparingInt(BINAuction::getPrice));
        core.sortExpiryMax.addAll(Util.reverseList(core.sortExpiry));
        core.sortPriceMax.addAll(Util.reverseList(core.sortPrice));

        core.expiredBINs.sort(Comparator.comparingInt(BINAuction::getExpiry));
        List<BINAuction> copy = new ArrayList<>(core.expiredBINs);
        core.expiredBINs.clear();
        core.expiredBINs.addAll(Util.reverseList(copy));
    }
    public static void save() {
        try {
            File temp = new File(folder, "temp-"+System.currentTimeMillis()+".yml");
            BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
            core.expiredBINs.forEach(binAuction -> {
                try {
                    writer.write(binAuction.saveToString());
                    writer.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            core.runningBINs.forEach((binAuction, integer) -> {
                try {
                    writer.write(binAuction.saveToString());
                    writer.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            writer.close();
            file.delete();
            Files.move(temp.toPath(), file.toPath());

            temp = new File(folder, "temp-retrieve-"+System.currentTimeMillis()+".yml");
            temp.createNewFile();
            BufferedWriter writer1 = new BufferedWriter(new FileWriter(temp));
            core.retrieveMap.forEach((offlinePlayer, itemStacks) -> {
                StringBuilder sb = new StringBuilder();
                sb.append(offlinePlayer.getUniqueId().toString()).append(";");
                if (itemStacks.size() <= 1) {
                    sb.append(Util.serializeItemstack(itemStacks.get(0)));
                } else {
                    int index = 0;
                    for (int i = 0; i < itemStacks.size()-1; i++) {
                        index = i;
                        sb.append(Util.serializeItemstack(itemStacks.get(i))).append(";");
                    }
                    sb.append(Util.serializeItemstack(itemStacks.get(index+1)));
                }
                try {
                    writer1.write(sb.toString());
                    writer1.newLine();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            writer1.close();
            expired.delete();
            Files.move(temp.toPath(), expired.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

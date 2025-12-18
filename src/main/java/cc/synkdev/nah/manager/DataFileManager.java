package cc.synkdev.nah.manager;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.objects.BINAuction;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class DataFileManager {
    private static final NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    private static final File folder = new File(core.getDataFolder(), "data");
    private static final File yml = new File(folder, "bins.yml");
    private static final File json = new File(folder, "bins.json");
    private static final File expiredYml = new File(folder, "expired-retrieve.yml");
    private static final File expiredJson = new File(folder, "expired-retrieve.json");

    public static void init() {
        try {
            if (!folder.exists()) folder.mkdirs();
            if (!json.exists()) json.createNewFile();
            if (!expiredJson.exists()) expiredJson.createNewFile();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadYml() {
        List<BINAuction> expiredBins = new ArrayList<>();
        List<BINAuction> running = new ArrayList<>();
        Map<UUID, List<ItemStack>> retrieveMap = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(yml));

            String line;

            while ((line = reader.readLine()) != null) {
                String[] split = line.split(";");
                BINAuction bA = new BINAuction(core.getId(), split[1], split[2], Long.parseLong(split[3]), Long.parseLong(split[4]), split[5]);
                core.setId(core.getId() + 1);
                if (bA.getBuyable()) {
                    running.add(bA);
                } else {
                    if (Math.toIntExact(System.currentTimeMillis() / 1000) - bA.getExpiry() < core.getKeepLogTime())
                        expiredBins.add(bA);
                }
            }

            if (!expiredYml.exists()) return;

            reader = new BufferedReader(new FileReader(expiredYml));

            while ((line = reader.readLine()) != null) {
                String[] split = line.split(";");
                UUID uuid = null;
                List<ItemStack> list = new ArrayList<>();
                if (split.length > 1) {
                    uuid = UUID.fromString(split[0]);
                    for (int i = 1; i < split.length; i++) {
                        String s = split[i];
                        if (s != null && !s.isEmpty()) {
                            list.add(Util.deserializeItemstack(s));
                        }
                    }
                }

                retrieveMap.put(uuid, list);
            }

            reader.close();
            core.expiredBINs.addAll(expiredBins);
            core.runningBINs.addAll(running);
            core.retrieveMap.putAll(retrieveMap);

            yml.delete();
            expiredYml.delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void load() {
        if (yml.exists()) {
            loadYml();
            return;
        }

        List<BINAuction> expiredBins = new ArrayList<>();
        List<BINAuction> running = new ArrayList<>();
        Map<UUID, List<ItemStack>> retrieveMap = new HashMap<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(json));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            reader.close();

            if (!sb.toString().isEmpty()) {
                JSONObject binsO = new JSONObject(sb.toString());
                if (binsO.has("money")) core.money = binsO.getInt("money");
                JSONArray bins = binsO.getJSONArray("bins");
                for (Object o : bins) {
                    JSONObject obj = (JSONObject) o;
                    int id = obj.getInt("id");
                    while (containsId(running, expiredBins, id)) {
                        id = core.getId() + 1;
                        core.setId(id);
                    }
                    if (id > core.getId()) core.setId(id + 1);
                    String seller = obj.getString("seller");
                    String item = obj.getString("item");
                    long price = obj.getLong("price");
                    long expiry = obj.getLong("expiry");
                    String buyer = obj.getString("buyer");
                    BINAuction bA = new BINAuction(id, seller, item, price, expiry, buyer);
                    if (bA.getItem().getType() == Material.AIR) continue;

                    if (bA.getBuyable()) {
                        running.add(bA);
                    } else {
                        if (Math.toIntExact(System.currentTimeMillis() / 1000) - bA.getExpiry() < core.getKeepLogTime())
                            expiredBins.add(bA);
                    }
                }
            }

            reader = new BufferedReader(new FileReader(expiredJson));
            sb.setLength(0);
            sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            reader.close();
            if (!sb.toString().isEmpty()) {
                JSONObject expiredObj = new JSONObject(sb.toString());
                JSONArray players = expiredObj.getJSONArray("players");
                for (Object o : players) {
                    JSONObject obj = (JSONObject) o;
                    String uuid = obj.getString("uuid");
                    JSONArray items = obj.getJSONArray("items");
                    List<ItemStack> list = new ArrayList<>();
                    for (Object o1 : items) {
                        String item = (String) o1;
                        list.add(Util.deserializeItemstack(item));
                    }
                    retrieveMap.put(UUID.fromString(uuid), list);
                }
            }
            core.expiredBINs.addAll(expiredBins);
            core.runningBINs.addAll(running);
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
        core.sortExpiry.addAll(core.runningBINs);
        core.sortPrice.addAll(core.runningBINs);
        core.sortExpiry.sort(Comparator.comparingLong(BINAuction::getExpiry));
        core.sortPrice.sort(Comparator.comparingLong(BINAuction::getPrice));
        core.sortExpiryMax.addAll(Util.reverseList(core.sortExpiry));
        core.sortPriceMax.addAll(Util.reverseList(core.sortPrice));

        core.expiredBINs.sort(Comparator.comparingLong(BINAuction::getExpiry));
        List<BINAuction> copy = new ArrayList<>(core.expiredBINs);
        core.expiredBINs.clear();
        core.expiredBINs.addAll(Util.reverseList(copy));
    }

    private static boolean containsId(List<BINAuction> running, List<BINAuction> expiredBins, int id) {
        return running.stream().anyMatch(bin -> bin.getId() == id) ||
                expiredBins.stream().anyMatch(bin -> bin.getId() == id);
    }

    public static void save() {
        try {
            File temp = new File(folder, "temp-" + System.currentTimeMillis() + ".json");
            BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
            JSONObject binObj = new JSONObject();
            binObj.put("money", core.money);
            JSONArray bins = new JSONArray();
            for (BINAuction bin : core.expiredBINs) {
                bins.put(bin.export());
            }
            for (BINAuction bin : core.runningBINs) {
                bins.put(bin.export());
            }
            binObj.put("bins", bins);
            writer.write(binObj.toString(2));
            writer.newLine();
            writer.close();
            json.delete();
            Files.move(temp.toPath(), json.toPath());

            temp = new File(folder, "temp-retrieve-" + System.currentTimeMillis() + ".json");
            temp.createNewFile();
            BufferedWriter writer1 = new BufferedWriter(new FileWriter(temp));
            JSONObject expiredObj = new JSONObject();
            JSONArray players = new JSONArray();
            core.retrieveMap.forEach((offlinePlayer, itemStacks) -> {
                JSONObject o = new JSONObject();
                o.put("uuid", offlinePlayer.toString());
                JSONArray items = new JSONArray();
                itemStacks.forEach(itemStack -> items.put(Util.serializeItemstack(itemStack)));
                o.put("items", items);
                players.put(o);
            });
            expiredObj.put("players", players);
            try {
                writer1.write(expiredObj.toString(2));
                writer1.newLine();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            writer1.close();
            expiredJson.delete();
            Files.move(temp.toPath(), expiredJson.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

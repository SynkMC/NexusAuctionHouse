package cc.synkdev.nah.manager;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.objects.ItemSort;
import org.bukkit.Material;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ItemSortsManager {
    private static final NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    private static File file = new File(new File(core.getDataFolder(), "data"), "sorts.json");
    public static void read() {
        core.itemSorts.clear();
        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                StringBuilder sb = new StringBuilder();
                String ln;
                while ((ln = reader.readLine()) != null) {
                    sb.append(ln);
                }
                if (sb.toString().isEmpty()) return;
                JSONObject obj = new JSONObject(sb.toString());
                for (Object o : obj.getJSONArray("sorts")) {
                    JSONObject sort = (JSONObject) o;
                    List<Material> list = new ArrayList<>();
                    JSONArray contents = sort.getJSONArray("contents");
                    for (Object content : contents) {
                        list.add(Material.valueOf((String) content));
                    }
                    core.itemSorts.put(Util.color(sort.getString("name")), new ItemSort(sort.getString("name"), Material.valueOf(sort.getString("icon")), list));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void save() {
        File temp = new File(file.getParentFile(), "temp-" + System.currentTimeMillis() + ".json");
        JSONArray arr = new JSONArray();
        for (ItemSort sort : core.itemSorts.values()) {
            arr.put(sort.export());
        }
        JSONObject obj = new JSONObject();
        obj.put("sorts", arr);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
            writer.write(obj.toString(2));
            writer.newLine();
            writer.close();

            file.delete();
            Files.copy(temp.toPath(), file.toPath());
            temp.delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

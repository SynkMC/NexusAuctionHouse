package cc.synkdev.nah.manager;

import cc.synkdev.nah.NexusAuctionHouse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.ChatColor;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class Lang {
    static final NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    static final File file = new File(core.getDataFolder(), "lang.json");
    public static void init() {
        try {
            if (!file.exists()) file.createNewFile();
            File temp = new File("lang-temp-"+System.currentTimeMillis()+".json");
            temp.createNewFile();
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://synkdev.cc/storage/lang-nah.json").openStream()));
            BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
            read();

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }

            writer.close();
            reader.close();

            Boolean[] changed = {false};

            Gson gson = new Gson();
            try (FileReader fileReader = new FileReader(temp)) {
                Map<String, String> tempMap = new HashMap<>(gson.fromJson(fileReader, HashMap.class));

                tempMap.forEach((s, s2) -> {
                    if (!core.langMap.containsKey(s)) {
                        changed[0] = true;
                        core.langMap.put(s, s2);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (changed[0]) {
                Gson gson1 = new GsonBuilder().setPrettyPrinting().create();
                try (FileWriter writer1 = new FileWriter(file)) {
                    gson1.toJson(core.langMap, writer1);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            file.delete();
            Files.move(temp.toPath(), file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void read() {
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(file)) {
            Map<String, String> map = gson.fromJson(reader, HashMap.class);
            if (map != null) core.langMap.putAll(map);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String translate(String s) {
        return ChatColor.translateAlternateColorCodes('&', core.langMap.getOrDefault(s, "Invalid translation"));
    }

    public static String translate(String s, String s1) {
        return translate(s).replace("%s1%", s1);
    }

    public static String translate(String s, String s1, String s2) {
        return translate(s, s1).replace("%s2%", s2);
    }

    public static String translate(String s, String s1, String s2, String s3) {
        return translate(s, s1, s2).replace("%s3%", s3);
    }
}
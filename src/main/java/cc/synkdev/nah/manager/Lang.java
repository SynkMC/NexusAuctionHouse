package cc.synkdev.nah.manager;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.synkLibs.bukkit.Utils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Lang {
    private static final NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    private static final File file = new File(core.getDataFolder(), "lang.yml");
    private static FileConfiguration config;
    public static void init() {
        try {
            if (!file.exists()) file.createNewFile();

            config = Utils.loadWebConfig("https://synkdev.cc/storage/lang-nah.php", file);
            config = YamlConfiguration.loadConfiguration(file);
            core.lang = config;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String translate(String s) {
        String ret = core.lang.getString(s);
        if (ret == null) {
            init();
        }
        return ret;
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

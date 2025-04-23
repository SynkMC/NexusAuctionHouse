package cc.synkdev.nah.manager;

import cc.synkdev.nah.NexusAuctionHouse;

import java.io.*;

public class ToggleManager {
    private final static NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    private static File file = new File(new File(core.getDataFolder(), "data"), "toggle.yml");
    public static void set(Boolean bool) {
        core.setToggle(bool);
        try {
            file.delete();
            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(bool.toString());
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void read() {
        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String ln;
                while ((ln = reader.readLine()) != null) {
                    core.setToggle(Boolean.valueOf(ln));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

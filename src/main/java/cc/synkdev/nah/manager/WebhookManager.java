package cc.synkdev.nah.manager;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.objects.BINAuction;
import cc.synkdev.synkLibs.bukkit.Utils;
import cc.synkdev.nah.objects.DiscordWebhook;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class WebhookManager {
    private final static NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    private static File file = new File(core.getDataFolder(), "webhook.yml");
    private static Boolean status = false;
    private static String url = null;
    private static FileConfiguration config;
    private static Map<String, Boolean> alerts = new HashMap<>();
    public static void read() {
        if (file.exists()) {
            config = YamlConfiguration.loadConfiguration(file);
            status = config.getBoolean("enabled");
            url = config.getString("url");

            alerts.clear();
            alerts.put("new-listing", config.getBoolean("alerts.new-listing"));
            alerts.put("listing-bought", config.getBoolean("alerts.listing-bought"));
            alerts.put("listing-expired", config.getBoolean("alerts.listing-expired"));
            alerts.put("listing-edited", config.getBoolean("alerts.listing-edited"));
            alerts.put("ah-toggle", config.getBoolean("alerts.ah-toggle"));
        } else {
            try {
                Files.copy(core.getResource("webhook.yml"), file.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void sendWebhook(String key, BINAuction bA, String... args) {
        if (!status) return;
        if (url == null) {
            Utils.log(ChatColor.RED+"Couldn't send the webhook as the URL was null!");
            return;
        }


        if (!alerts.getOrDefault(key, false)) return;
        DiscordWebhook wh = new DiscordWebhook(url);
        wh.setUsername("NexusAuctionHouse");
        wh.setAvatarUrl("https://synkdev.cc/img/nah.png");
        String desc = Util.sanitizeDiscordMsg(Util.addPlaceholders(config.getString("webhook-descriptions."+key), args));
        DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject().setTitle(config.getString("webhook-titles."+key)).setDescription(desc);
        if (bA != null) {
            embed.addField("Price", bA.getPrice()+"", true);
            ZonedDateTime dateTime = Instant.ofEpochSecond(bA.getExpiry())
                    .atZone(ZoneId.systemDefault());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            embed.addField("Expiry", dateTime.format(formatter), true);
        }
        wh.addEmbed(embed);
        try {
            wh.execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

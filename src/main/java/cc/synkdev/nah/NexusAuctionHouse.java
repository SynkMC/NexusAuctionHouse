package cc.synkdev.nah;

import cc.synkdev.nah.commands.AhCommand;
import cc.synkdev.nah.manager.*;
import cc.synkdev.nah.objects.BINAuction;
import cc.synkdev.nah.objects.SortingTypes;
import cc.synkdev.synkLibs.bukkit.Lang;
import cc.synkdev.synkLibs.components.SynkPlugin;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.MessageKeys;
import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public final class NexusAuctionHouse extends JavaPlugin implements SynkPlugin, Listener {
    @Getter private static NexusAuctionHouse instance;
    File configFile = new File(this.getDataFolder(), "config.yml");
    File langFile = new File(this.getDataFolder(), "lang.json");
    FileConfiguration config;
    @Getter private int keepLogTime;
    @Getter private int expireTime;
    @Getter private int taxPercent;
    public FileConfiguration lang;
    public List<BINAuction> expiredBINs = new ArrayList<>();
    public Map<BINAuction, Integer> runningBINs = new HashMap<>();
    public List<BINAuction> sortPrice = new ArrayList<>();
    public List<BINAuction> sortPriceMax = new ArrayList<>();
    public List<BINAuction> sortExpiry = new ArrayList<>();
    public List<BINAuction> sortExpiryMax = new ArrayList<>();
    public Map<OfflinePlayer, SortingTypes> playerSortingTypes = new HashMap<>();
    public Map<OfflinePlayer, List<ItemStack>> retrieveMap = new HashMap<>();
    public List<SortingTypes> sortingTypes;
    @Getter private Economy econ = null;
    public Map<String, String> langMap = new HashMap<>();
    public List<Material> banned = new ArrayList<>();
    public List<String> missingDeps = new ArrayList<>();
    @Getter @Setter private Boolean toggle = true;

    @Override
    public void onEnable() {
        missingDeps.clear();
        if (!Bukkit.getPluginManager().isPluginEnabled("SynkLibs")) {
            missingDeps.add("SynkLibs");
        }
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            missingDeps.add("Vault");
        }
        if (!setupEconomy()) {
            missingDeps.add("an economy plugin");
        }

        if (!missingDeps.isEmpty()) {
            int index = 0;
            String s;
            if (missingDeps.size() == 1) {
                s = missingDeps.get(0);
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < missingDeps.size()-1; i++) {
                    sb.append(missingDeps.get(i)).append(", ");
                    index++;
                }
                sb.append(missingDeps.get(index+1));
                s = sb.toString();
            }
            Bukkit.getLogger().info("You are missing plugin dependancies! Please download the following: "+s);
            Bukkit.getPluginManager().registerEvents(this, this);
        } else {
            instance = this;

            new Metrics(this, 23102);

            updateConfig();
            loadConfig();

            reloadLang();

            DataFileManager.init();
            DataFileManager.load();
            DataFileManager.sort();

            BannedItemsManager.read();
            ToggleManager.read();
            WebhookManager.read();

            sortingTypes = new ArrayList<>(Arrays.asList(SortingTypes.PRICEMIN, SortingTypes.PRICEMAX, SortingTypes.LATESTPOSTED, SortingTypes.EXPIRESSOON));

            BukkitCommandManager bCM = new BukkitCommandManager(this);
            bCM.usePerIssuerLocale(false);
            bCM.getLocales().addMessage(bCM.getLocales().getDefaultLocale(), MessageKeys.PERMISSION_DENIED, Lang.translate("noPerm", this));
            bCM.getLocales().addMessage(bCM.getLocales().getDefaultLocale(), MessageKeys.NOT_ALLOWED_ON_CONSOLE, Lang.translate("playerOnly", this));
            bCM.getLocales().addMessage(bCM.getLocales().getDefaultLocale(), MessageKeys.UNKNOWN_COMMAND, Lang.translate("noCmd", this));

            bCM.registerCommand(new AhCommand());

            Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

            periodicSave.runTaskTimer(this, 6000L, 6000L);
            checkExpiry.runTaskTimer(this, 1200L, 1200L);

            //72000 ticks = 1 hour on 20 tps
            purgeLogs.runTaskTimer(this, 72000L, 72000L);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (event.getPlayer().isOp()) {
            if (!missingDeps.isEmpty()) {
                int index = 0;
                String s;
                if (missingDeps.size() == 1) {
                    s = missingDeps.get(0);
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < missingDeps.size()-1; i++) {
                        sb.append(missingDeps.get(i)).append(", ");
                        index++;
                    }
                    sb.append(missingDeps.get(index+1));
                    s = sb.toString();
                }
                event.getPlayer().sendMessage(ChatColor.RED+"[NexusAuctionHouse] You are missing plugin dependancies! Please download the following: "+s);
            }
        }
    }

    private final BukkitRunnable checkExpiry = new BukkitRunnable() {
        @Override
        public void run() {
            List<BINAuction> list = new ArrayList<>();
            runningBINs.forEach((binAuction, integer) -> {
                int time = Math.toIntExact(System.currentTimeMillis()/1000);
                if (time>=integer) {
                    if (binAuction.getSeller().isOnline()) binAuction.getSeller().getPlayer().sendMessage(prefix()+ChatColor.GOLD+Lang.translate("expired", getInstance()));
                    binAuction.setBuyable(false);
                    list.add(binAuction);
                }
            });
            for (BINAuction bA : list) {
                runningBINs.remove(bA);
                expiredBINs.add(bA);
                WebhookManager.sendWebhook("listing-expired", null, bA.getSeller().getName());
                if (retrieveMap.containsKey(bA.getSeller())) {
                    List<ItemStack> users = new ArrayList<>(retrieveMap.get(bA.getSeller()));
                    users.add(bA.getItem());
                    retrieveMap.replace(bA.getSeller(), users);
                } else {
                    retrieveMap.put(bA.getSeller(), new ArrayList<>(Collections.singletonList(bA.getItem())));
                }
            }
            DataFileManager.sort();
        }
    };

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private void updateConfig() {
        if (!this.getDataFolder().exists()) this.getDataFolder().mkdirs();
        try {
            if (!configFile.exists()) {
                try {
                    Files.copy(getResource("config.yml"), configFile.toPath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                File temp = new File(getDataFolder(), "temp-config-"+System.currentTimeMillis()+".yml");
                try {
                    Files.copy(getResource("config.yml"), temp.toPath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                FileConfiguration tempConfig = YamlConfiguration.loadConfiguration(temp);
                FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
                boolean changed = false;
                for (String key : tempConfig.getKeys(true)) {
                    if (!config.contains(key)) {
                        config.set(key, tempConfig.get(key));
                        changed = true;
                    }
                }

                if (changed) {
                    config.save(configFile);
                }

                temp.delete();
            }
            config = YamlConfiguration.loadConfiguration(configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void loadConfig() {
        reloadConfig();
        keepLogTime = getConfig().getInt("log-keep-time");
        expireTime = getConfig().getInt("expire-time");
        taxPercent = getConfig().getInt("tax-percent");
    }

    public void reloadLang() {
        langMap.clear();
        langMap.putAll(cc.synkdev.synkLibs.bukkit.Lang.init(this, langFile));
    }

    public void save() {
        long time = System.currentTimeMillis();
        DataFileManager.save();
        DataFileManager.sort();

        time = System.currentTimeMillis()-time;
        if (config.getBoolean("save-notif")) Util.staffBc(prefix()+ChatColor.GOLD+Lang.translate("dataSave", this, time+""));
    }

    BukkitRunnable periodicSave = new BukkitRunnable() {
        @Override
        public void run() {
            save();
        }
    };
    BukkitRunnable purgeLogs = new BukkitRunnable() {
        @Override
        public void run() {
            int time = Math.toIntExact(System.currentTimeMillis()/1000);
            List<BINAuction> list = new ArrayList<>();
            for (BINAuction bA : expiredBINs) {
                int bATime = bA.getExpiry() + keepLogTime;

                if (time >= bATime) list.add(bA);
            }
            expiredBINs.removeAll(list);
            DataFileManager.sort();
        }
    };

    @Override
    public void onDisable() {
        if (missingDeps.isEmpty()) DataFileManager.save();
    }

    @Override
    public String name() {
        return "NexusAuctionHouse";
    }

    @Override
    public String ver() {
        return "1.5.2";
    }

    @Override
    public String dlLink() {
        return "https://modrinth.com/plugin/nexusauctionhouse";
    }

    @Override
    public String prefix() {
        return Lang.translate("prefix", this);
    }

    @Override
    public String lang() {
        return "https://synkdev.cc/storage/translations/lang-pld/NexusAuctionHouse/lang-nah.json";
    }

    @Override
    public Map<String, String> langMap() {
        return langMap;
    }
}

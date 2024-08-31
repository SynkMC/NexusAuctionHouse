package cc.synkdev.nah;

import cc.synkdev.nah.commands.AhCommand;
import cc.synkdev.nah.components.BINAuction;
import cc.synkdev.nah.components.SortingTypes;
import cc.synkdev.nah.manager.DataFileManager;
import cc.synkdev.nah.manager.EventHandler;
import cc.synkdev.nah.manager.Lang;
import cc.synkdev.nah.manager.Util;
import cc.synkdev.synkLibs.bukkit.SynkLibs;
import cc.synkdev.synkLibs.bukkit.Utils;
import cc.synkdev.synkLibs.components.SynkPlugin;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class NexusAuctionHouse extends JavaPlugin implements SynkPlugin {
    @Getter private static NexusAuctionHouse instance;
    File configFile = new File(this.getDataFolder(), "config.yml");
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

    @Override
    public void onEnable() {
        instance = this;

        new Metrics(this, 23102);

        SynkLibs.setSpl(this);
        Utils.checkUpdate(this, this);

        dlConfig();
        loadConfig();

        new Lang().init();

        DataFileManager.init();
        DataFileManager.load();
        DataFileManager.sort();

        sortingTypes = new ArrayList<>(Arrays.asList(SortingTypes.PRICEMIN, SortingTypes.PRICEMAX, SortingTypes.LATESTPOSTED, SortingTypes.EXPIRESSOON));

        getCommand("ah").setExecutor(new AhCommand());
        getCommand("ah").setTabCompleter(new AhCommand());

        Bukkit.getPluginManager().registerEvents(new EventHandler(), this);

        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        periodicSave.runTaskTimer(this, 6000L, 6000L);
        checkExpiry.runTaskTimer(this, 1200L, 1200L);

        //72000 ticks = 1 hour on 20 tps
        purgeLogs.runTaskTimer(this, 72000L, 72000L);
    }

    private BukkitRunnable checkExpiry = new BukkitRunnable() {
        @Override
        public void run() {
            List<BINAuction> list = new ArrayList<>();
            runningBINs.forEach((binAuction, integer) -> {
                int time = Math.toIntExact(System.currentTimeMillis()/1000);
                if (time>=integer) {
                    if (binAuction.getSeller().isOnline()) binAuction.getSeller().getPlayer().sendMessage(prefix()+ChatColor.GOLD+Lang.translate("expired"));
                    binAuction.setBuyable(false);
                    list.add(binAuction);
                }
            });
            for (BINAuction bA : list) {
                runningBINs.remove(bA);
                expiredBINs.add(bA);
                if (retrieveMap.containsKey(bA.getSeller())) {
                    List<ItemStack> users = new ArrayList<>(retrieveMap.get(bA.getSeller()));
                    users.add(bA.getItem());
                    retrieveMap.replace(bA.getSeller(), users);
                } else {
                    retrieveMap.put(bA.getSeller(), new ArrayList<>(Arrays.asList(bA.getItem())));
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

    private void dlConfig() {
        if (!this.getDataFolder().exists()) this.getDataFolder().mkdirs();
        try {
            if (!configFile.exists()) configFile.createNewFile();
            config = Utils.loadWebConfig("https://synkdev.cc/storage/config-nah.php", configFile);
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

    public void save() {
        long time = System.currentTimeMillis();
        DataFileManager.save();
        DataFileManager.sort();

        time = System.currentTimeMillis()-time;
        Util.staffBc(prefix()+ChatColor.GOLD+Lang.translate("dataSave", time+""));
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
        DataFileManager.save();
    }

    @Override
    public String name() {
        return "NexusAuctionHouse";
    }

    @Override
    public String ver() {
        return "1.0.1";
    }

    @Override
    public String dlLink() {
        return "https://modrinth.com/plugin/nexusauctionhouse";
    }

    @Override
    public String prefix() {
        return ChatColor.translateAlternateColorCodes('&', "&8[&6AuctionHouse&8] » &r");
    }
}

package cc.synkdev.nah.gui.expiry;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.api.NAHUtil;
import cc.synkdev.nah.gui.ManageMenu;
import cc.synkdev.nah.objects.BINAuction;
import cc.synkdev.synkLibs.bukkit.Lang;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ExpiryCalendarGuis {
    NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    Player p;
    BINAuction bA;
    int year;
    int month;
    int day;
    int hour;
    int min;
    public ExpiryCalendarGuis(Player p, BINAuction bA) {
        this.p = p;
        this.bA = bA;
        yearGui().open(p);
    }
    public Gui yearGui() {
        Gui gui = Gui.gui()
                .disableAllInteractions()
                .title(Component.text(Lang.translate("expiryCalendarYear", core)))
                .rows(1)
                .create();

        for (int i = 0; i < 9; i++) {
            int year = Calendar.getInstance().get(Calendar.YEAR)+i-4;
            gui.setItem(1, (i+1), ItemBuilder.from(Material.RED_TERRACOTTA)
                    .name(Component.text(ChatColor.YELLOW+""+year))
                    .asGuiItem(event -> {
                        this.year = year;
                        monthGui().open(p);
                    }));
        }
        return gui;
    }

    public Gui monthGui() {
        Gui gui = Gui.gui()
                .disableAllInteractions()
                .title(Component.text(Lang.translate("expiryCalendarMonth", core)))
                .rows(4)
                .create();

        Map<Integer, Integer> slotsMap = new java.util.HashMap<>(Map.of(
                3, Calendar.JANUARY,
                4, Calendar.FEBRUARY,
                5, Calendar.MARCH,
                12, Calendar.APRIL,
                13, Calendar.MAY,
                14, Calendar.JUNE,
                21, Calendar.JULY,
                22, Calendar.AUGUST,
                23, Calendar.SEPTEMBER,
                30, Calendar.OCTOBER));
        slotsMap.putAll(Map.of(
                31, Calendar.NOVEMBER,
                32, Calendar.DECEMBER));

        for (Map.Entry<Integer, Integer> entry : slotsMap.entrySet()) {
            gui.setItem(entry.getKey(), ItemBuilder.from(Material.ORANGE_TERRACOTTA)
                    .name(Component.text(ChatColor.YELLOW+""+(entry.getValue()+1)))
                    .asGuiItem(event -> {
                        this.month = entry.getValue()+1;
                        dayGui().open(p);
                    }));
        }
        return gui;
    }

    public Gui dayGui() {
        Gui gui = Gui.gui()
                .disableAllInteractions()
                .title(Component.text(Lang.translate("expiryCalendarDay", core)))
                .rows(4)
                .create();

        int days = 30;
        switch (month) {
            case 1,3,5,7,8,10,12 -> days = 31;
            case 2 -> days = LocalDate.parse(year+"-"+month+"-"+"01").isLeapYear() ? 29 : 28;
        }
        for (int i = 0; i < days; i++) {
            int day = i+1;
            gui.setItem(i, ItemBuilder.from(Material.YELLOW_TERRACOTTA)
                    .name(Component.text(ChatColor.YELLOW+""+(i+1)))
                    .asGuiItem(event -> {
                        this.day = day;
                        hourGui().open(p);
                    }));
        }

        return gui;
    }

    public Gui hourGui() {
        Gui gui = Gui.gui()
                .disableAllInteractions()
                .title(Component.text(Lang.translate("expiryCalendarHour", core)))
                .rows(3)
                .create();

        gui.setItem(2, 5, ItemBuilder.from(Material.PAPER)
                .name(Component.text(ChatColor.YELLOW+Lang.translate("expiryHourInfo", core)))
                .asGuiItem());

        Map<Integer, Integer> amMap = new HashMap<>(Map.of(
                1, 0,
                2, 1,
                3, 2,
                4, 3,
                5, 9,
                6, 10,
                7, 11,
                8, 12,
                9, 18,
                10, 19
        ));
        amMap.putAll(Map.of(
                11, 20,
                12, 21
        ));

        Map<Integer, Integer> pmMap = new HashMap<>(Map.of(
                1, 5,
                2, 6,
                3, 7,
                4, 8,
                5, 14,
                6, 15,
                7, 16,
                8, 17,
                9, 23,
                10, 24
        ));
        pmMap.putAll(Map.of(
                11, 25,
                12, 26
        ));

        for (Map.Entry<Integer, Integer> entry : amMap.entrySet()) {
            gui.setItem(entry.getValue(), ItemBuilder.from(Material.LIME_TERRACOTTA)
                    .name(Component.text(ChatColor.YELLOW+""+entry.getKey()))
                    .asGuiItem(event -> {
                        this.hour = entry.getKey();
                        tenthMinuteGui().open(p);
                    }));
        }

        for (Map.Entry<Integer, Integer> entry : pmMap.entrySet()) {
            gui.setItem(entry.getValue(), ItemBuilder.from(Material.LIME_TERRACOTTA)
                    .name(Component.text(ChatColor.YELLOW+""+entry.getKey()))
                    .asGuiItem(event -> {
                        this.hour = entry.getKey()+12;
                        tenthMinuteGui().open(p);
                    }));
        }
        return gui;
    };

    public Gui tenthMinuteGui() {
        Gui gui = Gui.gui()
                .disableAllInteractions()
                .title(Component.text(Lang.translate("expiryCalendarMinute", core)))
                .rows(1)
                .create();

        for (int i = 0; i < 6; i++) {
            int minute = i;
            gui.setItem((i > 2 ? i+2 : i+1), ItemBuilder.from(Material.GREEN_TERRACOTTA)
                    .name(Component.text(ChatColor.YELLOW+""+minute+"X"))
                    .asGuiItem(event -> {
                        this.min = minute*10;
                        minuteGui().open(p);
                    }));
        }
        return gui;
    }

    public Gui minuteGui() {
        Gui gui = Gui.gui()
                .disableAllInteractions()
                .title(Component.text(Lang.translate("expiryCalendarMinute", core)))
                .rows(2)
                .create();
        
        Map<Integer, Integer> map = new HashMap<>(Map.of(0, 2,
                1, 3,
                2, 4,
                3, 5,
                4, 6,
                5, 11,
                6, 12,
                7, 13,
                8, 14,
                9, 15));

            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                int minute = entry.getKey();
                gui.setItem(entry.getValue(), ItemBuilder.from(Material.GREEN_TERRACOTTA)
                        .name(Component.text(ChatColor.YELLOW+""+(this.min/10)+""+minute))
                        .asGuiItem(event -> {
                            this.min = this.min+minute;
                            LocalDateTime ldt = LocalDateTime.of(year, month, day, hour, min);
                            NAHUtil.setExpiry(this.bA, Math.toIntExact(ldt.toEpochSecond(ZoneOffset.UTC)), p.getDisplayName());
                            new ManageMenu().gui(this.bA).open(p);
                        }));
            }
        return gui;
    }
}

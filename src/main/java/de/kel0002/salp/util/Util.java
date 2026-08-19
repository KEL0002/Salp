package de.kel0002.salp.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.attribute.FileTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.regex.Pattern;

public class Util {
    public static Component mn(String input) {return MiniMessage.miniMessage().deserialize(input);}

    public static String formatTime(FileTime time) {
        ZonedDateTime zdt = time.toInstant().atZone(ZoneId.systemDefault());
        String d = String.valueOf(zdt.getDayOfMonth()); if (d.length() == 1) d = "0" + d;
        String m = String.valueOf(zdt.getMonthValue()); if (m.length() == 1) m = "0" + m;
        String y = String.valueOf(zdt.getYear()); y = y.substring(y.length() - 2);

        String h = String.valueOf(zdt.getHour()); if (h.length() == 1) h = "0" + h;
        String min = String.valueOf(zdt.getMinute()); if (min.length() == 1) min = "0" + min;
        return d + "." + m + "." + y + " " + h + ":" + min;
    }

    public static String formatBytes(long bytes) {
        if (bytes < 0) return "/";
        if (bytes < 1024) return bytes + "B";
        else if (bytes < Math.pow(1024, 2)) return (bytes/1024 >= 100 ? String.valueOf(Math.round((double) bytes/1024)) : String.format("%.1f", (double) bytes/1024)) + "KiB";
        else if (bytes < Math.pow(1024, 3)) return (bytes/Math.pow(1024, 2) >= 100 ? String.valueOf(Math.round(bytes/Math.pow(1024, 2))) : String.format("%.1f", bytes/Math.pow(1024, 2))) + "MiB";
        else if (bytes < Math.pow(1024, 4)) return (bytes/Math.pow(1024, 3) >= 100 ? String.valueOf(Math.round(bytes/Math.pow(1024, 3))) : String.format("%.1f", bytes/Math.pow(1024, 3))) + "GiB";
        else return bytes/Math.pow(1024, 4) + "TiB";
    }

    public static String formatInt(int number) {
        if (number < 0) return "/";
        if (number < 1000) return String.valueOf(number);
        if (number < 1000000) return (number/1000 >= 100) ? Math.round((double) number / 1000) + "k" : String.format("%.1f", (double) number / 1000) + "k";
        return (number/1000000 >= 100) ? Math.round((double) number / 1000000) + "m" : String.format("%.1f", (double) number / 1000000) + "m";
    }


    public static String getDisplayName(File file, Player player) {
        String displayname = file.getName();
        String search = PlayerAttributeManager.getSearch(player);
        if (!search.isEmpty()) {
            Pattern pattern = Pattern.compile(Pattern.quote(search), Pattern.CASE_INSENSITIVE);
            displayname = pattern.matcher(displayname).replaceAll("<color:aqua>$0</color>");
        }

        String displaycolor = file.isDirectory() ? "<color:#fcc932>" : "";
        String displayicon = file.isDirectory() ? "<sprite:items:item/bundle>" : "<sprite:items:item/filled_map>";

        if (FileUtil.getPath(file).equals(PlayerAttributeManager.getClipboard(player))) {
            displaycolor = PlayerAttributeManager.cut(player) ? "<color:red>" : "<color:green>"; }

        return displayicon + " " + displaycolor + displayname;
    }


    public static String getLoader() {
        String version = Bukkit.getName().toLowerCase();
        return switch (version) {
            case "paper", "spigot", "bukkit", "folia", "purpur", "sponge" -> version;
            default -> "paper";
        };
    }
    public static String getMCVersion() {
        return Bukkit.getVersion().split("-")[0];
    }
}

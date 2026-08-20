package de.kel0002.salp.util;

import de.kel0002.salp.Main;
import de.kel0002.salp.tempSaving.PlayerAttributes;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlayerAttributeManager {
    static PlayerAttributes playerAttributes = Main.getPlayerAttributes();

    public static String getSearch(Player player) {
        Object search = playerAttributes.get(player, "search");
        return search == null ? "" : search.toString();
    }

    @NotNull
    public static String getSort(Player player) {
        Object sort = playerAttributes.get(player, "sort");
        return sort == null ? "alphabetically_ff" : sort.toString();
    }

    @NotNull
    public static String getProjectSort(Player player) {
        Object sort = playerAttributes.get(player, "project_sort");
        return sort == null ? "relevance" : sort.toString();
    }

    public static String getSizeSortDirection(Player player) {
        Object sizeSortDirection = playerAttributes.get(player, "sort_size_direction");
        return sizeSortDirection == null ? "largest_first" : sizeSortDirection.toString();
    }

    public static String getCreatedSortDirection(Player player) {
        Object createdSortDirection = playerAttributes.get(player, "sort_created_direction");
        return createdSortDirection == null ? "oldest_first" : createdSortDirection.toString();
    }

    public static String getMessage(Player player) {
        return (String) playerAttributes.get(player, "message");
    }


    public static String getClipboard(Player player) {
        Object clipboard = playerAttributes.get(player, "clipboard");
        return clipboard == null ? null : clipboard.toString();
    }

    public static boolean cut(Player player) {
        return Boolean.TRUE.equals(playerAttributes.get(player, "cut"));
    }

    public static boolean update_messages(Player player) {
        return !Boolean.FALSE.equals(playerAttributes.get(player, "update_messages"));
    }
    public static int getWidth(Player player) {
        Object value = playerAttributes.get(player, "screen_width");
        return (value instanceof Integer number) ? Math.clamp(number, 64, 1080) : 1080;
    }

    public static void setMessage(Player player, String message) {
        playerAttributes.set(player, "message", message);
    }

    public static void resetSearch(Player player) {playerAttributes.set(player, "search", null);}
    public static void resetMessage(Player player) {playerAttributes.set(player, "message", null);}

    public static List<String> playerSettings() {
        return List.of(
                "update_messages",
                "screen_width"
        );
    }

    public static List<String> requiresBool() {
        return List.of(
                "update_messages");
    }

    public static List<String> requiresInt() {
        return List.of(
                "screen_width"
        );
    }
}

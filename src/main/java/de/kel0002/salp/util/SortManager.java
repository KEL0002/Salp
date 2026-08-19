package de.kel0002.salp.util;

import de.kel0002.salp.Main;
import de.kel0002.salp.tempSaving.PlayerAttributes;
import org.bukkit.entity.Player;

import static de.kel0002.salp.util.PlayerAttributeManager.*;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class SortManager {
    static PlayerAttributes playerAttributes = Main.getPlayerAttributes();
    public static void nextSort(Player player) {
        switch (getSort(player)){
            case "alphabetically_ff" -> playerAttributes.set(player, "sort", "alphabetically");
            case "alphabetically" -> playerAttributes.set(player, "sort", "size_ff");
            case "size_ff" -> playerAttributes.set(player, "sort", "size");
            case "size" -> playerAttributes.set(player, "sort", "created_ff");
            case "created_ff" -> playerAttributes.set(player, "sort", "created");
            case "created" -> playerAttributes.set(player, "sort", "alphabetically_ff");
        }
    }

    public static void nextProjectSort(Player player) {
        switch (getProjectSort(player)) {
            case "relevance" -> playerAttributes.set(player, "project_sort", "downloads");
            case "downloads" -> playerAttributes.set(player, "project_sort", "follows");
            case "follows" -> playerAttributes.set(player, "project_sort", "newest");
            case "newest" -> playerAttributes.set(player, "project_sort", "updated");
            case "updated" -> playerAttributes.set(player, "project_sort", "relevance");
        }
    }

    public static void nextSizeSortDirection(Player player) {
        switch (getSizeSortDirection(player)) {
            case "largest_first" -> Main.getPlayerAttributes().set(player, "sort_size_direction", "smallest_first");
            case "smallest_first" -> Main.getPlayerAttributes().set(player, "sort_size_direction", "largest_first");
        }
    }

    public static void nextCreatedSortDirection(Player player) {
        switch (getCreatedSortDirection(player)){
            case "oldest_first" -> Main.getPlayerAttributes().set(player,"sort_created_direction", "newest_first");
            case "newest_first" -> Main.getPlayerAttributes().set(player,"sort_created_direction", "oldest_first");
        }
    }







    public static File[] sort(File[] files, Player player) {
        String sort = getSort(player);
        
        files = switch (sort) {
            case "alphabetically", "alphabetically_ff"-> Arrays.stream(files).sorted(Comparator
                    .comparing(f -> f.getName().toLowerCase())).toArray(File[]::new);
            case "size", "size_ff" -> Arrays.stream(files).sorted(Comparator
                    .comparing(FileUtil::getSize)).toArray(File[]::new);
            case "created", "created_ff" -> Arrays.stream(files).sorted(Comparator
                    .comparing(FileUtil::getCreationTime)).toArray(File[]::new);
            default -> files;
        };
        
        if (sort.startsWith("size") && "largest_first".equals(getSizeSortDirection(player))) files = reverseOrder(files);
        if (sort.startsWith("created") && "newest_first".equals(getCreatedSortDirection(player))) files = reverseOrder(files);
        if (sort.endsWith("_ff")) files = sortFoldersFirst(files);
        return files;
    }




    private static File[] sortFoldersFirst(File[] files) {
        return Arrays.stream(files).sorted(Comparator
                .comparing((File f) -> !f.isDirectory())).toArray(File[]::new);
    }

    private static File[] reverseOrder(File[] files) {
        File[] reversed = files.clone();
        Collections.reverse(Arrays.asList(reversed));
        return reversed;
    }

}

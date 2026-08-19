package de.kel0002.salp.dialogs.main;

import de.kel0002.salp.Main;
import de.kel0002.salp.advancedStuff.Downloader;
import de.kel0002.salp.dialogs.KDialog;
import de.kel0002.salp.dialogs.fileactions.CreateDialog;
import de.kel0002.salp.dialogs.fileactions.UploadDialog;
import de.kel0002.salp.tempSaving.ModrinthIndexer;
import de.kel0002.salp.tempSaving.PlayerAttributes;
import de.kel0002.salp.dialogs.fileactions.DeleteDialog;
import de.kel0002.salp.dialogs.fileactions.RenameDialog;
import de.kel0002.salp.util.PlayerAttributeManager;
import de.kel0002.salp.util.SortManager;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import static de.kel0002.salp.util.DialogUtil.*;
import static de.kel0002.salp.util.Util.*;
import static de.kel0002.salp.util.FileUtil.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class FileListDialog extends KDialog {
    Player player;
    String path;
    String sort;
    String search;

    boolean additionalPluginsColumn;
    boolean additionalSortColumn;

    PlayerAttributes playerAttributes = Main.getPlayerAttributes();

    List<File> updating_now = new ArrayList<>();


    public FileListDialog(String path, Player player) {
        init(path, player);
    }

    @Override
    public void init(String path, Player player) {
        this.player = player;
        this.path = path;

        sort = PlayerAttributeManager.getSort(player);
        search = PlayerAttributeManager.getSearch(player);

        additionalSortColumn = !sort.contains("alphabetically");
        additionalPluginsColumn = "/plugins/".equals(path);
        Bukkit.getAsyncScheduler().runNow(Main.getInstance(), _ -> openDialog());
    }

    public void openTxtDialog(String path) {
        Bukkit.getAsyncScheduler().runDelayed(Main.getInstance(), _ -> new TxtEditorDialog(path, player, this), 50L, TimeUnit.MILLISECONDS);
    }


    public void openDialog() {
        List<DialogInput> inputs = new ArrayList<>();
        List<ActionButton> buttons = new ArrayList<>();

        // ==============
        // === Search ===
        // ==============

        inputs.add(DialogInput.text("search", width(player, 80), mn("Search"),
                false, search, Integer.MAX_VALUE, null));

        // ==========================
        // === NAVIGATION BUTTONS ===
        // ==========================

        // INFO/SORT Button
        buttons.add(getInfoButton());

        // Size Button
        if (sort.contains("size")) buttons.add(getSizeButton());
        // Created Button
        if (sort.contains("created")) buttons.add(getCreatedButton());

        // Modrinth Browser
        if (additionalPluginsColumn) buttons.add(ActionButton.builder(mn("<sprite:gui:icon/link>"))
                .tooltip(mn("Browse Modrinth"))
                .action(getDialogAction(() -> {
                    PlayerAttributeManager.resetMessage(player);
                    new ModrinthDialog(player, this);}))
                .width(width(player, 4)).build());


        // Upload Button
        buttons.add(ActionButton.builder(mn("<sprite:items:item/ender_pearl>"))
                        .action(getDialogAction(() -> {
                            PlayerAttributeManager.resetMessage(player);
                            new UploadDialog(path, player, this);}))
                .tooltip(mn("Upload to server"))
                .width(width(player, 4)).build());

        // Search Button
        buttons.add(ActionButton.builder(mn("<sprite:gui:icon/search>"))
                .tooltip(mn("Search (Text entered above)"
                    + "<newline><color:gray>Clear search when text entered is already searched"))
                .action(getDialogAction((view, _) -> {
                    PlayerAttributeManager.resetMessage(player);
                    if (search.equals(view.getText("search")))
                        playerAttributes.set(player, "search", "");
                    else
                        playerAttributes.set(player, "search", view.getText("search"));
                    reOpen();
                }))
                .width(width(player, 4)).build());

        // PASTE BUTTON
        buttons.add(getPasteButton());

        // CREATE BUTTON
        buttons.add(ActionButton.builder(mn("+"))
                .action(getDialogAction(()-> {
                    PlayerAttributeManager.resetMessage(player);
                    new CreateDialog(path, player, this);
                }))
                .tooltip(mn("Create"))
                .width(width(player,4)).build());

        // BACK BUTTON
        buttons.add(ActionButton.builder(mn("<-"))
                .tooltip(mn("Back"))
                .action(!"/".equals(path) ?
                        getDialogAction(() ->
                        {PlayerAttributeManager.resetMessage(player);init(getParentPath(path), player);})
                        : (search.isEmpty() ?
                            getDialogAction(() -> {
                                player.closeDialog();
                                PlayerAttributeManager.resetMessage(player);
                                PlayerAttributeManager.resetSearch(player);
                            })
                            : getDialogAction(() -> {
                                PlayerAttributeManager.resetMessage(player);
                                PlayerAttributeManager.resetSearch(player);
                                reOpen();
                })))
                .width(width(player,4)).build());

        // ============================
        // === Create List of Files ===
        // ============================

        File[] files = getFolderContents(path);

        files = SortManager.sort(files, player);

        files = performSearch(files);

        // ====================
        // === FILE BUTTONS ===
        // ====================

        for (File file : files) {
            // NAME BUTTON
            String displayname = getDisplayName(file, player);


            buttons.add(ActionButton.builder(mn(displayname))
                    .tooltip(mn("<bold>File Info:</bold><newline>"
                            + "<bold>Size:</bold> " + formatBytes(getSize(file)) + "<newline>"
                            + "<bold>Created:</bold> " + formatTime(getCreationTime(file)) + "<newline>"
                            + "<bold>Modified:</bold> " + formatTime(getModificationTime(file)) + "<newline>"
                            + "<bold>Path:</bold> " + getPath(file)
                    ))
                    .action(getDialogAction((view, _) -> {
                        playerAttributes.set(player, "search", view.getText("search"));
                        PlayerAttributeManager.resetMessage(player);
                        if (file.isDirectory()) init(getPath(file), player);
                        else {new TxtEditorDialog(getPath(file), player, this);}
                    }))
                    .width(getMainButtonWidth()).build()
            );

            // SIZE BUTTON
            if (sort.startsWith("size")) {
                buttons.add(ActionButton.builder(mn(formatBytes(getSize(file))))
                        .tooltip(mn(getSize(file) + "B"))
                        .width(width(player, 10))
                        .build());
            }
            // CREATED BUTTON
            if (sort.startsWith("created")) {
                buttons.add(ActionButton.builder(mn(formatTime(getCreationTime(file))))
                        .width(width(player, 10))
                        .build());
            }

            // UPDATE BUTTON
            if (additionalPluginsColumn) buttons.add(getUpdateButton(file));

            // DOWNLOAD BUTTON //TODO
            buttons.add(ActionButton.builder(mn("<sprite:items:item/hopper>"))
                    .tooltip(mn("Downloading coming soon™"))
                    .width(width(player, 4)).build());

            // CUT BUTTON
            buttons.add(ActionButton.builder(mn("<sprite:items:item/shears>"))
                    .tooltip(mn("Cut"))
                    .action(getDialogAction(() -> {
                        PlayerAttributeManager.resetMessage(player);
                        playerAttributes.set(player, "clipboard", getPath(file));
                        playerAttributes.set(player, "cut", true);
                        reOpen();
                    }))
                    .width(width(player,4)).build());

            // COPY BUTTON
            buttons.add(ActionButton.builder(mn("<sprite:items:item/book>"))
                    .tooltip(mn("Copy"))
                    .action(getDialogAction(() -> {
                        PlayerAttributeManager.resetMessage(player);
                        playerAttributes.set(player, "clipboard", getPath(file));
                        playerAttributes.set(player, "cut", false);
                        reOpen();
                    }))
                    .width(width(player,4)).build());

            // RENAME BUTTON
            buttons.add(ActionButton.builder(mn("<sprite:items:item/ink_sac>"))
                    .tooltip(mn("Rename"))
                    .action(getDialogAction(() -> {
                        PlayerAttributeManager.resetMessage(player);
                        new RenameDialog(getPath(file), player, this);
                    }))
                    .width(width(player,4)).build());

            // DELETE BUTTON
            buttons.add(ActionButton.builder(mn("<sprite:items:item/lava_bucket>"))
                    .tooltip(mn("Delete"))
                    .action(getDialogAction(() -> {
                        PlayerAttributeManager.resetMessage(player);
                        new DeleteDialog(getPath(file), player, this);
                    }))
                    .width(width(player,4)).build());
        }

        // ==========================
        // === BUILD FINAL DIALOG ===
        // ==========================

        String message = PlayerAttributeManager.getMessage(player);

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(mn("Filemanager: " + path))
                        .body(message == null ? List.of() : List.of(DialogBody.plainMessage(mn(message))))
                        .inputs(inputs)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .pause(false)
                        .build())
                .type(DialogType.multiAction(
                        buttons,
                        null,
                        additionalPluginsColumn ? (additionalSortColumn ? 8 : 7) : (additionalSortColumn) ? 7 : 6)
                ));

        player.showDialog(dialog);
    }

    private ActionButton getInfoButton() {
        int width = getMainButtonWidth();
        return ActionButton.builder(mn("Listing Files in " + path))
                .tooltip(mn("<bold>Sort:</bold><newline>" +
                        ("alphabetically_ff".equals(sort) ? "<bold>" : "") + "-> Alphabetically (<sprite:items:item/bundle>↑)<reset><newline>" +
                        ("alphabetically".equals(sort) ? "<bold>" : "") + "-> Alphabetically<reset><newline>" +
                        ("size_ff".equals(sort) ? "<bold>" : "") + "-> Size (<sprite:items:item/bundle>↑)<reset><newline>" +
                        ("size".equals(sort) ? "<bold>" : "") + "-> Size<reset><newline>" +
                        ("created_ff".equals(sort) ? "<bold>" : "") + "-> Created (<sprite:items:item/bundle>↑)<reset><newline>" +
                        ("created".equals(sort) ? "<bold>" : "") + "-> Created<reset>"
                ))
                .action(getDialogAction(() -> {
                    SortManager.nextSort(player);
                    reOpen();
                }))
                .width(width).build();
    }

    private int getMainButtonWidth() {
        int width = 80;
        if (additionalSortColumn) width -= 10;
        if (additionalPluginsColumn) width -= 4;

        return width(player, width);
    }

    private ActionButton getSizeButton() {
        String sort_direction = PlayerAttributeManager.getSizeSortDirection(player);
        return ActionButton.builder(mn("Size"))
                .tooltip(mn("<bold>Sort Direction:</bold><newline>" +
                        ("largest_first".equals(sort_direction) ? "<bold>" : "") + "-> Largest First<reset><newline>" +
                        ("smallest_first".equals(sort_direction) ? "<bold>" : "") + "-> Smallest First"
                ))
                .action(getDialogAction(() -> {
                    SortManager.nextSizeSortDirection(player);
                    reOpen();
                }))
                .width(width(player, 10))
                .build();
    }

    private ActionButton getCreatedButton() {
        String sort_direction = PlayerAttributeManager.getCreatedSortDirection(player);
        return ActionButton.builder(mn("Created"))
                .tooltip(mn("Sort Direction:<newline>" +
                        ("oldest_first".equals(sort_direction) ? "<bold>" : "") + "-> Oldest First<reset><newline>" +
                        ("newest_first".equals(sort_direction) ? "<bold>" : "") + "-> Newest First"
                ))
                .action(getDialogAction(() -> {
                    SortManager.nextCreatedSortDirection(player);
                    reOpen();
                }))
                .width(width(player, 10))
                .build();
    }

    private ActionButton getPasteButton() {
        return ActionButton.builder(mn("<sprite:items:item/writable_book>"))
                .tooltip(mn("Paste"))
                .action(getDialogAction(() -> {

                    String clipboard = PlayerAttributeManager.getClipboard(player);

                    if (clipboard == null) {
                        PlayerAttributeManager.setMessage(player, "<color:red>Cannot paste: Your clipboard is empty");
                        reOpen();return;}
                    try {
                        if (!getFile(clipboard).exists()) {
                            PlayerAttributeManager.setMessage(player, "<color:red>Cannot paste: The original file was moved or deleted");
                            reOpen();return;}

                        if (PlayerAttributeManager.cut(player)) {
                            Files.move(getFile(clipboard).toPath(), getFile(path).toPath().resolve(getNameOnly(clipboard)));
                            playerAttributes.set(player, "clipboard", getPath(getFile(path).toPath().resolve(getNameOnly(clipboard)).toFile()));
                            playerAttributes.set(player, "cut", false);

                            PlayerAttributeManager.setMessage(player, "<color:green>File moved!");

                        } else {
                            Files.copy(getFile(clipboard).toPath(), getFile(path).toPath().resolve(getNameOnly(clipboard)));

                            PlayerAttributeManager.setMessage(player, "<color:green>File copied!");
                        }

                        reOpen();
                    } catch (IOException e) {
                        PlayerAttributeManager.setMessage(player, "<color:red>Cannot paste: " + e.getMessage());
                        reOpen();}

                }))
                .width(width(player,4))
                .build();
    }

    private ActionButton getUpdateButton(File file) {
        ModrinthIndexer mrIndexer = Main.getModrinthIndexer();
        Map<String, String> mrInfo = mrIndexer.getInfo(file);

        if (mrInfo != null) {

            if (mrIndexer.hasUpdate(file)) { // Update available
                if (updating_now.contains(file)) { // Updating now
                    return ActionButton.builder(mn("<color:yellow>\uD83D\uDD01"))
                            .tooltip(mn("<sprite:gui:pending_invite/accept> <bold>Updating...</bold><newline>"
                                    + "<bold>Name:</bold> " + mrInfo.get("name") + "<newline>"
                                    + "<bold>Version:</bold> " + mrInfo.get("current_version") + "<newline>"
                                    + "<bold>Published:</bold> " + formatTime(FileTime.from(Instant.parse(mrInfo.get("current_date_published"))))
                                    + "<newline><italic>The page will not refresh automatically"
                            ))
                            .width(width(player, 4)).build();
                } else {
                    return ActionButton.builder(mn("\uD83D\uDD01"))
                            .tooltip(mn("<bold>\uD83D\uDD01 Update Available!</bold><newline>"
                                    + "<bold>Name:</bold> " + mrInfo.get("name") + "<newline>"
                                    + "<bold><sprite:blocks:block/command_block_side> Version:</bold> " + mrInfo.get("current_version") + "<newline>"
                                    + "<bold>\uD83D\uDD01 Version:</bold> " + mrInfo.get("newest_version") + "<newline>"
                                    + "<bold><sprite:blocks:block/command_block_side> Published:</bold> " + formatTime(FileTime.from(Instant.parse(mrInfo.get("current_date_published")))) + "<newline>"
                                    + "<bold>\uD83D\uDD01 Published:</bold> " + formatTime(FileTime.from(Instant.parse(mrInfo.get("newest_date_published"))))))
                            .width(width(player, 4))
                            .action(getDialogAction(() -> {
                                PlayerAttributeManager.setMessage(player, "<color:green><hover:show_text:'The page will not refresh automatically'>Updating '" + mrInfo.get("name") + "' \uD83D\uDEC8");
                                new Downloader(path, player, mrInfo.get("newest_link"), () -> {
                                    String url = mrInfo.get("name");
                                    PlayerAttributeManager.setMessage(player, "<color:green>Updated '" + url + "'");
                                    String outputName;
                                    try {
                                        outputName = Path.of(new URI(url).getPath()).getFileName().toString(); //Found no better way
                                    } catch (URISyntaxException e) {
                                        outputName = url.substring(url.lastIndexOf("/") + 1);
                                    }
                                    if (!(getPath(file).equals(path + outputName))) delete(getPath(file));
                                });
                                updating_now.add(file);
                                reOpen();
                            })).build();
                }
            } else { // Latest Version
                return ActionButton.builder(mn("<sprite:gui:pending_invite/accept>"))
                        .tooltip(mn("<sprite:gui:pending_invite/accept> <bold>Latest Version</bold><newline>"
                                + "<bold>Name:</bold> " + mrInfo.get("name") + "<newline>"
                                + "<bold>Version:</bold> " + mrInfo.get("current_version") + "<newline>"
                                + "<bold>Published:</bold> " + formatTime(FileTime.from(Instant.parse(mrInfo.get("current_date_published"))))
                        ))
                        .width(width(player, 4)).build();
            }
        } else {
            if (mrIndexer.notIndexed(file) && file.getName().endsWith(".jar")) mrIndexer.index(file);
            if (mrIndexer.isIndexing(file) ||
                    (mrIndexer.notIndexed(file) && file.getName().endsWith(".jar"))) { // Indexing
                return ActionButton.builder(mn("<color:yellow>⚠"))
                        .tooltip(mn("⚠ <bold>Indexing...</bold><newline>"
                                + "Salp is currently indexing this file.<newline>"
                                + "Reopen this menu in a bit to check if any updates are available"
                        ))
                        .width(width(player, 4)).build();
            } else { // Not found
                if (file.getName().endsWith(".jar")) {
                    return ActionButton.builder(mn("<color:dark_red>⚠"))
                            .tooltip(mn( "⚠ <bold>Plugin not found</bold><newline>"
                                    + "Salp was unable to find this plugin on Modrinth.<newline>"
                                    + "If you believe this is an error, try redownloading it.<newline>"
                                    + "Plugins which are not listed as compatible with your server version will not be found"
                            ))
                            .width(width(player, 4)).build();
                } else { // Not a plugin
                    return ActionButton.builder(mn(""))
                            .width(width(player, 4))
                            .build();
                }
            }
        }
    }


    private File[] performSearch(File[] files) {
        return Arrays.stream(files)
                .filter(f -> f.getName().toLowerCase().contains(search.toLowerCase()) || containsString(f, search))
                .sorted(Comparator.comparingInt(file -> {
                    if (file.getName().toLowerCase().startsWith(search.toLowerCase())) return 0;
                    if (file.getName().toLowerCase().contains(search.toLowerCase())) return 1;
                    return 2;
                }))
                .toArray(File[]::new);
    }
    private static boolean containsString(File f, String string) {
        try {
            return Files.readString(f.toPath()).contains(string);
        } catch (IOException _) {return false;}
    }




    public void reOpen() {
        init(path, player);
    }
}

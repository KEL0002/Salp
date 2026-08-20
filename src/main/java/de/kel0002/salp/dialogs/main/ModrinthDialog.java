package de.kel0002.salp.dialogs.main;

import de.kel0002.salp.Main;
import de.kel0002.salp.advancedStuff.Downloader;
import de.kel0002.salp.tempSaving.ModrinthIndexer;
import de.kel0002.salp.tempSaving.ModrinthPages;
import de.kel0002.salp.tempSaving.ModrinthProject;
import de.kel0002.salp.tempSaving.PlayerAttributes;
import de.kel0002.salp.util.SortManager;
import de.kel0002.salp.util.PlayerAttributeManager;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.kel0002.salp.util.DialogUtil.*;
import static de.kel0002.salp.util.Util.*;
import static de.kel0002.salp.util.Util.formatTime;

public class ModrinthDialog {
    Player player;
    FileListDialog superDialog;
    String sort;
    int page = 0; // Page is always one below page shown
    String search;
    ModrinthPages modrinthPages = Main.getModrinthPages();

    List<String> downloadingNow = new ArrayList<>();
    PlayerAttributes playerAttributes = Main.getPlayerAttributes();

    public ModrinthDialog(Player player, FileListDialog superDialog) {
        init(player, superDialog);
    }

    public void init(Player player, FileListDialog superDialog) {

        this.player = player;
        this.superDialog = superDialog;

        sort = PlayerAttributeManager.getProjectSort(player);
        search = PlayerAttributeManager.getSearch(player);


        Bukkit.getAsyncScheduler().runNow(Main.getInstance(), _ -> openDialog());
    }

    public void openDialog() {
        List<DialogInput> inputs = new ArrayList<>();
        List<ActionButton> buttons = new ArrayList<>();

        List<ModrinthProject> projects = modrinthPages.getPage(page, search, sort);
        if (projects == null) {
            PlayerAttributeManager.setMessage(player, "<color:red>Failed to load Plugins!");
            projects = new ArrayList<>();}

        // ==============
        // === Search ===
        // ==============

        inputs.add(DialogInput.text("search", width(player, 80), mn("Search"),
                false, search, Integer.MAX_VALUE, null));

        // ==========================
        // === NAVIGATION BUTTONS ===
        // ==========================
        // Info Button
        buttons.add(getInfoButton());

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
                    page = 0;
                    reOpen();
                }))
                .width(width(player, 4)).build());

        // Back
        buttons.add(ActionButton.builder(mn("←"))
                .tooltip(mn(getPageInfo()))
                .action(getDialogAction(() -> {
                    PlayerAttributeManager.resetMessage(player);
                    if (page == 0) return;
                    page -= 1;
                    reOpen();
                }))
                .width(width(player, 3)).build());

        // Next Page
        buttons.add(ActionButton.builder(mn("→"))
                .tooltip(mn(getPageInfo()))
                .action(getDialogAction(() -> {
                    PlayerAttributeManager.resetMessage(player);
                    List<ModrinthProject> pagelist = modrinthPages.getPage((page+1), search, sort);
                    if (pagelist == null || pagelist.isEmpty()) return;
                    page += 1;
                    reOpen();
                }))
                .width(width(player, 3)).build());

        // Close Dialog
        buttons.add(ActionButton.builder(mn("<sprite:gui:pending_invite/reject>"))
                .tooltip(mn("Close Modrinth-Browser"))
                        .action(getDialogAction(() -> {
                            PlayerAttributeManager.resetMessage(player);
                            superDialog.reOpen();
                        }))
                .width(width(player, 4)).build());

        // ===========================
        // === Add Project Buttons ===
        // ===========================


        for (ModrinthProject project : projects) {
            buttons.add(ActionButton.builder(mn(project.getName()))
                    .tooltip(mn("<bold>" + project.getName()
                            + "<newline><reset>" + project.getDescription()
                    ))
                    .action(getDialogAction((_, _) -> {}))
                    .width(width(player, 86)).build()
            );

            // Downloads
            buttons.add(ActionButton.builder(mn("<sprite:gui:hud/heart/full>"))
                    .tooltip(mn( "<bold>Downloads:<reset> " + formatInt(project.getDownloads())
                            + "<newline><bold>Follows:<reset> " + formatInt(project.getLikes())))
                    .width(width(player, 4)).build());

            // Author
            buttons.add(ActionButton.builder(mn("<head:" + project.getAuthor().replaceAll("[^A-Za-z0-9_]", "").replaceFirst("^(.{16}).*$", "$1") + ">"))
                    .tooltip(mn("<bold>Author:<reset> " + project.getAuthor()))
                    .width(width(player, 3)).build());


            // Updated
            buttons.add(ActionButton.builder(mn("\uD83D\uDD01"))
                    .tooltip(mn("<bold>Updated:<reset> " + formatTime(project.getUpdated())
                        + "<newline><bold>Created:<reset> " + formatTime(project.getCreated())))
                    .width(width(player, 3)).build());
            // Download
            String downloadIcon;
            String downloadTooltip;
            if (Main.getModrinthIndexer().isInstalled(project.getId())) {
                downloadIcon = "<sprite:gui:pending_invite/accept>";
                downloadTooltip = "Already installed";
            } else if (downloadingNow.contains(project.getId())) {
                downloadIcon = "<sprite:items:item/hopper_minecart>";
                downloadTooltip = "Downloading...<newline><italic>The page does not refresh automatically";
            } else {
                downloadIcon = "<sprite:items:item/hopper>";
                downloadTooltip = "Download";
            }
            buttons.add(ActionButton.builder(mn(downloadIcon))
                            .tooltip(mn(downloadTooltip))
                            .action(getDialogAction(() -> {
                                PlayerAttributeManager.resetMessage(player);
                                if (Main.getModrinthIndexer().isInstalled(project.getId()) || downloadingNow.contains(project.getId()))
                                    return;

                                downloadingNow.add(project.getId());

                                reOpen();

                                Bukkit.getAsyncScheduler().runNow(Main.getInstance(), _ -> {
                                    Map<String, String> newestInfo = ModrinthIndexer.getNewest(project.getId());
                                    if (newestInfo == null) {
                                        PlayerAttributeManager.setMessage(player, "<color:red>Unable to get the latest version of this project");
                                        reOpen();
                                        return;
                                    }

                                    new Downloader(superDialog.path, player, newestInfo.get("newest_link"), () -> {
                                    });

                                });
                            }))
                    .width(width(player, 4)).build());
        }

        // ==========================
        // === BUILD FINAL DIALOG ===
        // ==========================

        String message = PlayerAttributeManager.getMessage(player);

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(mn(getBranding() +  "Browsing Modrinth | " + getPageInfo()))
                        .body(message == null ? List.of() : List.of(DialogBody.plainMessage(mn(message))))
                        .inputs(inputs)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .pause(false)
                        .build())
                .type(DialogType.multiAction(
                        buttons,
                        null,
                        5
                )));

        player.showDialog(dialog);

        // Preload next Page
        modrinthPages.getPage(page+1, search, sort);
    }

    private ActionButton getInfoButton() {
        return ActionButton.builder(mn("<gradient:#1396f9:#7639f9>" + getPageInfo()))
                .tooltip(mn("<bold>Sort:</bold><newline>" +
                        ("relevance".equals(sort) ? "<bold>" : "") + "-> Relevance<reset><newline>" +
                        ("downloads".equals(sort) ? "<bold>" : "") + "-> Downloads<reset><newline>" +
                        ("follows".equals(sort) ? "<bold>" : "") + "-> Follows<reset><newline>" +
                        ("newest".equals(sort) ? "<bold>" : "") + "-> Newest<reset><newline>" +
                        ("updated".equals(sort) ? "<bold>" : "") + "-> Updated<reset>"
                ))
                .action(getDialogAction(() -> {
                    SortManager.nextProjectSort(player);
                    page = 0;
                    reOpen();
                }))
                .width(width(player,86)).build();
    }

    private String getPageInfo() {
        return "Page " + (page + 1) + "/" + modrinthPages.getTotalPages(search, sort);
    }

    public void reOpen() {
        init(player, superDialog);
    }
}

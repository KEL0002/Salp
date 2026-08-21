package de.kel0002.salp.dialogs.main;

import de.kel0002.salp.Main;
import de.kel0002.salp.dialogs.KDialog;
import de.kel0002.salp.dialogs.confirmation.CloseDialog;
import de.kel0002.salp.dialogs.confirmation.ReloadDialog;
import de.kel0002.salp.util.MethodResult;
import de.kel0002.salp.util.PlayerAttributeManager;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static de.kel0002.salp.util.DialogUtil.*;
import static de.kel0002.salp.util.FileUtil.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static de.kel0002.salp.util.Util.mn;

public class TxtEditorDialog extends KDialog{
    KDialog superDialog;
    Player player;
    String path;
    String saved_text;
    String current_text;
    String search;

    public TxtEditorDialog(String path, Player player, KDialog superDialog) {
        this.superDialog = superDialog;
        init(path, player);
    }

    @Override
    public void init(String path, Player player) {
        this.player = player;
        this.path = path;

        search = PlayerAttributeManager.getSearch(player);

        Bukkit.getAsyncScheduler().runNow(Main.getInstance(), _ -> openDialog());
    }

    public void openDialog() {
        List<DialogInput> inputs = new ArrayList<>();
        List<ActionButton> buttons = new ArrayList<>();
        List<DialogBody> body = new ArrayList<>();


        String message = PlayerAttributeManager.getMessage(player);
        // Try to add TextBox
        try {
            saved_text = saved_text == null ? Files.readString(getFile(path).toPath()) : saved_text;
            if (current_text == null) current_text = saved_text;

            // SIZE CHECK
            if ((search.isEmpty() ? current_text : current_text.replace(search, "§c" + search + "§r")).getBytes(StandardCharsets.UTF_8).length > 65535) {
                PlayerAttributeManager.setMessage(player, "<color:red>Failed to open '" + getNameOnly(path) + "': " + "File is to large to be opened in game");
                superDialog.init(getParentPath(path), player);
                return;
            }


            int boxHeight = 49;
            if (!search.isEmpty() && current_text.contains(search)) boxHeight -= 3;
            if (message != null) boxHeight -= 1;

            if ((!search.isEmpty() && current_text.contains(search)) && message != null) boxHeight -= 2;

            inputs.add(DialogInput.text("content", width(player, 100), mn("File Content"), false,
                    search.isEmpty() ? current_text : current_text.replace(search, "§c" + search + "§r"),
                    Integer.MAX_VALUE, TextDialogInput.MultilineOptions.create(Integer.MAX_VALUE,
                            width(player, (boxHeight)))));

        } catch (IOException e) {
            String error = e.getMessage();
            PlayerAttributeManager.setMessage(player, "<color:red>Failed to open '" + getNameOnly(path) + "': " + error);
            superDialog.init(getParentPath(path), player);
            return;
        }

        // ==============================
        // === ADD NAVIGATION BUTTONS ===
        // ==============================

        // Save Button
        buttons.add(ActionButton.builder(mn("Save"))
                .action(getDialogAction((view, _) -> {
                    String content = view.getText("content");
                    if (content == null) return;
                    current_text = content.replace("§c" + search + "§r", search);
                    if (current_text.equals(saved_text)) {
                        PlayerAttributeManager.setMessage(player, "<color:yellow>No changes to save");
                        init(path, player);
                        return;
                    }

                    MethodResult result = save(path, current_text);
                    if (result.failed()) {
                        PlayerAttributeManager.setMessage(player, "<color:red>Unable to save File: " + result.error() + " - If you did a lot of progress and cannot save, you can copy the text to your clipboard and then try to edit the file another way");
                        init(path, player);}
                    else {
                        PlayerAttributeManager.setMessage(player, "<color:green>Saved '" + getNameOnly(path) + "'");
                        saved_text = current_text;
                        init(path, player);
                    }
                })).build());

        // Reload button
        buttons.add(ActionButton.builder(mn("Reload"))
                        .action(getDialogAction((view, _) -> {
                            String content = view.getText("content");
                            if (content == null) return;
                            current_text = content.replace("§c" + search + "§r", search);
                            PlayerAttributeManager.resetMessage(player);
                            if (current_text.equals(saved_text)) {
                                resetSaved();
                                PlayerAttributeManager.resetSearch(player);
                                init(path, player);
                            } else {
                                PlayerAttributeManager.resetSearch(player);
                                new ReloadDialog(path, player, this);
                            }
                        }))
                .build());

        // CLOSE BUTTON
        buttons.add(ActionButton.builder(mn("Close"))
                .action(getDialogAction((view, _) ->
                {
                    PlayerAttributeManager.resetMessage(player);
                    String content = view.getText("content");
                    if (content == null) return;
                    current_text = content.replace("§c" + search + "§r", search);
                    if (current_text.equals(saved_text)) {
                        superDialog.init(getParentPath(path), player);
                    } else {
                        new CloseDialog(path, player, this, superDialog);
                    }
                })).build());


        // CREATE MESSAGES

        if (!(message == null)) body.add(DialogBody.plainMessage(mn(message)));
        if (!"".equals(search) && current_text.contains(search)) body.add(DialogBody.plainMessage(
                mn("Search results highlighted. <bold>Don't edit highlighted content.</bold><newline>Reload the file first. \uD83D\uDEC8")
                        .hoverEvent(HoverEvent.showText(mn("At both right and left of the search are 2 invisible characters. " +
                        "If you modify the highlighted text, the characters will be saved to the file. Reload to remove them, or delete them manually.")))));

        // ==========================
        // === BUILD FINAL DIALOG ===
        // ==========================

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(mn(getBranding() + path))
                        .body(body)
                        .inputs(inputs)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .pause(false)
                        .build())
                .type(DialogType.multiAction(
                        buttons,
                        getSaveAndExitButton(),
                        3
                )));

        player.showDialog(dialog);
    }

    public void resetSaved() {
        current_text = null;
        saved_text = null;
    }


    private ActionButton getSaveAndExitButton() {
        return ActionButton.builder(mn("Save & Quit"))
                .action(getDialogAction((view, _) -> {
                    String content = view.getText("content");
                    if (content == null) return;
                    current_text = content.replace("§c" + search + "§r", search);
                    if (current_text.equals(saved_text)) {
                        superDialog.init(getParentPath(path), player);
                        return;
                    }

                    MethodResult result = save(path, current_text);
                    if (result.failed()) {
                        PlayerAttributeManager.setMessage(player, "<color:red>Unable to save File: " + result.error() + " - If you did a lot of progress and cannot save, you can copy the text to your clipboard and then try to edit the file another way");
                        init(path, player);}
                    else {
                        PlayerAttributeManager.setMessage(player, "<color:green>Saved '" + getNameOnly(path) + "'");
                        saved_text = current_text;
                        superDialog.init(getParentPath(path), player);
                    }

                }))
                .width(width(player, 60)).build();


    }

    public void reOpen() {
        init(path, player);
    }
}

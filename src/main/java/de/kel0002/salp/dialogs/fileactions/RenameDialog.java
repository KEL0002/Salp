package de.kel0002.salp.dialogs.fileactions;

import de.kel0002.salp.dialogs.KDialog;
import de.kel0002.salp.util.MethodResult;
import de.kel0002.salp.util.PlayerAttributeManager;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;

import static de.kel0002.salp.util.DialogUtil.*;
import static de.kel0002.salp.util.FileUtil.*;
import static de.kel0002.salp.util.Util.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;



public class RenameDialog {
    KDialog superDialog;
    Player player;
    String path;
    String current_input;

    public RenameDialog(String path, Player player, KDialog superDialog) {
        this.superDialog = superDialog;

        current_input = getNameOnly(path);
        init(path, player);
    }

    public void init(String path, Player player) {
        this.player = player;
        this.path = path;

        openDialog();
    }

    public void openDialog() {
        List<DialogInput> inputs = new ArrayList<>();

        inputs.add(DialogInput.text("name", width(player, 80), mn("New Filename"),
                true, current_input, 255, null));

        // ==========================
        // === BUILD FINAL DIALOG ===
        // ==========================

        String message = PlayerAttributeManager.getMessage(player);

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(mn("Filemanager: Rename " + path))
                        .body(message == null ? List.of() : List.of(DialogBody.plainMessage(mn(message))))
                        .inputs(inputs)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .pause(false)
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(mn("Rename"))
                                .action(getRenameAction())
                                .width(width(player, 40)).build(),
                        ActionButton.builder(mn("Cancel"))
                                .action(getDialogAction(() -> superDialog.init(getParentPath(path), player)))
                                .width(width(player, 40)).build()
                )));

        player.showDialog(dialog);
    }

    private DialogAction getRenameAction() {
        return DialogAction.customClick(
                (view, _) -> {
                    String name = view.getText("name");

                    if (getNameOnly(path).equals(name)) {
                        superDialog.init(getParentPath(path), player);
                        return;
                    }

                    for (File file : getFolderContents(getParentPath(path))) {
                        if (Objects.equals(name, getNameOnly(getPath(file)))) {
                            current_input = name;
                            PlayerAttributeManager.setMessage(player, "<color:red>Renaming failed:" + "The File already exists");
                            reOpen();
                            return;
                        }
                    }


                    MethodResult result = rename(path, name);
                    if (result.failed()) {
                        current_input = name;
                        PlayerAttributeManager.setMessage(player, "<color:red>Renaming failed: " + result.error());
                        reOpen();
                    } else {
                        PlayerAttributeManager.setMessage(player, "<color:green>Renamed '" + getNameOnly(path) +
                                "' to '" + name + "'");
                        superDialog.init(getParentPath(path), player);
                    }

                }, ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build()
        );
    }


    public void reOpen() {
        init(path, player);
    }
}

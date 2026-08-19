package de.kel0002.salp.dialogs.confirmation;

import de.kel0002.salp.dialogs.main.TxtEditorDialog;
import de.kel0002.salp.util.PlayerAttributeManager;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import org.bukkit.entity.Player;

import static de.kel0002.salp.util.DialogUtil.*;
import static de.kel0002.salp.util.FileUtil.*;
import static de.kel0002.salp.util.Util.*;

import java.util.ArrayList;
import java.util.List;


public class ReloadDialog {
    Player player;
    String path;
    TxtEditorDialog superDialog;

    public ReloadDialog(String path, Player player, TxtEditorDialog superDialog) {
        this.superDialog = superDialog;

        init(path, player);
    }

    public void init(String path, Player player) {
        this.player = player;
        this.path = path;

        openDialog();
    }

    public void openDialog() {
        List<DialogBody> body = new ArrayList<>();

        String message = PlayerAttributeManager.getMessage(player);
        if (message != null) body.add(DialogBody.plainMessage(mn(message)));

        body.add(DialogBody.plainMessage(mn("<color:red>Are you sure you want to reload '" + getNameOnly(path) + "'?")));
        body.add(DialogBody.plainMessage(mn("All of your progress will be lost!")));

        // ==========================
        // === BUILD FINAL DIALOG ===
        // ==========================

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(mn("Filemanager: Reload " + path))
                        .body(body)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .pause(false)
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(mn("Reload"))
                                .action(getDialogAction(() -> {
                                    superDialog.resetSaved();
                                    superDialog.reOpen();
                                }))
                                .width(width(player, 40)).build(),
                        ActionButton.builder(mn("Cancel"))
                                .action(getDialogAction(() -> superDialog.reOpen()))
                                .width(width(player, 40)).build()
                )));

        player.showDialog(dialog);
    }

    public void reOpen() {
        init(path, player);
    }
}

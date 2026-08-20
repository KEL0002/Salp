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

import java.util.ArrayList;
import java.util.List;

public class CreateDialog {
    Player player;
    String path;
    KDialog superDialog;
    String current_input = "";
    boolean isDir = false;

    public CreateDialog(String path, Player player, KDialog superDialog) {
        this.superDialog = superDialog;

        init(path, player);
    }

    public void init(String path, Player player) {
        this.player = player;
        this.path = path;

        openDialog();
    }

    public void openDialog() {
        List<DialogInput> inputs = new ArrayList<>();

        inputs.add(DialogInput.text("name", width(player, 80), mn("Name (including file extension)"),
                true, current_input, 255, null));
        inputs.add(DialogInput.bool("isdir",mn("Is Folder"), isDir, "You will create a directory", "You will create a file"));

        // ==========================
        // === BUILD FINAL DIALOG ===
        // ==========================

        String message = PlayerAttributeManager.getMessage(player);

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(mn(getBranding() + "Create file at " + path))
                        .body(message == null ? List.of() : List.of(DialogBody.plainMessage(mn(message))))
                        .inputs(inputs)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .pause(false)
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(mn("Create"))
                                .action(getCreateAction())
                                .width(width(player, 40)).build(),
                        ActionButton.builder(mn("Cancel"))
                                .action(getDialogAction(() -> superDialog.init(path, player)))
                                .width(width(player, 40)).build()
                )));

        player.showDialog(dialog);
    }

    public void reOpen() {
        init(path, player);
    }

    private DialogAction getCreateAction() {
        return DialogAction.customClick((view, _) -> {
            String name = view.getText("name");
            boolean isdir = Boolean.TRUE.equals(view.getBoolean("isdir"));

            MethodResult result = create(path + name, isdir);
            if (result.failed()) {
                isDir = isdir;
                current_input = name;
                PlayerAttributeManager.setMessage(player, "<color:red>Creation failed: " + result.error());
                reOpen();
            } else {
                PlayerAttributeManager.setMessage(player, "<color:green>Created '" + name + "'");
                superDialog.init(path, player);
            }
        }, ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build());
    }
}

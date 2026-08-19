package de.kel0002.salp.dialogs.fileactions;

import de.kel0002.salp.Main;
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



public class DeleteDialog {
    KDialog superDialog;
    Player player;
    String path;
    String required_confirmation;
    boolean confirmation_required;
    String currentConfirmation;

    public DeleteDialog(String path, Player player, KDialog superDialog) {
        this.superDialog = superDialog;
        init(path, player);
    }


    public void init(String path, Player player) {
        this.player = player;
        this.path = path;

        required_confirmation = "DELETE " + getNameOnly(path);
        confirmation_required = isUsedDir(path);

        openDialog();
    }

    public void openDialog() {
        List<DialogInput> inputs = new ArrayList<>();
        List<DialogBody> body = new ArrayList<>();


        String message = PlayerAttributeManager.getMessage(player);
        if (message != null) body.add(DialogBody.plainMessage(mn(message)));

        body.add(DialogBody.plainMessage(mn("<color:red>Are you sure you want to delete '" + getNameOnly(path) + "'?")));
        body.add(DialogBody.plainMessage((mn("You will not be able to restore it later"))));


        // Confirmation String
        if (confirmation_required) {
            inputs.add(DialogInput.text("confirmation", width(player, 80),
                    mn("Type '" + required_confirmation + "' and press 'Delete' to confirm this action"),
            true, currentConfirmation == null ? "" : currentConfirmation, 262, null));
        }
        // ==========================
        // === BUILD FINAL DIALOG ===
        // ==========================


        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(mn("Filemanager: Delete " + path))
                        .body(body)
                        .inputs(inputs)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .pause(false)
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(mn("Delete"))
                                .action(getDeleteAction())
                                .width(width(player, 40)).build(),
                        ActionButton.builder(mn("Cancel"))
                                .action(getDialogAction(() -> superDialog.init(getParentPath(path), player)))
                                .width(width(player, 40)).build()
                )));

        player.showDialog(dialog);
    }

    public void reOpen() {
        init(path, player);
    }

    private DialogAction getDeleteAction() {
        return DialogAction.customClick(
                (view, _) -> {
                    String confirmationInput = view.getText("confirmation");

                    if (confirmation_required && !required_confirmation.equals(confirmationInput)) {
                        currentConfirmation = confirmationInput;
                        PlayerAttributeManager.setMessage(player, "<color:red>You did not enter the confirmation correctly");
                        reOpen();
                        return;
                    }

                    MethodResult result = delete(path);

                    if (result.failed()) {
                        PlayerAttributeManager.setMessage(player, "<color:red>Deletion failed: " + result.error());
                        currentConfirmation = confirmationInput;
                        reOpen();
                    } else {
                        Main.getModrinthIndexer().remove(getFile(path));

                        PlayerAttributeManager.setMessage(player, "<color:green>Deleted '" + getNameOnly(path) + "'");
                        superDialog.init(getParentPath(path), player);
                    }
                }, ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build()
        );
    }
}

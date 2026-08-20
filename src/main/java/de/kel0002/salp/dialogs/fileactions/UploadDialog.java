package de.kel0002.salp.dialogs.fileactions;

import de.kel0002.salp.advancedStuff.Downloader;
import de.kel0002.salp.dialogs.KDialog;
import de.kel0002.salp.util.PlayerAttributeManager;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static de.kel0002.salp.util.DialogUtil.*;
import static de.kel0002.salp.util.FileUtil.*;
import static de.kel0002.salp.util.Util.*;

public class UploadDialog {
    KDialog superDialog;
    Player player;
    String path;
    String current_input;


    public UploadDialog(String path, Player player, KDialog superDialog){
        this.superDialog = superDialog;

        current_input = "";
        init(path, player);
    }

    public void init(String path, Player player) {
        this.player = player;
        this.path = path;

        openDialog();
    }

    public void openDialog() {
        List<DialogInput> inputs = new ArrayList<>();
        List<DialogBody> body = new ArrayList<>();

        String message = PlayerAttributeManager.getMessage(player);
        if (message != null) body.add(DialogBody.plainMessage(mn(message)));

        body.add(DialogBody.plainMessage(mn("Please enter the url you want to upload from below.")));
        body.add(DialogBody.plainMessage(mn("Use direct download links. \uD83D\uDEC8")
                .hoverEvent(HoverEvent.showText(mn("In a normal Browser, you can obtain the direct link by right-clicking on the download button and pressing 'Copy Link'"
                        + "<newline><sprite:gui:pending_invite/reject>https://modrinth.com/plugin/smpe"
                        + "<newline><sprite:gui:pending_invite/accept>https://cdn.modrinth.com/data/QbF7LLz8/versions/pT5TQYjT/SMPE-2.0-3.jar")))));

        inputs.add(DialogInput.text("link", width(player, 80), mn("URL"),
                true, current_input, 255, null));

        // ==========================
        // === BUILD FINAL DIALOG ===
        // ==========================



        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(mn(getBranding() + "Upload to " + path))
                        .body(body)
                        .inputs(inputs)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .pause(false)
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(mn("Upload"))
                                .action(getDownloadAction())
                                .width(width(player, 40)).build(),
                        ActionButton.builder(mn("Cancel"))
                                .action(getDialogAction(() -> superDialog.init(path, player)))
                                .width(width(player, 40)).build()
                )));

        player.showDialog(dialog);
    }

    private DialogAction getDownloadAction() {
        return DialogAction.customClick(
                (view, _) -> {
                    String url = view.getText("link");

                    if (url == null || url.isEmpty()) {
                        PlayerAttributeManager.setMessage(player, "<color:red>Uploading failed: " + "You did not enter any link to upload from");
                        reOpen();
                        return;
                    }

                    String outputName;
                    try {outputName = Path.of(new URI(url).getPath()).getFileName().toString();
                    } catch (URISyntaxException e) {outputName = url.substring(url.lastIndexOf("/") + 1);}


                    for (File file : getFolderContents(path)) {
                        if (Objects.equals(outputName, getNameOnly(getPath(file)))) {
                            current_input = url;
                            PlayerAttributeManager.setMessage(player, "<color:red>Uploading failed: " + "The file already exists");
                            reOpen();
                            return;
                        }
                    }


                    new Downloader(path, player, url, ()->{});


                    PlayerAttributeManager.setMessage(player, "<hover:show_text:'The page will not refresh automatically'><color:green>Uploading '" + outputName + "' \uD83D\uDEC8");
                    superDialog.init(path, player);

                }, ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build()
        );
    }


    public void reOpen() {
        init(path, player);
    }
}

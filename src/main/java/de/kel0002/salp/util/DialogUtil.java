package de.kel0002.salp.util;

import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

public class DialogUtil {
    public static int width(Player player, int screenPercent){
        return (int) ((PlayerAttributeManager.getWidth(player)*0.8)*((double) screenPercent/100));
    }

    public static DialogAction getDialogAction(BiConsumer<DialogResponseView, Audience> code) {
        return DialogAction.customClick(code::accept, ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build());
    }
    public static DialogAction getDialogAction(Runnable code){
        return getDialogAction((view, audience) -> code.run());
    }
}

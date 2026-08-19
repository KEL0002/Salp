package de.kel0002.salp.playerInteraction;

import de.kel0002.salp.Main;
import de.kel0002.salp.util.FileUtil;
import de.kel0002.salp.util.PlayerAttributeManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static de.kel0002.salp.util.Util.*;

public class Command implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players are able to use this plugin");
            return false;
        }

        PlayerAttributeManager.resetSearch(player);
        PlayerAttributeManager.resetMessage(player);

        if (args.length == 0) {
            FileUtil.open("", player);
            if (!(Main.getPlayerAttributes().get(player, "screen_width") instanceof Integer)) {
                sendScalingInfo(player);
                Main.getPlayerAttributes().set(player, "screen_width", 1080);
            }
        } else if (args.length == 1) {
            if (args[0].startsWith("/")) {FileUtil.open(args[0], player);
                if (!(Main.getPlayerAttributes().get(player, "screen_width") instanceof Integer)) {sendScalingInfo(player);
                Main.getPlayerAttributes().set(player, "screen_width", 1080);
            }}
            else if (args[0].equals("help")) player.sendMessage(mn("<bold>Salp Overview"
                    + "<newline><reset><yellow>/salp help<reset> <dark_gray>- <gray>Show this message"
                    + "<newline><reset><yellow>/salp<reset> <dark_gray>- <gray>Open the root directory of the server"
                    + "<newline><reset><yellow>/salp /path/to/file<reset> <dark_gray>- <gray>Directly open a specific file or directory"
                    + "<newline><reset><yellow>/salp $someSetting someValue <dark_gray>- <gray>Configure your settings. This is not required to use the plugin, but allows for some customisation"
                    ));
            else if (args[0].startsWith("$")) {
                String setting = args[0].substring(1);
                if (!(PlayerAttributeManager.playerSettings().contains(setting))) {
                    player.sendMessage(mn("<color:red>This is not a valid setting"));
                } else {
                    player.sendMessage(mn("<color:yellow>" + "'" + setting + "' is currently set to '" + Main.getPlayerAttributes().get(player, setting) + "'"));
                }
            }
        } else if (args.length == 2) {
            if (args[0].startsWith("$")) {
                Object toSet = args[1];

                String setting = args[0].substring(1);

                if (!PlayerAttributeManager.playerSettings().contains(setting)) {
                    player.sendMessage(mn("<color:red>This is not a valid setting"));
                    return false;
                }

                if (PlayerAttributeManager.requiresBool().contains(setting)) {
                    if ("true".equals(toSet)) toSet = true;
                    else if ("false".equals(toSet)) toSet = false;
                    else {
                        player.sendMessage(mn("<color:red>This setting requires either true or false"));
                        return false;
                    }
                }

                if (PlayerAttributeManager.requiresInt().contains(setting)) {
                    try {toSet = Integer.parseInt(toSet.toString());
                    } catch (NumberFormatException e) {
                        player.sendMessage(mn("<color:red>This setting requires an integer"));
                        return false;
                    }
                }

                Main.getPlayerAttributes().set(player, setting, toSet);
                player.sendMessage(mn("<color:green>" + "Set '" + setting + "' to " + toSet));
            } else {
                FileUtil.open("", player);
            }
        } else {
            FileUtil.open("", player);
        }
        return true;
    }

    public void sendScalingInfo(Player player) {
        player.sendMessage(mn("<color:#04c4d6>Does the menu not fit onto your screen?" +
                "<newline>Use <color:#0469d6>/salp $screen_width VALUE </color>to scale correctly." +
                "<newline>The value should be between 600 and 1080. Smaller value -> smaller menu"));
    }
}

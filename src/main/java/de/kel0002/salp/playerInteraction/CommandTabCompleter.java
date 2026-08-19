package de.kel0002.salp.playerInteraction;

import de.kel0002.salp.util.PlayerAttributeManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static de.kel0002.salp.util.FileUtil.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CommandTabCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> suggestions = new ArrayList<>();

        if (!(sender instanceof Player)) return List.of();

        if (args.length == 1 && "help".startsWith(args[0])) {
            suggestions.add("help");
        }
        if (args.length == 1 && args[0].isEmpty()) {
            suggestions.add("/");
            suggestions.add("$");

        } else if (args.length == 1 && args[0].startsWith("/")) {
            File[] files = (args[0].endsWith("/")) ? getFolderContents(args[0]) : getFolderContents(getParentPath(args[0]));
            for (File file : files) {
                if (!getPath(file).startsWith(args[0])) continue;
                suggestions.add(getPath(file));
            }
        } else if (args.length == 1 && args[0].startsWith("$")) {
            suggestions.addAll(PlayerAttributeManager.playerSettings().stream().map(s -> "$" + s).toList());
        }

        return suggestions;
    }
}

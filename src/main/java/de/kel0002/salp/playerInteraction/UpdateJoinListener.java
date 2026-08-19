package de.kel0002.salp.playerInteraction;

import de.kel0002.salp.Main;
import de.kel0002.salp.tempSaving.ModrinthIndexer;
import de.kel0002.salp.util.PlayerAttributeManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


import static de.kel0002.salp.util.FileUtil.*;
import static de.kel0002.salp.util.Util.*;

import java.io.File;

public class UpdateJoinListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (event.getPlayer().hasPermission("salp.access") && PlayerAttributeManager.update_messages(event.getPlayer())) {
            Bukkit.getAsyncScheduler().runNow(Main.getInstance(), _ -> {
               try {
                   int updates = 0;
                   ModrinthIndexer mrIndexer = Main.getModrinthIndexer();
                   for (File file : getFolderContents("/plugins/")) {
                       if (mrIndexer.hasUpdate(file)) {updates += 1;}
                   }
                   if (updates == 0) return;
                   boolean multiple = updates != 1;
                   event.getPlayer().sendMessage(mn("There " + (multiple ? "are <color:yellow>" : "is <color:yellow>") + updates +
                           "</color> plugin update" + (multiple ? "s" : "") + " available!<click:run_command:'/files /plugins/'> <color:dark_gray>[<color:#80dffc>SHOW<color:dark_gray>]</click>"));
               } catch (Exception _){}
            });
        }
    }
}

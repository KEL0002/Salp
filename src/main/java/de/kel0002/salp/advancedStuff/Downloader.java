package de.kel0002.salp.advancedStuff;

import de.kel0002.salp.Main;
import de.kel0002.salp.tempSaving.ModrinthIndexer;
import de.kel0002.salp.util.NetworkStuff;
import de.kel0002.salp.util.PlayerAttributeManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.kel0002.salp.util.FileUtil.*;

public class Downloader {

    String targetPath;
    Player player;
    String url;
    Runnable afterAction;

    String outputName;
    ModrinthIndexer mrIndexer = Main.getModrinthIndexer();

    public Downloader(String targetPath, Player player, String url, Runnable afterAction) {
        this.targetPath = targetPath;
        this.player = player;
        this.url = url;
        this.afterAction = afterAction;

        try {this.outputName = Path.of(new URI(url).getPath()).getFileName().toString();
        } catch (URISyntaxException e) {this.outputName = url.substring(url.lastIndexOf("/") + 1);}

        download();
    }


    public void download() {
        Bukkit.getAsyncScheduler().runNow(Main.getInstance(), _ -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
                conn.setRequestProperty("User-Agent", NetworkStuff.getUserAgent());
                conn.connect();

                try (InputStream in = conn.getInputStream();
                     OutputStream out = Files.newOutputStream(getFile(targetPath).toPath().resolve(outputName))) {

                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                }

                // DOWNLOAD DONE:

                if (targetPath.equals("/plugins/")) mrIndexer.index(getFile(targetPath).toPath().resolve(this.outputName).toFile());

                afterAction.run();

            } catch (Exception e) {
                PlayerAttributeManager.setMessage(player,"<color:red>Download of '" + outputName + "' failed: " + e.getMessage());
            }
        });
    }
}

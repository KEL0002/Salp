package de.kel0002.salp;

import de.kel0002.salp.advancedStuff.Metrics;
import de.kel0002.salp.playerInteraction.Command;
import de.kel0002.salp.playerInteraction.CommandTabCompleter;
import de.kel0002.salp.playerInteraction.UpdateJoinListener;
import de.kel0002.salp.tempSaving.ModrinthIndexer;
import de.kel0002.salp.tempSaving.ModrinthPages;
import de.kel0002.salp.tempSaving.PlayerAttributes;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.TimeUnit;

public final class Main extends JavaPlugin {
    private static final PlayerAttributes playerAttributes = new PlayerAttributes();
    private static Plugin instance;
    private static ModrinthIndexer modrinthIndexer;
    private static ModrinthPages modrinthPages;
    File playerdataFile = new File(getDataFolder(), "playerdata");

    @Override
    public void onEnable() {
        new Metrics(this, 33534);

        this.getCommand("files").setExecutor(new Command());
        this.getCommand("salp").setExecutor(new Command());

        this.getCommand("files").setTabCompleter(new CommandTabCompleter());
        this.getCommand("salp").setTabCompleter(new CommandTabCompleter());


        instance = this;
        modrinthIndexer = new ModrinthIndexer();
        modrinthPages = new ModrinthPages();

        playerAttributes.load(playerdataFile);

        Bukkit.getPluginManager().registerEvents(new UpdateJoinListener(), this);

        Bukkit.getAsyncScheduler().runAtFixedRate(this, _ -> reCheck(), 1, 1, TimeUnit.HOURS);

    }

    @Override
    public void onDisable() {
        if (!getDataFolder().exists()) getDataFolder().mkdir();
        playerAttributes.save(playerdataFile);
    }

    public void reCheck() {
        modrinthIndexer = new ModrinthIndexer();
        modrinthPages = new ModrinthPages();
    }

    public static PlayerAttributes getPlayerAttributes() {return playerAttributes;}
    public static Plugin getInstance() {return instance;}
    public static ModrinthIndexer getModrinthIndexer() {return modrinthIndexer;}
    public static ModrinthPages getModrinthPages() {return modrinthPages;}
}

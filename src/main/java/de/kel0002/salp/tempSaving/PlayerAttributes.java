package de.kel0002.salp.tempSaving;

import de.kel0002.salp.util.FileUtil;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;

public class PlayerAttributes {
    HashMap<UUID, HashMap<String, Object>> playerMap = new HashMap<>();

    public void set(Player player, String attribute, Object value){
        if (!playerMap.containsKey(player.getUniqueId())) playerMap.put(player.getUniqueId(), new HashMap<>());
        playerMap.get(player.getUniqueId()).put(attribute, value);
    }

    public Object get(Player player, String attribute) {
        if (!playerMap.containsKey(player.getUniqueId())) playerMap.put(player.getUniqueId(), new HashMap<>());
        return playerMap.get(player.getUniqueId()).get(attribute);
    }

    public void save(File file) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(playerMap);
        } catch (Exception _) {}
    }
    public void load(File file) {
        if (!FileUtil.getFile("/plugins/salp/playerdata").exists()) return;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                playerMap = (HashMap<UUID, HashMap<String, Object>>) in.readObject();
        } catch (Exception _){}
    }
}

package de.kel0002.salp.tempSaving;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.kel0002.salp.Main;
import de.kel0002.salp.util.FileUtil;
import de.kel0002.salp.util.Util;
import org.bukkit.Bukkit;

import java.io.File;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.kel0002.salp.util.NetworkStuff.*;

public class ModrinthIndexer {
    HttpClient client = HttpClient.newHttpClient();
    Map<File, Map<String, String>> fileInfos = new HashMap<>();
    List<File> indexing = new ArrayList<>();
    List<File> attempted_index = new ArrayList<>();

    public ModrinthIndexer () {
        Main.getInstance().getLogger().info("Checking for plugin updates...");
        for (File file : FileUtil.getFolderContents("/plugins/")) {
            index(file);
        }
    }

    public void index(File file) {
        Bukkit.getAsyncScheduler().runNow(Main.getInstance(), _ -> {
            if (!file.getName().endsWith(".jar")) return;
            indexing.add(file);
            attempted_index.add(file);

            Map<String, String> current_info = getCurrentlyInstalled(file);
            if (current_info == null) {indexing.remove(file); return;}
            Map<String, String> newest_info = getNewest(current_info.get("project_id"));
            if (newest_info == null) {indexing.remove(file); return;}
            String title = getName(current_info.get("project_id"));
            if (title == null) {indexing.remove(file); return;}

            Map<String, String> merged = new HashMap<>(current_info);
            merged.putAll(newest_info);
            merged.put("name", title);

            indexing.remove(file);

            fileInfos.put(file, merged);
        });
    }

    public boolean isInstalled(String id) {
        return fileInfos.values().stream().anyMatch(inner -> id.equals(inner.get("project_id")));
    }

    public void remove(File file) {
        fileInfos.remove(file);
        indexing.remove(file);
        attempted_index.remove(file);
    }

    public Map<String, String> getCurrentlyInstalled(File file) {
        try {
            String hash = Files.asByteSource(file).hash(Hashing.sha512()).toString();
            String url = "https://api.modrinth.com/v2/version_file/" + hash + "?algorithm=sha512";

            HttpResponse<String> response = client.send(getRequest(url), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) return null;

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            return Map.of(
                    "project_id", json.get("project_id").getAsString(),
                    "current_version_id", json.get("id").getAsString(),
                    "current_hash", hash,
                    "current_version", json.get("version_number").getAsString(),
                    "current_date_published", json.get("date_published").getAsString()
            );
        } catch (Exception e) {return null;}
    }


    public static Map<String, String> getNewest(String project_id) {
        try {
            String loader = Util.getLoader();
            String version = Util.getMCVersion();
            String loaderParam = URLEncoder.encode("[\"" + loader + "\"]", StandardCharsets.UTF_8);
            String versionParam = URLEncoder.encode("[\"" + version + "\"]", StandardCharsets.UTF_8);
            String url = "https://api.modrinth.com/v2/project/" + project_id + "/version?loaders=" + loaderParam + "&game_versions=" + versionParam;

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(getRequest(url), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) return null;

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonArray().get(0).getAsJsonObject();

            return Map.of(
                    "newest_version_id", json.get("id").getAsString(),
                    "newest_version", json.get("version_number").getAsString(),
                    "newest_date_published", json.get("date_published").getAsString(),
                    "newest_link", json.get("files").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString()
            );

        } catch (Exception e) {
            return null;
        }
    }

    public String getName(String project_id) {
        try {
            String url = "https://api.modrinth.com/v2/project/" + project_id;

            HttpResponse<String> response = client.send(getRequest(url), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) return null;

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            return json.get("title").getAsString();


        } catch (Exception e) {return null;}
    }


    public Map<String, String> getInfo(File file) {
        return fileInfos.get(file);
    }

    public boolean hasUpdate(File file) {
        Map<String, String> fileinfo = getInfo(file);
        if (fileinfo == null) return false;
        return Instant.parse(fileinfo.get("current_date_published")).isBefore(Instant.parse(fileinfo.get("newest_date_published")));
    }



    public boolean isIndexing(File file) {
        return indexing.contains(file);
    }
    public boolean notIndexed(File file) {return !attempted_index.contains(file);}
}

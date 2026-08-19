package de.kel0002.salp.tempSaving;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.kel0002.salp.util.Util;

import static de.kel0002.salp.util.NetworkStuff.*;

import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModrinthPages {

    Map<PageKey, List<ModrinthProject>> pageIndex = new HashMap<>();
    Map<TotalPagesKey, Integer> totalPagesIndex = new HashMap<>();
    record PageKey(int pageNumber, String search, String sort) {}
    record TotalPagesKey(String search, String sort) {}

    public ModrinthPages() {}

    public List<ModrinthProject> getPage(int pageNumber, String search, String sort) {
        PageKey pageKey = new PageKey(pageNumber, search, sort);
        if (pageIndex.get(pageKey) != null) return pageIndex.get(pageKey);
        pageIndex.put(pageKey, getNewPage(pageNumber, search, sort));
        return pageIndex.get(pageKey);
    }

    public int getTotalPages(String search, String sort) {
        return totalPagesIndex.getOrDefault(new TotalPagesKey(search, sort), -1);
    }

    private List<ModrinthProject> getNewPage(int pageNumber, String search, String sort) {
        try {
            String loader = Util.getLoader();
            String version = Util.getMCVersion();
            String url = "https://api.modrinth.com/v2/search"

                    + "?facets=" + URLEncoder.encode("["
                        + "[\"versions:" + version + "\"]"
                        + ",[\"categories:" + loader + "\"]"
                        + "]", StandardCharsets.UTF_8)
                    + (search.isEmpty() ? "" : "&query=" + URLEncoder.encode(search, StandardCharsets.UTF_8))
                    + "&index=" + sort
                    + "&offset=" + pageNumber * 16
                    + "&limit=16";


            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(getRequest(url), HttpResponse.BodyHandlers.ofString());


            if (response.statusCode() != 200) return null;
            JsonElement parsed = JsonParser.parseString(response.body());
            JsonArray projectsJsonArray = parsed.getAsJsonObject().getAsJsonArray("hits");

            List<ModrinthProject> projects = new ArrayList<>();

            if (totalPagesIndex.get(new TotalPagesKey(search, sort)) == null) {
                totalPagesIndex.put(new TotalPagesKey(search, sort), (int) Math.ceil((double) parsed.getAsJsonObject().get("total_hits").getAsInt()/16));
            }

            for (JsonElement projectJSON : projectsJsonArray) {
                ModrinthProject project = new ModrinthProject();
                JsonObject json = projectJSON.getAsJsonObject();

                project.setId(json.get("project_id").getAsString());
                project.setName(json.get("title").getAsString());
                project.setAuthor(json.get("author").getAsString());
                project.setDescription(json.get("description").getAsString());
                project.setDownloads(json.get("downloads").getAsInt());
                project.setLikes(json.get("follows").getAsInt());

                project.setUpdated(FileTime.from(Instant.parse(json.get("date_modified").getAsString())));
                project.setCreated(FileTime.from(Instant.parse(json.get("date_created").getAsString())));

                projects.add(project);
            }

            return projects;
        } catch (Exception e) {
            return null;
        }
    }


}

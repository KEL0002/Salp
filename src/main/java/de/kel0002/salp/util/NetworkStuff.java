package de.kel0002.salp.util;

import de.kel0002.salp.Main;

import java.net.URI;
import java.net.http.HttpRequest;

public class NetworkStuff {
    public static HttpRequest getRequest(String url) {
        //Bukkit.getLogger().info("Contacting: " + url);
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", getUserAgent())
                .GET()
                .build();
    }
    public static String getUserAgent() {
        return "KEL0002/salp/" + Main.getInstance().getPluginMeta().getVersion() + " (contact@kel0002.de)";
    }

}

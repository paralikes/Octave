package xyz.gnarbot.gnar.utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DiscordFMLibraries {

    public static final String[] libraryTypes =
            {"electro-hub", "chill-corner", "korean-madness",
            "japanese-lounge", "classical", "retro-renegade",
            "metal-mix", "hip-hop", "electro-swing",
            "purely-pop", "rock-n-roll", "coffee-house"};

    private static Map<String, JSONArray> libraries = new HashMap<>();

    static {
        for(String s : libraryTypes) {
            try {
                OkHttpClient ok = new OkHttpClient();
                String url = new URIBuilder("hhttps://temp.discord.fm/libraries/" + s + "/json")
                        .toString();
                Request request = new Request.Builder()
                        .url(url)
                        .header("Accept", "application/json")
                        .build();
                Response r = ok.newCall(request).execute();
                libraries.put(s, new JSONArray(r.body().string()));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getRandomSong(String library) {
        JSONObject j = libraries.get(library).getJSONObject(new Random().nextInt(libraries.get(library).length()));
        if (j.has("url")) {
            return j.getString("url");
        }

        if(j.getString("service").equals("YouTubeVideo")) return "https://youtube.com/watch?v=" + j.getString("identifier");

        System.out.println(j.toString());
        return "Error, a problem occurred while loading from DiscordFM.";
    }

}

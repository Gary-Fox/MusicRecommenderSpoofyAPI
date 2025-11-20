package org.example.com.musicrecommender.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.core5.http.ParseException;
import org.example.com.musicrecommender.config.Config;
import org.example.com.musicrecommender.model.Track;
import org.example.com.musicrecommender.model.AudioFeatures;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * REST API Client for Spotify Web API
 * Demonstrates REST API calls and JSON parsing
 */
public class SpotifyAPIClient {
    private String accessToken;
    private long tokenExpirationTime;
    private final CloseableHttpClient httpClient;

    public SpotifyAPIClient() {
        this.httpClient = HttpClients.createDefault();
    }

    /**
     * Authenticate with Spotify API using Client Credentials Flow
     */
    public void authenticate() throws IOException {
        String auth = Config.CLIENT_ID + ":" + Config.CLIENT_SECRET;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        HttpPost httpPost = new HttpPost(Config.TOKEN_URL);
        httpPost.setHeader("Authorization", "Basic " + encodedAuth);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new StringEntity("grant_type=client_credentials"));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            String jsonResponse = EntityUtils.toString(response.getEntity());
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

            this.accessToken = jsonObject.get("access_token").getAsString();
            int expiresIn = jsonObject.get("expires_in").getAsInt();
            this.tokenExpirationTime = System.currentTimeMillis() + (expiresIn * 1000L);

            System.out.println("Successfully authenticated with Spotify API");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void ensureValidToken() throws IOException {
        if (accessToken == null || System.currentTimeMillis() >= tokenExpirationTime) {
            authenticate();
        }
    }

    /**
     * Search for tracks by query string
     */
    public List<Track> searchTracks(String query, int limit) throws IOException, ParseException {
        ensureValidToken();

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = String.format("%s/search?q=%s&type=track&limit=%d",
                Config.API_BASE_URL, encodedQuery, limit);

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Bearer " + accessToken);

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            String jsonResponse = EntityUtils.toString(response.getEntity());
            return parseTracksFromSearchResponse(jsonResponse);
        }
    }

    /**
     * Parse Track objects from JSON search response
     */
    private List<Track> parseTracksFromSearchResponse(String jsonResponse) {
        List<Track> tracks = new ArrayList<>();
        JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();

        if (!root.has("tracks")) {
            return tracks;
        }

        JsonArray items = root.getAsJsonObject("tracks").getAsJsonArray("items");

        for (JsonElement element : items) {
            JsonObject item = element.getAsJsonObject();
            Track track = parseTrackFromJson(item);
            tracks.add(track);
        }

        return tracks;
    }

    private Track parseTrackFromJson(JsonObject json) {
        String id = json.get("id").getAsString();
        String name = json.get("name").getAsString();

        List<String> artists = new ArrayList<>();
        JsonArray artistsArray = json.getAsJsonArray("artists");
        for (JsonElement artistElement : artistsArray) {
            artists.add(artistElement.getAsJsonObject().get("name").getAsString());
        }

        String albumName = json.getAsJsonObject("album").get("name").getAsString();

        Track track = new Track(id, name, artists, albumName);
        track.setDurationMs(json.get("duration_ms").getAsInt());
        track.setPopularity(json.get("popularity").getAsInt());

        if (json.has("preview_url") && !json.get("preview_url").isJsonNull()) {
            track.setPreviewUrl(json.get("preview_url").getAsString());
        }

        return track;
    }

    /**
     * Get audio features for a specific track
     * This is the KEY method for building recommendations!
     */
    public AudioFeatures getAudioFeatures(String trackId) throws IOException {
        ensureValidToken();

        String url = String.format("%s/audio-features/%s", Config.API_BASE_URL, trackId);

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Bearer " + accessToken);

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            String jsonResponse = EntityUtils.toString(response.getEntity());
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();

            AudioFeatures features = new AudioFeatures(trackId);
            features.setDanceability(json.get("danceability").getAsDouble());
            features.setEnergy(json.get("energy").getAsDouble());
            features.setValence(json.get("valence").getAsDouble());
            features.setTempo(json.get("tempo").getAsDouble());
            features.setAcousticness(json.get("acousticness").getAsDouble());
            features.setInstrumentalness(json.get("instrumentalness").getAsDouble());
            features.setLiveness(json.get("liveness").getAsDouble());
            features.setSpeechiness(json.get("speechiness").getAsDouble());
            features.setKey(json.get("key").getAsInt());
            features.setMode(json.get("mode").getAsInt());

            return features;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get audio features for multiple tracks in batch
     * More efficient than individual requests
     */
    public List<AudioFeatures> getBatchAudioFeatures(List<String> trackIds) throws IOException, ParseException {
        ensureValidToken();

        // Spotify allows max 100 IDs per request
        List<AudioFeatures> allFeatures = new ArrayList<>();

        for (int i = 0; i < trackIds.size(); i += 100) {
            int end = Math.min(i + 100, trackIds.size());
            List<String> batch = trackIds.subList(i, end);
            String ids = String.join(",", batch);

            String url = String.format("%s/audio-features?ids=%s", Config.API_BASE_URL, ids);

            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Authorization", "Bearer " + accessToken);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String jsonResponse = EntityUtils.toString(response.getEntity());
                JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();
                JsonArray features = root.getAsJsonArray("audio_features");

                for (JsonElement element : features) {
                    if (element.isJsonNull()) continue;

                    JsonObject json = element.getAsJsonObject();
                    AudioFeatures af = new AudioFeatures(json.get("id").getAsString());
                    af.setDanceability(json.get("danceability").getAsDouble());
                    af.setEnergy(json.get("energy").getAsDouble());
                    af.setValence(json.get("valence").getAsDouble());
                    af.setTempo(json.get("tempo").getAsDouble());
                    af.setAcousticness(json.get("acousticness").getAsDouble());
                    af.setInstrumentalness(json.get("instrumentalness").getAsDouble());
                    af.setLiveness(json.get("liveness").getAsDouble());
                    af.setSpeechiness(json.get("speechiness").getAsDouble());
                    af.setKey(json.get("key").getAsInt());
                    af.setMode(json.get("mode").getAsInt());

                    allFeatures.add(af);
                }
            }
        }

        return allFeatures;
    }

    public void close() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
    }
}
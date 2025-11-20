package org.example.com.musicrecommender.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.com.musicrecommender.config.Config;
import org.example.com.musicrecommender.model.Track;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

/**
 * Client-side socket connection to the server
 */
public class ServerConnection {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson;

    public ServerConnection() {
        this.gson = new Gson();
    }

    public void connect() throws IOException {
        socket = new Socket();
        socket.connect(new java.net.InetSocketAddress(Config.SERVER_HOST, Config.SERVER_PORT), 5000); // 5s timeout
        out = new PrintWriter(socket.getOutputStream(), true);
        in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("Connected to server");
    }

    public List<Track> searchTracks(String query) throws IOException {
        JsonObject request = new JsonObject();
        request.addProperty("action", "SEARCH");
        request.addProperty("query", query);
        request.addProperty("limit", 20);

        return sendRequest(request);
    }

    public List<Track> getRecommendations(Track seedTrack) throws IOException {
        JsonObject request = new JsonObject();
        request.addProperty("action", "RECOMMEND");
        request.addProperty("trackId", seedTrack.getId());
        request.addProperty("trackName", seedTrack.getName());
        request.addProperty("trackArtist", seedTrack.getArtists().get(0));
        request.addProperty("trackAlbum", seedTrack.getAlbumName());
        request.addProperty("count", 10);

        return sendRequest(request);
    }

    private List<Track> sendRequest(JsonObject request) throws IOException {
        if (socket == null || out == null || in == null || !isConnected()) {
            throw new IllegalStateException("Not connected. Call connect() and ensure it succeeds before sending requests.");
        }
        out.println(gson.toJson(request));
        String responseLine = in.readLine();
        if (responseLine == null) {
            throw new IOException("Server closed the connection or sent no data.");
        }
        JsonObject response = gson.fromJson(responseLine, JsonObject.class);
        if ("success".equals(response.get("status").getAsString())) {
            Track[] tracksArray = gson.fromJson(response.get("data"), Track[].class);
            return List.of(tracksArray);
        } else {
            throw new IOException("Server error: " + response.get("message").getAsString());
        }
    }


    public void disconnect() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}
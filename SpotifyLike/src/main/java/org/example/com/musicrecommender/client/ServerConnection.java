package org.example.com.musicrecommender.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.com.musicrecommender.config.Config;
import org.example.com.musicrecommender.model.Track;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Proxy;
import java.net.Socket;
import java.util.List;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.example.com.musicrecommender.model.Artist;


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

    public void robustConnect() throws IOException {
        String host = Config.SERVER_HOST;
        int port = Config.SERVER_PORT;

        // Log what we're doing
        System.out.printf("Attempting connect to '%s':%d%n", host, port);
        System.out.printf("Proxy flags: socks=%s http=%s https=%s useSystemProxies=%s%n",
                System.getProperty("socksProxyHost"),
                System.getProperty("http.proxyHost"),
                System.getProperty("https.proxyHost"),
                System.getProperty("java.net.useSystemProxies"));

        // Bypass *any* proxy just for this socket
        socket = new Socket(Proxy.NO_PROXY);

        // Try a few sensible address variants (avoids IPv6/localhost surprises)
        List<String> candidates = new java.util.ArrayList<>();
        candidates.add(host);
        // If user configured "localhost", try IPv4 loopback explicitly too
        if ("localhost".equalsIgnoreCase(host)) candidates.add("127.0.0.1");

        IOException last = null;
        for (String candidate : candidates) {
            try {
                InetAddress[] addrs = InetAddress.getAllByName(candidate);
                System.out.printf("Resolved %s to: %s%n", candidate, java.util.Arrays.toString(addrs));
                for (InetAddress a : addrs) {
                    try {
                        System.out.printf("Connecting to %s:%d with 5s timeout...%n", a.getHostAddress(), port);
                        socket.connect(new InetSocketAddress(a, port), 5000);
                        System.out.printf("Connected to %s%n", socket.getRemoteSocketAddress());
                        // Success: set up streams and return
                        out = new PrintWriter(socket.getOutputStream(), true);
                        in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        return;
                    } catch (IOException e) {
                        System.err.printf("Connect failed to %s:%d -> %s%n", a.getHostAddress(), port, e);
                        last = e;
                    }
                }
            } catch (IOException e) {
                System.err.printf("DNS resolve failed for %s -> %s%n", candidate, e);
                last = e;
            }
        }
        throw (last != null ? last : new IOException("Unable to connect (no exception captured)"));
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
        request.addProperty("trackArtist", seedTrack.getArtists().getName());
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
package org.example.com.musicrecommender.server;

import org.example.com.musicrecommender.api.SpotifyAPIClient;
import org.example.com.musicrecommender.config.Config;
import org.example.com.musicrecommender.server.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Multi-threaded server that handles multiple client connections
 */
public class MusicRecommendationServer {
    private final int port;
    private final SpotifyAPIClient apiClient;
    private final ExecutorService threadPool;
    private volatile boolean running;

    public MusicRecommendationServer(int port) {
        this.port = port;
        this.apiClient = new SpotifyAPIClient();
        this.threadPool = Executors.newFixedThreadPool(10);
        this.running = false;
    }

    public void start() throws IOException {
        System.out.println("Authenticating with Spotify API...");
        apiClient.authenticate();

        running = true;

        // Bind explicitly to host + port so there’s no ambiguity
        String bindHost = Config.SERVER_HOST;     // e.g., "127.0.0.1"
        int    bindPort = this.port;              // typically Config.SERVER_PORT

        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new java.net.InetSocketAddress(bindHost, bindPort));
            System.out.printf("Music Recommendation Server listening on %s:%d%n", bindHost, bindPort);
            System.out.println("Waiting for client connections...");

            while (running) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, apiClient);
                threadPool.execute(handler);
            }
        } finally {
            shutdown();
        }
    }


    public void shutdown() {
        running = false;
        threadPool.shutdown();
        try {
            apiClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        MusicRecommendationServer server = new MusicRecommendationServer(Config.SERVER_PORT);
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));

        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
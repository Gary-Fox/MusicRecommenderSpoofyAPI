package org.example.com.musicrecommender.config;

public class Config
{
    // Replace with your Spotify credentials
    public static final String CLIENT_ID = "da611c839f7f482da097b710f5ec32d3";
    public static final String CLIENT_SECRET = "6779aae23a374fdda441f34cad01c2a1";

    // API Endpoints
    public static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    public static final String API_BASE_URL = "https://api.spotify.com/v1";

    // Server Configuration
    public static final int SERVER_PORT = 8080;
    public static final String SERVER_HOST = System.getProperty("SERVER_HOST",
            System.getenv().getOrDefault("SERVER_HOST", "127.0.0.1"));

    // Tracks to analyze for recommendations
    public static final int TRACK_POOL_SIZE = 50;
    public static final int DEFAULT_RECOMMENDATIONS = 10;


}
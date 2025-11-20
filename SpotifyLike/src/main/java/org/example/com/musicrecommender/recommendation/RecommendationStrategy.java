package org.example.com.musicrecommender.recommendation;

import org.apache.hc.core5.http.ParseException;
import org.example.com.musicrecommender.model.Track;
import org.example.com.musicrecommender.api.SpotifyAPIClient;
import java.util.List;
import java.io.IOException;

/**
 * Strategy interface for different recommendation algorithms
 * Demonstrates Abstraction and Strategy Pattern
 */
public interface RecommendationStrategy {
    /**
     * Generate recommendations based on a seed track
     * @param seedTrack The track to base recommendations on
     * @param apiClient API client for fetching additional data
     * @param count Number of recommendations to generate
     * @return List of recommended tracks
     */
    List<Track> recommend(Track seedTrack, SpotifyAPIClient apiClient, int count) throws IOException, ParseException;

    /**
     * Get the name of this recommendation strategy
     */
    String getStrategyName();
}
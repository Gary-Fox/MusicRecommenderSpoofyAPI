package org.example.com.musicrecommender.recommendation;

import org.apache.hc.core5.http.ParseException;
import org.example.com.musicrecommender.api.SpotifyAPIClient;
import org.example.com.musicrecommender.model.Track;

import java.io.IOException;
import java.util.List;

public class GenreBasedStrategy implements RecommendationStrategy
{
    //@Override
    public List<Track> recommend(Track seedTrack, SpotifyAPIClient apiClient, int count) throws IOException, ParseException {
        return null;
    }

   //@Override
    public String getStrategyName() {
        return "GenreBasedStrategy";
    }
}
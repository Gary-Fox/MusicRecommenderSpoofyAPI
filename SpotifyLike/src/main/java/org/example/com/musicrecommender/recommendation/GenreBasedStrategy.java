package org.example.com.musicrecommender.recommendation;

import org.apache.hc.core5.http.ParseException;
import org.example.com.musicrecommender.api.SpotifyAPIClient;
import org.example.com.musicrecommender.config.Config;
import org.example.com.musicrecommender.model.Artist;
import org.example.com.musicrecommender.model.AudioFeatures;
import org.example.com.musicrecommender.model.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenreBasedStrategy implements RecommendationStrategy
{
    //@Override
    public List<Track> recommend(Track seedTrack, SpotifyAPIClient apiClient, int count) throws IOException, ParseException
    {
        AudioFeatures seedFeatures = apiClient.getAudioFeatures(seedTrack.getId());
        seedTrack.setAudioFeatures(seedFeatures);
        //getArtists() used to return a string instead of an Artist object.
        Artist seedArtist = seedTrack.getArtists();
        List<String> seedGenre = seedArtist.getGenres();

        // RandomSearch can be found in SpotifyAPIClient, line 91.
        List<Track> candidateTracks = apiClient.randomSearch(Config.TRACK_POOL_SIZE);
        List<String> candidateIds = candidateTracks.stream()
                .map(Track::getId)
                .filter(id -> !id.equals(seedTrack.getId())) // Exclude seed track
                .collect(Collectors.toList());

        List<AudioFeatures> candidateFeatures = apiClient.getBatchAudioFeatures(candidateIds);
        List<String> canidateGenres = new ArrayList<>();

        for (Track track : candidateTracks)
        {
            canidateGenres.add(track.getArtists().getGenres().toString());
        }

        //Now that we have the list of canidate genres, we would then want to compare them.
        //The more genres the candiate's match against the seed genre, the higher their score
        //Highest 10 scores get pushed to the user, perhaps slightly weighted by popularity of artist/track.
        // to be implemented....

        return null;
    }

   //@Override
    public String getStrategyName() {
        return "GenreBasedStrategy";
    }
}
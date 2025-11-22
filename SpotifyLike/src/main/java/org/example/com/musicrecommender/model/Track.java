package org.example.com.musicrecommender.model;

import org.example.com.musicrecommender.api.SpotifyAPIClient;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Represents a music track with encapsulated properties.
 * Demonstrates Encapsulation - private fields with public getters/setters
 */
public class Track {
    private String id;
    private String name;
    private Artist artists;
    private String albumName;
    private int durationMs;
    private int popularity;
    private String previewUrl;
    private org.example.com.musicrecommender.model.AudioFeatures audioFeatures;

    // Constructor
    public Track(String id, String name, Artist artists, String albumName) {
        this.id = id;
        this.name = name;
        this.artists = artists;
        this.albumName = albumName;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Artist getArtists() {
        return artists;
    }

    public void setArtists(Artist artists) {
        this.artists = artists;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public int getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(int durationMs) {
        this.durationMs = durationMs;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public AudioFeatures getAudioFeatures() throws IOException {
        // spotifyApi.getAudioFeaturesForTrack(trackId).build().execute()
        SpotifyAPIClient API = new SpotifyAPIClient();
        return API.getAudioFeatures(this.id);
    }

    public void setAudioFeatures(AudioFeatures audioFeatures) {
        this.audioFeatures = audioFeatures;
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s)",
                String.join(", ", artists.getName()), name, albumName);
    }

    public String getFormattedDuration() {
        int seconds = durationMs / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return Objects.equals(id, track.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
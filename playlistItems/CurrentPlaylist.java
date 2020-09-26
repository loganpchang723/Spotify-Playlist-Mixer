package advisor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

/**
 *The "CurrentPlaylist" object deals with all relevant details to the original playlist selected to be mixed, including determining the new tracks in hte mixed playlist
 * @author Logan Chang (Github: loganpchang723)
 */
public class CurrentPlaylist {
    JsonObject playlistJSON;
    String accessToken;
    ArrayList<String> newPlaylistURIs = new ArrayList<>();

    /**
     * Constructs an instance of the CurrentPlaylist
     * @param playlistJSON  The JsonObject of the playlist selected by the user to be mixed
     * @param accessToken
     */
    public CurrentPlaylist(JsonObject playlistJSON, String accessToken){
        this.playlistJSON = playlistJSON;
        this.accessToken = accessToken;
    }

    /**
     * From the JSON object of the current playlist, return an ArrayList of the names of all tracks in the playlist according to the order of the playlist
     * @return                                  Return an ArrayList of the names of all tracks in the selected playlist according to the order of the playlist
     * @throws java.io.IOException              Possible Exception while sending the HTTP Request
     * @throws java.lang.InterruptedException   Possible Exception while sending the HTTP Request
     */
    ArrayList<String> getAllCurrentTrackNames() throws java.io.IOException, java.lang.InterruptedException{
        ArrayList<String> currentTracks = new ArrayList<>();
        JsonObject tracks = playlistJSON.get("tracks").getAsJsonObject();
        String tracksLink = tracks.get("href").getAsString();
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(tracksLink))
                .GET()
                .build();
        HttpResponse<String> response = client.send(httpRequest,HttpResponse.BodyHandlers.ofString());
        JsonArray items = JsonParser.parseString(response.body()).getAsJsonObject().getAsJsonArray("items");
        for(JsonElement item: items) {
            JsonObject track = item.getAsJsonObject().get("track").getAsJsonObject();
            String trackName = track.get("name").getAsString();
            currentTracks.add(trackName);
        }
        return currentTracks;
    }

    /**
     * Prints the name, album, album type(single, album, compilation), artist for each track in the playlist
     * Also prints the name, origin (same album or one of the same artist's top tracks), API link to track, and URI of the track being put into the new playlist
     * Will modify ArrayList of URI's of tracks selected to be in new playlist in 'newPlaylistURIs'
     * @throws java.io.IOException              Possible Exception while sending the HTTP Request
     * @throws java.lang.InterruptedException   Possible Exception while sending the HTTP Request
     */
    void getTracksInfo() throws java.io.IOException, java.lang.InterruptedException{
        //access the JSON for each track in the playlist
        JsonObject tracks = playlistJSON.get("tracks").getAsJsonObject();
        String tracksLink = tracks.get("href").getAsString();
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(tracksLink))
                .GET()
                .build();
        HttpResponse<String> response = client.send(httpRequest,HttpResponse.BodyHandlers.ofString());
        JsonArray items = JsonParser.parseString(response.body()).getAsJsonObject().getAsJsonArray("items");
        //for each track in the playlist...
        int counter = 1;
        for(JsonElement item: items){
            //get relevant info about the current track in the playlist (relative order in the original playlist, track name)
            JsonObject track = item.getAsJsonObject().get("track").getAsJsonObject();
            String trackName = track.get("name").getAsString();
            String trackURI = track.get("uri").getAsString();
            System.out.println("Track #" +counter+": "+trackName);

            //get relevant info about the current track's album (album name, album type, album API link)
            JsonObject albumJSON = track.get("album").getAsJsonObject();
            Album album = new Album(albumJSON, accessToken, trackName, trackURI);
            System.out.println(album.getAlbumName()+" - "+album.getAlbumType()+": "+album.getAlbumLink());

            //get relevant info about the current track's artist (artist name, artist API link)
            JsonArray artists = track.getAsJsonArray("artists");
            JsonObject artistJSON = artists.get(0).getAsJsonObject();
            Artist artist = new Artist(artistJSON, accessToken, trackName, trackURI);
            System.out.println(artist.getArtistName()+" "+artist.getArtistLink());

            //when swapping, if the song exists in a "single" album, choose one of the artist's top tracks
            if(album.getAlbumType().equals("single")){
                newPlaylistURIs = artist.getArtistTrack(getAllCurrentTrackNames(), newPlaylistURIs);
            } //else if the song exists in an "album" or "compilation", randomly choose whether to pick another song from the album or one of the artist's top tracks
            // The random choice is 80/20 skewed to pick a song from the same album over one of the artist's top tracks in order to better maintain the "vibe" of the original track
            else{
                if(Math.random()>0.2) newPlaylistURIs = album.getAlbumTracks(getAllCurrentTrackNames(), newPlaylistURIs);
                else newPlaylistURIs = artist.getArtistTrack(getAllCurrentTrackNames(), newPlaylistURIs);
            }
            counter++;
        }
    }

    /**
     * Gets and returns the ArrayList of URIs of tracks selected to be in new playlist
     * @return newPlaylistURIs  ArrayList of URIs of tracks selected to be in new playlist
     */
    ArrayList<String> getNewPlaylistURIs(){ return newPlaylistURIs; }
}

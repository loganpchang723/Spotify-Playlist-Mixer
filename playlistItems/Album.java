package advisor;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
/**
 * An "Album" object will deal with all relevant info about the album of the current track in the selected playlist and make a selection from other tracks from the album for the new playlist
 * @author Logan Chang (Github: loganpchang723)
 */

public class Album {
    JsonObject albumJSON;
    String accessToken;
    String trackName;
    String trackURI;
    /**
     *Constructs a new Album instance
     * @param albumJSON     The JsonObject of the Album's profile
     * @param accessToken   The access token provided from authorization needed for making HTTP Requests to the API
     * @param trackName     The name of the original track chosen from the original playlist
     * @param trackURI      The URI of the original track chosen from the original playlist
     */
    public Album(JsonObject albumJSON, String accessToken, String trackName, String trackURI){
        this.albumJSON = albumJSON;
        this.accessToken = accessToken;
        this.trackName = trackName;
        this.trackURI = trackURI;
    }

    /**
     * Gets and returns the API link of the album
     * @return      String of API link of the album to which the selected track belongs
     */
    String getAlbumLink(){ return albumJSON.get("href").getAsString(); }
    /**
     * Gets and returns the name of the album
     * @return      Name of the album to which the selected track belongs
     */
    String getAlbumName(){ return albumJSON.get("name").getAsString(); }
    /**
     * Gets and returns the type of the album
     * @return      Type album to which the selected track belongs (i.e. single, album, or compilation)
     */
    String getAlbumType(){ return albumJSON.get("album_type").getAsString(); }

    /**
     * From the tracks in the album of the selected track, choose a random track that is preferably neither in the selected playlist or the new playlist being made
     * @param currentTracks                     ArrayList of tracks in the current playlist
     * @param newPlaylistURIs                   ArrayList of the URI's of the tracks selected to be in the new playlist
     * @return                                  An updated reference of the ArrayList of the URI's of the tracks selected to be in the new playlist, including the appended choice for the next track in the playlist
     * @throws java.io.IOException              Possible Exception while sending the HTTP Request
     * @throws java.lang.InterruptedException   Possible Exception while sending the HTTP Request
     */
    ArrayList<String> getAlbumTracks(ArrayList<String> currentTracks, ArrayList<String> newPlaylistURIs) throws java.io.IOException, java.lang.InterruptedException{
        String chosenTrackName = trackName;
        String chosenTrackLink = "";
        String chosenTrackURI = trackURI;
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(getAlbumLink()+"/tracks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(httpRequest,HttpResponse.BodyHandlers.ofString());
        JsonArray tracks = JsonParser.parseString(response.body()).getAsJsonObject().getAsJsonArray("items");
        System.out.println("# of Tracks in " + getAlbumName()+": "+tracks.size());
        //keep track of which tracks from the album haven't been tried yet on this method call (the presently-selected song)
        ArrayList<Integer> availTracks = new ArrayList<>();
        for(int i= 0; i< tracks.size(); i++) availTracks.add(i);
        while (currentTracks.contains(chosenTrackName)) {
            //if it is impossible to pick a track from the album that satisfies these conditions (not in the old playlist and not in the playlist being created), put the original track into the new playlist
            if(availTracks.isEmpty()) break;
            //otherwise, pick a random track from the album...
            int randomChoice = (int)(Math.random() * availTracks.size());
            JsonObject chosenTrack = tracks.get((availTracks.get(randomChoice))).getAsJsonObject();
            chosenTrackName = chosenTrack.get("name").getAsString();
            chosenTrackLink = chosenTrack.get("href").getAsString();
            chosenTrackURI = chosenTrack.get("uri").getAsString();
            //if the randomly-chosen track is not in the old playlist and not in the playlist being created, we have found our new track
            if (!newPlaylistURIs.contains(chosenTrackURI) && !currentTracks.contains(chosenTrackName)) break;
            //otherwise, remove the chosen track from possible choices and try again
            else {
                availTracks.remove(randomChoice);
                chosenTrackName = trackName;
            }
        }
        //print relevant info about the selected track for the new playlist
        System.out.println("\nRANDOM TRACK FROM "+getAlbumName());
        System.out.println(chosenTrackName+" "+"Link: "+chosenTrackLink+" URI "+chosenTrackURI+"\n");
        //update the ArrayList of URI's of tracks to be added to the new playlist and return it
        newPlaylistURIs.add(chosenTrackURI);
        return newPlaylistURIs;
    }
}

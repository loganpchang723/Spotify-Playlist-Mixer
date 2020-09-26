package advisor;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
/**
 *The "NewPlaylist" object deals with all creating a new playlist and adding the selected tracks to the playlist in the specified order
 * @author Logan Chang (Github: loganpchang723)
 */
public class NewPlaylist {
    String accessToken;
    String userID;
    String newPlaylistLink;

    /**
     * Constructs a new instance of a NewPlaylist
     * @param userID        The Spotify ID of the user needed to access the user's playlist library
     * @param accessToken   The access token provided from authorization needed for making HTTP Requests to the API
     */
    public NewPlaylist(String userID, String accessToken){
        this.userID = userID;
        this.accessToken = accessToken;
    }

    /**
     * Creates an empty playlist with the title "{original playlist name} BUT MIXED!! :) ({date and time of creation})"
     * @param currentName                       The name of the original playlist being mixed
     * @throws java.io.IOException              Possible Exception while sending the HTTP Request
     * @throws java.lang.InterruptedException   Possible Exception while sending the HTTP Request
     */
    void createNewPlaylist(String currentName) throws java.io.IOException, java.lang.InterruptedException{
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        //create the name of the new playlist in this format: "{original playlist name} BUT MIXED!! :) ({date and time of creation})"
        String newName = currentName+" BUT MIXED!! :) — ("+dtf.format(now)+")";
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .uri(URI.create("https://api.spotify.com/v1/users/"+userID+"/playlists/?limit=50"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\""+newName+"\", \"public\":false}"))
                .build();
        HttpResponse<String> response = client.send(httpRequest,HttpResponse.BodyHandlers.ofString());
        JsonObject jo = JsonParser.parseString(response.body()).getAsJsonObject();
        //if playlist was successfully created, print such a message in the output window and save the API link to the newly-created playlist in newPlaylistLink
        if(!response.body().contains("error")) {
            System.out.println("New Playlist Made");
            newPlaylistLink = jo.get("href").getAsString();
        }
    }
    void addTracks(List<String> newPlaylistURIs) throws Exception{
        if(newPlaylistLink == null) return;
        StringBuilder uriJSON = new StringBuilder("{\"uris\":[");
        for(int i = 0; i<newPlaylistURIs.size(); i++){
            String uri = newPlaylistURIs.get(i);
            if(i == newPlaylistURIs.size()-1) uriJSON.append("\""+uri+"\"");
            else uriJSON.append("\""+uri+"\",");
        }
        uriJSON.append("]}");
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .uri(URI.create(newPlaylistLink+"/tracks"))
                .POST(HttpRequest.BodyPublishers.ofString(uriJSON.toString()))
                .build();
        HttpResponse<String> response = client.send(httpRequest,HttpResponse.BodyHandlers.ofString());
        JsonObject jo = JsonParser.parseString(response.body()).getAsJsonObject();
        if(!response.body().contains("error")) {
            System.out.println("Tracks have been added!");
        }
    }

}

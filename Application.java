package advisor;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Scanner;
/**
 * An "Application" object will deal with all relevant info regarding running the actual driver program
 * @author Logan Chang (Github: loganpchang723)
 */

public class Application {
    //CLIENT_ID and CLIENT SECRET taken from my Spotify App Dashboard (link: https://developer.spotify.com/dashboard/applications/7e25696ab9294afe9f72e41bf08ab242)
    private static final String CLIENT_ID = "7e25696ab9294afe9f72e41bf08ab242";
    private static final String CLIENT_SECRET = "624f9994417c490d9d1ec9f601544a55";
    private static final String SERVER = "http://localhost";
    private static final int PORT = 8080;
    private static String accessToken = "";
    private static String userID = "";

    public Application(){}

    /**
     * Runs the driver program
     * @throws java.lang.Exception   Possible Exception while sending the multiple HTTP Request
     */
    void run() throws java.lang.Exception{
        Scanner scan = new Scanner(System.in);
        String localServerUrl = "https://accounts.spotify.com";
        String localAPIUrl = "https://api.spotify.com";

        //*see "LocalServer.java"*
        LocalServer localServer = new LocalServer(CLIENT_ID, CLIENT_SECRET, SERVER, PORT);
        localServer.start(localServerUrl);
        while(!localServer.isAuthorized()) Thread.sleep(10);
        localServer.stop(localServerUrl);
        //upon stopping the server, the program will be authorized to access user data and create playlists on the user's account
        accessToken = localServer.getAccessToken();
        userID = localServer.getUserId();
        //prompt whether playlist to be mixed is in the users library or is a playlist by Spotify
        System.out.println("\nUser Playlists or Spotify Playlists? (input \"user\" or \"spotify\"): ");
        String userOrSpotify = scan.nextLine();
        //create and initialize instances to be used to parse the selected playlist
        String currentPlaylistName = null;
        JsonObject currentPlaylistJSON = null;
        //set the Playlist object to be parsed to a selected playlist in the user's library
        if(userOrSpotify.equals("user")) {
            localServer.getUserPlaylists(localAPIUrl);
            System.out.println("\nWhich playlist? (case sensitive): ");
            currentPlaylistName = scan.nextLine();
            currentPlaylistJSON = localServer.getSpecificUserPlaylist(currentPlaylistName);
        }
        //set the Playlist object to be parsed to a selected playlist created by Spotify, as first identified by the playlist's category/genre
        else if(userOrSpotify.equals("spotify")){
            localServer.getCategories();
            System.out.println("Which Category? (case sensitive): ");
            String category = scan.nextLine();
            localServer.getCategoricalPlaylists(category);
            System.out.println("Which Playlist? (case sensitive): ");
            currentPlaylistName = scan.nextLine();
            currentPlaylistJSON = localServer.getSpecificSpotifyPlaylist(currentPlaylistName);
        }
        //create a CurrentPlaylist object based off the playlist selected and retrieve it's relevant info
        CurrentPlaylist currentPlaylist = new CurrentPlaylist(currentPlaylistJSON, accessToken);
        currentPlaylist.getTracksInfo();
        //using the info from the selected playlist, create a NewPlaylist object and add the newly-crated playlist to the user's library
        NewPlaylist newPlaylist = new NewPlaylist(userID, accessToken);
        newPlaylist.createNewPlaylist(currentPlaylistName);
        ArrayList<String> newPlaylistURIs = currentPlaylist.getNewPlaylistURIs();
        for(int i = 0; i<=(newPlaylistURIs.size()/100); i++){
            int start = i*100;
            int end = Math.min(newPlaylistURIs.size(), (i*100+99));
            System.out.println(newPlaylistURIs.subList(start, end).toString());
            newPlaylist.addTracks(newPlaylistURIs.subList(start, end));
        }
    }
}

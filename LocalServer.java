package advisor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
/**
 * The "LocalServer" object creates a local server on an empty port (in this case, it's port 8080) and runs relevant functions
 * @author Logan Chang (Github: loganpchang723)
 */
public class LocalServer{
    HttpServer server;
    String client_id;
    String client_secret;
    String serverURI;
    int port;
    boolean gotToken = false;
    boolean authorized = false;
    String code = "";
    HttpResponse<String> accessTokenResponse;
    JsonArray userPlaylists;
    HashMap<String, String> categoryIDs = new HashMap<>();
    HashMap<String, String> categoricalPlaylists = new HashMap<>();

    /**
     * Constructs a new instance of LocalServer
     * @param client_id     The client ID of my web app as specified by the web app's website (Link: https://developer.spotify.com/dashboard/applications/7e25696ab9294afe9f72e41bf08ab242)
     * @param client_secret The client secret of my web app as specified by the web app's website (Link: https://developer.spotify.com/dashboard/applications/7e25696ab9294afe9f72e41bf08ab242)
     * @param serverURI     The URI of the local server (i.e. http://localhost)
     * @param port          The local port number to run the local server from (i.e. 8080)
     */
    LocalServer(String client_id, String client_secret, String serverURI, int port){
        this.client_id = client_id;
        this.client_secret = client_secret;
        this.serverURI = serverURI;
        this.port = port;
    }
    /**
     * Gets and returns the value of authorized
     * @return authorized   true, if authorized is true, else false
     */
    boolean isAuthorized(){ return authorized;}

    /**
     * Getter for access token received after authorizing the program to access and manipulate personal data
     * @return accessToken  String representation of API access token
     */
    String getAccessToken() {
        JsonObject jsonObject =  JsonParser.parseString(accessTokenResponse.body()).getAsJsonObject();
        String accessToken = jsonObject.get("access_token").getAsString();
        return accessToken;
    }

    /**
     * Getter for the authorized user's Spotify ID for API access
     * @return userID                           String representation of authorized user's Spotify ID for API access
     * @throws java.io.IOException              Possible Exception while sending the HTTP Request
     * @throws java.lang.InterruptedException   Possible Exception while sending the HTTP Request
     */
    String getUserId() throws java.io.IOException, java.lang.InterruptedException{
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + getAccessToken())
                .uri(URI.create("https://api.spotify.com/v1/me"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(httpRequest,HttpResponse.BodyHandlers.ofString());
        JsonObject user = JsonParser.parseString(response.body()).getAsJsonObject();
        return user.get("id").getAsString();
    }

    /**
     * Build and begin running client server upon prompting user to Spotify account authorization
     * @param localServerUrl                    URL of local server for redirection and client server binding purposes (i.e. http://localserver:8080)
     * @throws java.io.IOException              Possible Exception while sending the HTTP Request
     * @throws java.lang.InterruptedException   Possible Exception while sending the HTTP Request
     */
    void start(String localServerUrl) throws java.io.IOException, java.lang.InterruptedException{
        //build client server by binding to empty port 8080
        server = HttpServer.create();
        server.bind(new InetSocketAddress(8080), 0);
        //create mapping for handler (created in method call) to match any request URI with prefix of "/"
        server.createContext("/",
                new HttpHandler() {
                    //will post a "success" or "error" message on local port HTML after action taken on authorization page
                    public void handle(HttpExchange exchange) throws IOException {
                        String query = exchange.getRequestURI().getQuery();
                        String responseText = gotRequest(query);
                        exchange.sendResponseHeaders(200, responseText.length());
                        exchange.getResponseBody().write(responseText.getBytes());
                        exchange.getResponseBody().close();
                    }
                }
        );
        //start client server listening and prompt user to link to authorize program access
        server.start();
        System.out.println("Local Server Started");
        System.out.println("use this link to request access code:\n"+localServerUrl+"/authorize?client_id=" +
                client_id + "&scope=playlist-modify,playlist-modify-private&redirect_uri=" + serverURI + ":" + port + "&response_type=code");
        //wait for authorization to be approved and receive authorization code from Spotify server
        System.out.println("waiting for code...");
        while (code.length() == 0) {
            Thread.sleep(10);
        }
    }

    /**
     * Terminate the local client server after receiving the authorization code and obtain the access token through HTTP request
     * @param localServerUrl                    URL of local server that was used for redirection and client server binding purposes (i.e. http://localserver:8080)
     * @throws java.io.IOException              Possible Exception while sending the HTTP Request
     * @throws java.lang.InterruptedException   Possible Exception while sending the HTTP Request
     */
    void stop(String localServerUrl) throws java.io.IOException, java.lang.InterruptedException{
        server.stop(1);
        System.out.println("requesting access_token...");
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(localServerUrl + "/api/token"))
                .POST(HttpRequest.BodyPublishers.ofString("client_id="+client_id+"&client_secret="+client_secret+"&grant_type=authorization_code&code="+code+"&redirect_uri=http://localhost:8080"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        accessTokenResponse = response;
        //if access token was properly received, print such a message in the output window
        if(response.body().contains("access_token")) {
            System.out.println("access token received.");
            gotToken = true;
        }
        //otherwise, print a message saying no token was received in output window
        else{
            System.out.println("Error, no access token.");
        }
    }

    /**
     * Returns the "success" or "error" message to be printed to local port HTML after action taken on authorization page to retrieve authorization code
     * Will also modify local "code" and "authorized" variables to reflect the value of the authorization code and state of authorization, respectively
     * @param query     Query response from API server upon filling out authorization request
     * @return          String message describing state of access authorization
     */
    String gotRequest(String query){
        if (query != null) {
            System.out.println("Query: " + query);
            String[] queryParams = query.split("&");
            if (queryParams.length > 0) {
                for (String param : queryParams) {
                    String[] parameters = param.split("=");
                    if (parameters.length > 0) {
                        //if authorization was successful
                        if ("code".equalsIgnoreCase(parameters[0])) {
                            code = parameters[1];
                            authorized = true;
                            System.out.println("code received.");
                            return "Got the code. Return back to your program.";
                        }
                        //if error occurred during authorization
                        if ("error".equalsIgnoreCase(parameters[0])) {
                            System.out.println("Authorization code not found. Try again.");
                            return "Not found authorization code. Try again.";
                        }
                    }
                }
            }
        }
        return "Not found authorization code. Try again.";
    }

    //The following methods are only relevant if the user indicated a personally-owned playlist to be mixed

    /**
     * Prints a list of names of all playlists in user's library, if user playlist previously indicated their preference for a user playlist
     * @param localAPIUrl                       URL of Spotify API (i.e https://api.spotify.com)
     * @throws java.io.IOException              Possible Exception while sending the HTTP Request
     * @throws java.lang.InterruptedException   Possible Exception while sending the HTTP Request
     */
    void getUserPlaylists(String localAPIUrl) throws java.io.IOException, java.lang.InterruptedException{
        HttpClient client = HttpClient.newBuilder().build();
        String accessToken = getAccessToken();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(localAPIUrl+"/v1/me/playlists"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(httpRequest,HttpResponse.BodyHandlers.ofString());
        JsonObject userPlaylistsJSON =  JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray items = userPlaylistsJSON.getAsJsonArray("items");
        userPlaylists = items;
        System.out.println("\nPLAYLIST TITLES");
        for(JsonElement playlist: userPlaylists){
            System.out.println(playlist.getAsJsonObject().get("name").getAsString());
        }
    }

    /**
     * Returns the JsonObject of the specific playlist in the user's library as indicated by the user's input
     * @param name      Name of the selected playlist
     * @return          the JsonObject of the selected playlist if it exists, else null
     */
    JsonObject getSpecificUserPlaylist(String name){
        for(JsonElement playlist: userPlaylists){
            if(name.equals(playlist.getAsJsonObject().get("name").getAsString())){
                return playlist.getAsJsonObject();
            }
        }
        return null;
    }

    //all methods that follow are only relevant if the user specifies that he/she wants a Spotify-created playlist to be mixed

    /**
     * Print the top 50 categories (genres) of playlists generated by Spotify in the US
     * @throws java.io.IOException              Possible Exception while sending the HTTP Request
     * @throws java.lang.InterruptedException   Possible Exception while sending the HTTP Request
     */
    void getCategories() throws java.io.IOException, java.lang.InterruptedException{
        HttpClient client = HttpClient.newBuilder().build();
        String accessToken = getAccessToken();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create("https://api.spotify.com/v1/browse/categories/?locale=US&limit=50"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(httpRequest,HttpResponse.BodyHandlers.ofString());
        JsonObject categoriesJSON =  JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray items = categoriesJSON.get("categories").getAsJsonObject().getAsJsonArray("items");
        for(JsonElement item : items) {
            String name = item.getAsJsonObject().get("name").getAsString();
            String id = item.getAsJsonObject().get("id").getAsString();
            categoryIDs.put(name, id);
        }
        System.out.println(categoryIDs.toString());
        System.out.println("---CATEGORIES---");
        for(String category : categoryIDs.keySet()) System.out.println(category);
    }

    /**
     * Prints the names of the top 50 playlists in the category specified by the user's input
     * @param category                          the playlist category which the user wants to browse
     * @throws java.io.IOException              Possible Exception while sending the HTTP Request
     * @throws java.lang.InterruptedException   Possible Exception while sending the HTTP Request
     */
    void getCategoricalPlaylists(String category) throws Exception{
        String catID = categoryIDs.get(category);
        HttpClient client = HttpClient.newBuilder().build();
        String accessToken = getAccessToken();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create("https://api.spotify.com/v1/browse/categories/"+catID+"/playlists/?country=US&limit=50"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(httpRequest,HttpResponse.BodyHandlers.ofString());
        JsonObject jsonObject =  JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray items = jsonObject.get("playlists").getAsJsonObject().getAsJsonArray("items");
        for(JsonElement item : items) {
            String name = item.getAsJsonObject().get("name").getAsString();
            String href = item.getAsJsonObject().get("href").getAsString();
            categoricalPlaylists.put(name, href);
        }
        System.out.println(categoryIDs.toString());
        System.out.println("---PLAYLISTS FOR "+category.toUpperCase()+" CATEGORY---");
        for(String playlist : categoricalPlaylists.keySet()) System.out.println(playlist);
    }

    /**
     * Returns the JsonObject of the specific Spotify playlist indicated by the user's input
     * @param name                              Name of the selected playlist
     * @return                                  JSONObject of the selected playlist
     * @throws java.io.IOException              Possible Exception while sending the HTTP Request
     * @throws java.lang.InterruptedException   Possible Exception while sending the HTTP Request
     */
    JsonObject getSpecificSpotifyPlaylist(String name) throws java.io.IOException, java.lang.InterruptedException{
        for(String playlist : categoricalPlaylists.keySet()){
            if(name.equals(playlist)){
                HttpClient client = HttpClient.newBuilder().build();
                String accessToken = getAccessToken();
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .header("Authorization", "Bearer " + accessToken)
                        .uri(URI.create(categoricalPlaylists.get(name)))
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(httpRequest,HttpResponse.BodyHandlers.ofString());
                JsonObject jsonObject =  JsonParser.parseString(response.body()).getAsJsonObject();
                return jsonObject;
            }
        }
        return null;
    }

}

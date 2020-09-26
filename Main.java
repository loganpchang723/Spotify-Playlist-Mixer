package advisor;

/**
 *The "Main" method which simply runs the application in the output screen of the IDE through the "main" method
 * @author Logan Chang (Github: loganpchang723)
 *
 * This is a project I built as I was learning about working with API's
 * This program will prompt the user, upon authorizing the program access to the user's Spotify account, to enter the name of a playlist
 * This playlist can be either a playlist created by the user or one of the playlists curated by Spotify
 * The program will swap each track in the selected playlist with either a song by the same artist or a song from the same album and create a new playlist in the user's library
 *
 * (This project was based off "Playlist Souffle" by Zach Hammer. I credit him for the inspiration and include relevant links to his work here: (Website: https://playlistsouffle.com/  ,  Showcase on Spotify Developers Webpage: https://developer.spotify.com/community/showcase/playlist-souffle/).
 * I simply used his work as inspiration, as all code written in this project is 100% original and my own genuine code)
 *
 */
public class Main {
    /**
     * The "main" method which runs the application's driver code
     * @param args          Command-line arguments to be passed when running the program
     * @throws Exception    Possible exceptions throughout the program's process as multiple HTTP requests are made
     */
    public static void main(String[] args) throws Exception {
        Application application = new Application();
        application.run();
    }
}


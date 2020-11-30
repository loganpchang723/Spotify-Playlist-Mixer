# Spotify-Playlist-Mixer
Based off Playlist Souffle on the Spotify API Spotlight website, this program will take a playlist from the user's Spotify library or one of Spotify's curated playlists and 'mix' it by changing each song with a song from the same artist or same album and saving the new playlist to your Spotify library
I find myself all too often listening to the same music over and over. I enjoy my playlists (hence why I went through all the work to make it in the first place!), but sometimes we all just want a little more spice in our lives.
While Spotify's "Make a Similar Playlist" function is awesome, it only curates one similar playlist per playlists. Similar playlists of similar playlists of similar playlists often begin to transfrom into whole new genres altogether!
That is where this program will come in handy! Without drifitng too far from my favorite tracks, this program can give me a new playlist that's just a little different, but different enough to keep me fascinated!

## Technologies

- Java 14.0.2
- Google GSON 
- Spotify API

This project uses Java and Google GSON installed via Gradle to properly handle and format API requests
This program will authroize a Spotify user's login credentials to access their account. Upon access, a console-based text menu walks through which playlist the user wants to 'mix'.

## Launch
In order to run on your own machine, all code dependencies are necessary. Google GSON must also be added as a dependency via gradle or another build automation tool. 
Compile all code files and running 'Main.java' will start the program. It will give text-based prompts in the console, all of which are case-sensitive and must be mathced word-for-word.
Upon receving a playlist selection, the program will swap each song with a song either by the same artist or from the same album.
Upon completing the playlist, the new playlist will be added to the user's library with the title: '*playlistName* BUT MIXED :) !! (*date and time of program run*)'
The program will terminate after succesfully creating and adding a playlist to the user's library.

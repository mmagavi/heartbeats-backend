package SpotifyUtilities;

import ExceptionClasses.PlaylistExceptions.*;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.requests.data.playlists.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PlaylistUtilities {

    /**
     * Create a playlist
     * @param spotify_api SpotifyApi object that has been built with the current user's access token
     * @param user_id     id of the user
     * @param user_name   name of the user
     * @return The created Playlist object
     * @throws CreatePlaylistException if an exception was encountered in createPlaylistRequest.execute()
     */
    public static Playlist createPlaylist(SpotifyApi spotify_api, String user_id, String user_name)
            throws CreatePlaylistException {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        LocalDateTime now = LocalDateTime.now();

        String current_date = formatter.format(now);

        return createPlaylist(spotify_api, user_id, user_name,
                false, "My heartBeats playlist",
                "A playlist created by heartBeats " + current_date);
    }

    /**
     * Creates a playlist
     * @param spotify_api SpotifyApi object that has been built with the current user's access token
     * @param user_id     id of the user
     * @param user_name   name of the user
     * @param is_public   determines if the created playlist should be public of private
     * @param playlist_name name of the playlist
     * @param description description of the created playlist
     * @return The created Playlist object
     * @throws CreatePlaylistException if an exception was encountered in createPlaylistRequest.execute()
     */
    public static Playlist createPlaylist(SpotifyApi spotify_api, String user_id, String user_name,
                                          boolean is_public, String playlist_name, String description)
            throws CreatePlaylistException {

        try {

            CreatePlaylistRequest createPlaylistRequest = spotify_api.createPlaylist(user_id, user_name)
                    .public_(is_public)
                    .name(playlist_name)
                    .description(description)
                    .build();

            return createPlaylistRequest.execute();

        } catch (Exception ex) {

            throw new CreatePlaylistException(ex.getMessage());
        }
    }

    /**
     * Adds items to a playlist
     * @param spotify_api SpotifyApi object that has been built with the current user's access token
     * @param playlist_id ID of the playlist
     * @param uris        Array of the track uris
     * @throws AddItemsToPlaylistException if an exception was encountered in addItemsToPlaylistRequest.execute()
     */
    public static void addItemsToPlaylist(SpotifyApi spotify_api, String playlist_id, String[] uris)
            throws AddItemsToPlaylistException {

        // need this or an error will be thrown by se.michaelthelin.spotify library
        if(uris.length == 0)
            return;

        try {
            SnapshotResult snapshotResult;

            AddItemsToPlaylistRequest addItemsToPlaylistRequest =
                    spotify_api.addItemsToPlaylist(playlist_id, uris).build();

            snapshotResult = addItemsToPlaylistRequest.execute();

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new AddItemsToPlaylistException(ex.getMessage());
        }
    }

    /**
     * Gets ALL the user's playlists
     * @param spotify_api SpotifyApi object that has been built with the current user's access token
     * @return List of PlaylistSimplified[] objects
     * @throws GetUsersPlaylistsException if exception encountered in getListOfCurrentUsersPlaylistsRequest.execute()
     */
    public static PlaylistSimplified[] getUsersPlaylists(SpotifyApi spotify_api) throws GetUsersPlaylistsException {

        try {
            Paging<PlaylistSimplified> playlists;

            GetListOfCurrentUsersPlaylistsRequest getListOfCurrentUsersPlaylistsRequest =
                    spotify_api.getListOfCurrentUsersPlaylists().build();

            playlists = getListOfCurrentUsersPlaylistsRequest.execute();

            return playlists.getItems();

        } catch (Exception ex) {

            throw new GetUsersPlaylistsException(ex.getMessage());
        }
    }

    /**
     * Gets a certain number of the user's playlists
     * @param spotify_api SpotifyApi object that has been built with the current user's access token
     * @param limit Max number of playlists desired
     * @return List of PlaylistSimplified[] objects
     * @throws GetUsersPlaylistsException if exception encountered in getListOfCurrentUsersPlaylistsRequest.execute()
     */
    public static PlaylistSimplified[] getUsersPlaylists(SpotifyApi spotify_api, int limit)
            throws GetUsersPlaylistsException {

        try {
            GetListOfCurrentUsersPlaylistsRequest getListOfCurrentUsersPlaylistsRequest =
                    spotify_api.getListOfCurrentUsersPlaylists().limit(limit).build();

            Paging<PlaylistSimplified> playlists = getListOfCurrentUsersPlaylistsRequest.execute();

            return playlists.getItems();

        } catch (Exception ex) {

            throw new GetUsersPlaylistsException(ex.getMessage());
        }
    }

    /**
     * Gets a playlist based on a playlist id
     * @param spotify_api SpotifyApi object that has been built with the current user's access token
     * @param playlist_id id of the playlist to retrieve
     * @return Playlist object that was fetched
     * @throws GetPlaylistException if exception encountered in getPlaylistRequest.execute()
     */
    public static Playlist getPlaylist(SpotifyApi spotify_api, String playlist_id) throws GetPlaylistException {


        try {
            GetPlaylistRequest getPlaylistRequest = spotify_api.getPlaylist(playlist_id).build();

            return getPlaylistRequest.execute();

        }catch (Exception ex){

            throw new GetPlaylistException(ex.getMessage());
        }
    }

    /**
     * Gets the tracks from a given playlist id
     * @param spotify_api SpotifyApi object that has been built with the current user's access token
     * @param playlist_id id of the playlist to retrieve
     * @return PlaylistTrack array of all the tracks in the playlist fetched by the given playlist id
     * @throws GetPlaylistItemsException if exception encountered in getPlaylistsItemsRequest.execute()
     */
    public static PlaylistTrack[] getPlaylistItems(SpotifyApi spotify_api, String playlist_id)
            throws GetPlaylistItemsException {

        try{
            GetPlaylistsItemsRequest getPlaylistsItemsRequest = spotify_api.getPlaylistsItems(playlist_id).build();

            Paging<PlaylistTrack> playlistTrackPaging = getPlaylistsItemsRequest.execute();

            return playlistTrackPaging.getItems();

        }catch (Exception ex){

            throw new GetPlaylistItemsException(ex.getMessage());
        }
    }

}

package Server;

import ExceptionClasses.InvalidInputExceptions.*;
import PlaylistGenerating.PlaylistTypes.Classic.GenerateClassic;
import PlaylistGenerating.PlaylistTypes.Interval.GenerateIntervalOne;
import PlaylistGenerating.PlaylistTypes.PyramidInterval.GenerateIntervalTwo;
import PlaylistGenerating.PlaylistTypes.GeneratePlaylist;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import spark.Request;
import spark.Response;
import spark.Route;

import java.lang.reflect.Type;
import java.util.*;

import static Server.Server.spotify_api;

public class GeneratePlaylistHandler implements Route {

    // Heavy inspiration from : https://github.com/spotify-web-api-java/spotify-web-api-java/blob/master/examples/
    // authorization/authorization_code/AuthorizationCodeExample.java

    static ArrayList<String> available_genres = new ArrayList<>(Arrays.asList("afrobeat", "alt-rock", "alternative", "blues",
            "brazil", "british", "chill", "classical", "club", "country", "dance", "disco", "dubstep", "edm", "emo",
            "folk", "french", "funk", "garage", "german", "grunge", "hardcore", "hip-hop", "indian", "indie",
            "indie-pop", "industrial", "j-pop", "jazz", "k-pop", "latino", "malay", "mandopop", "metal", "new-age",
            "new-release", "opera", "party", "philippines-opm", "pop", "punk", "punk-rock", "r-n-b", "rainy-day",
            "reggae", "rock", "sad", "samba", "sertanejo", "sleep", "soul", "spanish", "synth-pop", "techno",
            "work-out", "world-music"));

    /**
     * Constructor
     * No fields for now
     */
    public GeneratePlaylistHandler() {
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {

        String url = request.url();
        String playlist_id = ""; // Will be populated and returned

        try {
            verifyNonNullParameters(request);

            // Fetch all the parameters
            String access_token = request.queryParams("access_token");
            String refresh_token = request.queryParams("refresh_token");
            String playlist_type = request.queryParams("playlist_type"); // classic, interval_one, interval_two, relax
            String intensity = request.queryParams("intensity");
            String genres = request.queryParams("genres"); // british,hip-hop,country  etc
            int age = Integer.parseInt(request.queryParams("age")); // 13-100
            int workout_length = Integer.parseInt(request.queryParams("workout_length")); // 15-180 (minutes)

            // Verify all the parameters
            verifyPlaylistType(playlist_type);
            verifyIntensity(intensity);
            verifyGenre(genres);
            verifyAge(age);
            verifyWorkoutLength(workout_length);

            spotify_api.setAccessToken(access_token);
            spotify_api.setRefreshToken(refresh_token);

            GeneratePlaylist generator;

            switch (playlist_type) {
                case "classic" -> {
                    generator = new GenerateClassic(spotify_api, genres, age, workout_length, intensity);
                }
                case "interval_one" -> {
                    generator = new GenerateIntervalOne(spotify_api, genres, age, workout_length, intensity);
                }
                case "interval_two" -> {
                    generator = new GenerateIntervalTwo(spotify_api, genres, age, workout_length, intensity);
                }
//                case "relax" -> {
//                    generator = new GenerateRelax(spotify_api, genres, age, workout_length, intensity);
//                }
                default -> {
                    throw new InvalidPlaylistTypeException("playlist_type must be" +
                            " \"classic\", \"interval_one\", or \"interval_two\"");
                }
            }

            playlist_id = generator.generatePlaylist();

            return serialize("Success", url, playlist_id);

        } catch (Exception ex) {
            ex.printStackTrace();

            return serialize("Failure: " + ex.getMessage(), url, null);
        }
    }

    /**
     * Verifies all parameters were passed to the endpoint
     *
     * @param request user request to be verified
     * @throws NullParameterException if any one parameter was not provided
     */
    public static void verifyNonNullParameters(Request request) throws NullParameterException {
        if (request.queryParams("access_token") == null)
            throw new NullParameterException("Parameter \"access_token\" was not provided");

        if (request.queryParams("refresh_token") == null)
            throw new NullParameterException("Parameter \"refresh_token\" was not provided");


        if (request.queryParams("playlist_type") == null) {
            throw new NullParameterException("Parameter \"playlist_type\" was not provided");
        }

        if (request.queryParams("intensity") == null) {
            throw new NullParameterException("Parameter \"intensity\" was not provided");
        }

        if (request.queryParams("genres") == null)
            throw new NullParameterException("Parameter \"genre\" was not provided");

        if (request.queryParams("age") == null)
            throw new NullParameterException("Parameter \"age\" was not provided");

        if (request.queryParams("workout_length") == null)
            throw new NullParameterException("Parameter \"workout_length\" was not provided");
    }

    /**
     * Verifies the given playlist type is valid
     *
     * @param playlist_type given playlist type to be verified
     * @throws InvalidPlaylistTypeException if the given playlist type is not "classic", "interval_one", "interval_two"
     */
    public static void verifyPlaylistType(String playlist_type) throws InvalidPlaylistTypeException {

        if (playlist_type.equalsIgnoreCase("classic")) return;
        if (playlist_type.equalsIgnoreCase("interval_one")) return;
        if (playlist_type.equalsIgnoreCase("interval_two")) return;

        throw new InvalidPlaylistTypeException("playlist_type must be \"classic\", \"interval_one\", or \"interval_two\"");
    }

    /**
     * Verifies the given intensity is valid
     *
     * @param intensity the provided intensity to be verified
     * @throws InvalidIntensityException if intensity is not "low", "medium", or "high" (Not case-sensitive)
     */
    public static void verifyIntensity(String intensity) throws InvalidIntensityException {
        if (intensity.equalsIgnoreCase("low")) return;
        if (intensity.equalsIgnoreCase("medium")) return;
        if (intensity.equalsIgnoreCase("high")) return;

        throw new InvalidIntensityException("intensity must be \"low\", \"medium\", or \"high\"");
    }

    /**
     * Verifies the given seed genres are valid and do not exceed 5 genres in number
     *
     * @param desired_genres provided seed genres
     * @throws InvalidGenreException if more than 3 genres were provided or if the provided genres are not valid genres
     */
    public static void verifyGenre(String desired_genres) throws InvalidGenreException {

        String[] genres = desired_genres.split(",");

        if (genres.length > 3) {
            throw new InvalidGenreException("A maximum of 3 genres can be provided");
        }

        for (String genre : genres) {
            if (!available_genres.contains(genre.toLowerCase())) {
                throw new InvalidGenreException(genre + " is not a valid genre");
            }
        }
    }

    /**
     * Checks the given age and throws an exception if the age is outside the desired range
     *
     * @param age given age to be verified
     * @throws InvalidAgeException if age is outside the desired range 13-100
     */
    public static void verifyAge(int age) throws InvalidAgeException {

        if (age < 13 || age > 100) {
            throw new InvalidAgeException("Age must be greater than 12 and less than 101");
        }
    }

    /**
     * Verifies the provided workout length is within the desired range of 15-180
     *
     * @param workout_length given workout_length to be verified
     * @throws InvalidWorkoutLength if workout_length is outside the desired range
     */
    public static void verifyWorkoutLength(int workout_length) throws InvalidWorkoutLength {
        if (workout_length < 15 || workout_length > 180) {
            throw new InvalidWorkoutLength("workout_length must be in the range 15-180");
        }
    }

    /**
     * @return url, filepath, hasHeaders, and response, serialized as Json
     */
    private String serialize(String result, String url, String playlist_id) {
        try {
            // add to our response map!
            HashMap<String, Object> map = new HashMap<>();
            map.put("result", result);
            map.put("request", Objects.requireNonNullElse(url, "null"));
            map.put("playlist_id", Objects.requireNonNullElse(playlist_id, "null"));

            // .toJson our response map!
            Type responseMap = Types.newParameterizedType(Map.class, String.class, Object.class);
            Moshi moshi = new Moshi.Builder().build();
            JsonAdapter<Map<String, Object>> adapter = moshi.adapter(responseMap);

            return adapter.toJson(map);

        } catch (Exception e) {
            // For debugging purposes, show in the console _why_ this fails
            // Otherwise we'll just get an error 500 from the API in integration
            // testing.
            e.printStackTrace();
            throw e;
        }
    }
}

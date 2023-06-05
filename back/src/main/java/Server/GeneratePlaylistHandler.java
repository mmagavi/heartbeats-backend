package Server;

import ExceptionClasses.EndpointParamExceptions.NotBooleanException;
import ExceptionClasses.InvalidInputExceptions.*;
import PlaylistGenerating.PlaylistTypes.GenerateExercise;
import PlaylistGenerating.PlaylistTypes.GenerateRelax;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import spark.Request;
import spark.Response;
import spark.Route;
import java.lang.reflect.Type;
import java.util.*;

public class GeneratePlaylistHandler implements Route {

    // Heavy inspiration from : https://github.com/spotify-web-api-java/spotify-web-api-java/blob/master/examples/
    // authorization/authorization_code/AuthorizationCodeExample.java

    static ArrayList<String> available_genres = new ArrayList<>(Arrays.asList("afrobeat", "alt-rock", "alternative", "blues",
            "brazil", "british", "chill", "classical", "club", "country", "dance", "disco", "dubstep", "edm", "emo",
            "folk", "french", "funk", "garage", "german", "grunge", "hardcore", "hip-hop", "indian", "indie",
            "indie-pop", "industrial", "j-pop", "jazz", "k-pop", "latino", "malay", "mandopop", "metal", "new-age",
            "new-release", "opera", "party", "philippines-opm", "pop", "punk", "punk-rock", "r-n-b", "rainy-day",
            "reggae", "rock", "sad", "samba", "sertanejo ", "sleep", "soul", "spanish", "synth-pop", "techno",
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
        String access_token = "";
        String refresh_token = "";
        String genres = ""; // british,hip-hop,country
        String playlist_type = ""; // working_out or winding_down
        String desired_warmup = ""; // start_quickly or start_with_warmup
        String desired_cool_down = ""; // short_cool_down or long_cool_down
        int age = -1; // 13-100
        int workout_length = -1; // 15-180 (minutes)
        int current_bpm = -1; // 45-140
        String playlist_id = "";

        try {
            verifyNonNullParameters(request);

            // Fetch all the parameters
            access_token = request.queryParams("access_token");
            refresh_token = request.queryParams("refresh_token");
            genres = request.queryParams("genres");
            playlist_type = request.queryParams("playlist_type");
            desired_warmup = request.queryParams("desired_warmup");
            desired_cool_down = request.queryParams("desired_cool_down");
            age = Integer.parseInt(request.queryParams("age"));
            workout_length = Integer.parseInt(request.queryParams("workout_length"));
            current_bpm = Integer.parseInt(request.queryParams("current_bpm"));

            // Verify all the parameters
            verifyGenre(genres);
            verifyPlaylistType(playlist_type);
            verifyAge(age);
            verifyWarmup(desired_warmup);
            verifyCoolDown(desired_cool_down);
            verifyWorkoutLength(workout_length);
            verifyCurrentBPM(current_bpm);

            Server.spotify_api.setAccessToken(access_token);
            Server.spotify_api.setRefreshToken(refresh_token);

            boolean is_quick_start = desired_warmup.equalsIgnoreCase("start_quickly");
            boolean is_quick_end = desired_cool_down.equalsIgnoreCase("short_cool_down");


            if(playlist_type.equals("working_out")) {
                GenerateExercise generateExercise = new GenerateExercise(age, current_bpm, is_quick_start, is_quick_end,
                        workout_length, genres, Server.spotify_api);


                playlist_id = generateExercise.getPlaylist();
            }
            else {


                GenerateRelax generateRelax = new GenerateRelax(age, workout_length, genres, Server.spotify_api);

                playlist_id = generateRelax.getPlaylist();
            }

        } catch (Exception ex) {
            ex.printStackTrace();

            return serialize("Failure: " + ex.getMessage(), url, null);
        }

        return serialize("Success", url, playlist_id);
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

        if (request.queryParams("genres") == null)
            throw new NullParameterException("Parameter \"genre\" was not provided");

        if (request.queryParams("playlist_type") == null)
            throw new NullParameterException("Parameter \"playlist_type\" was not provided");

        if (request.queryParams("desired_warmup") == null)
            throw new NullParameterException("Parameter \"desired_warmup\" was not provided");

        if (request.queryParams("desired_cool_down") == null)
            throw new NullParameterException("Parameter \"desired_cool_down\" was not provided");

        if (request.queryParams("age") == null)
            throw new NullParameterException("Parameter \"age\" was not provided");

        if (request.queryParams("workout_length") == null)
            throw new NullParameterException("Parameter \"workout_length\" was not provided");
    }

    /**
     * Checks if the given bpm is within a valid range
     *
     * @param bpm provided bpm to be verified
     * @throws InvalidBpmException if the provided bpm is outside the allowed range (45-140) inclusive
     */
    public static void verifyCurrentBPM(int bpm) throws InvalidBpmException {
        if (bpm < 45 || bpm > 140) {
            throw new InvalidBpmException("BPM must be between the range of 45-140");
        }
    }


    /**
     * Checks if the given string is a representation of a boolean
     *
     * @param bool String to be analyzed
     * @return true if the given String equals "true", false otherwise
     * @throws NotBooleanException if the provided string is not "true" or "false" (case is ignored)
     */
    public static boolean processBoolean(String bool) throws NotBooleanException {

        if (bool.equalsIgnoreCase("true")) {
            return true;
        } else if (bool.equalsIgnoreCase("false")) {
            return false;
        } else {
            throw new NotBooleanException("Provided value \"" + bool + "\" is not a boolean");
        }
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
     * Verifies the given playlist type is valid
     *
     * @param playlist_type given playlist type to be verified
     * @throws InvalidPlaylistTypeException if the given playlist type is not "working_out" or "winding_down"
     */
    public static void verifyPlaylistType(String playlist_type) throws InvalidPlaylistTypeException {

        if (!playlist_type.equalsIgnoreCase("working_out") &&
                !playlist_type.equalsIgnoreCase("winding_down")) {
            throw new InvalidPlaylistTypeException("playlist_type must be \"working_out\" or \"winding_down\"");
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
     * Verifies the given cool_down is valid
     *
     * @param warmup given warmup type to be verified
     * @throws InvalidWarmupException if the given warmup type is not "start_quickly" or "start_with_warmup"
     */
    public static void verifyWarmup(String warmup) throws InvalidWarmupException {

        if (!warmup.equalsIgnoreCase("start_quickly") &&
                !warmup.equalsIgnoreCase("start_with_warmup")) {
            throw new InvalidWarmupException("Warmup must be \"start_quickly\" or \"start_with_warmup\"");
        }

    }

    /**
     * Verifies the given cool_down is valid
     *
     * @param cool_down given cool_down type to be verified
     * @throws InvalidCoolDownException if the given playlist type is not "short_cool_down" or "long_cool_down"
     */
    public static void verifyCoolDown(String cool_down) throws InvalidCoolDownException {

        if (!cool_down.equalsIgnoreCase("short_cool_down") &&
                !cool_down.equalsIgnoreCase("long_cool_down")) {
            throw new InvalidCoolDownException("Cool down must be \"short_cool_down\" or \"long_cool_down\"");
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

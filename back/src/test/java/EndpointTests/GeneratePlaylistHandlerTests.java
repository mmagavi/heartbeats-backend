package EndpointTests;

import Server.GeneratePlaylistHandler;
import Server.LoginHandler;
import Server.UserCodeRegistrationHandler;
import org.junit.jupiter.api.*;
import spark.Spark;
import java.io.IOException;

import static EndpointTests.EndpointTestingUtilities.makeRequest;
import static spark.Spark.after;


public class GeneratePlaylistHandlerTests {

    // Some dummy variables with valid args to pass to endpoint
    String access_token = "access_token";
    String refresh_token = "refresh_token";
    String genres = "hip-hop,chill,british";
    String playlist_type = "working_out"; // working_out or winding_down
    String desired_warmup = "start_quickly";
    String desired_cool_down = "short_cool_down"; // short_cool_down or long_cool_down
    String age = "69";
    String workout_length = "120"; // In minutes
    String current_bpm = "69"; // 45-140

    @BeforeAll
    public static void initialSetup(){
        Spark.port(3232);
    }

    @BeforeEach
    public void setup() {

        after((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "*");
        });

        Spark.get("generate-playlist", new GeneratePlaylistHandler());
        Spark.get("login", new LoginHandler());
        Spark.get("register-user-code", new UserCodeRegistrationHandler());
        Spark.init();
        Spark.awaitInitialization();
    }

    @AfterEach
    public void teardown() {
        // Gracefully stop Spark listening on both endpoints
        Spark.unmap("generate-playlist");
        Spark.unmap("login");
        Spark.unmap("register-user-code");
        Spark.awaitStop(); // don't proceed until the server is stopped
    }

    @Test
    public void passingInvalidGenreReturnsFailureResponse() throws IOException {

        String response = makeRequest(
                "generate-playlist?access_token=" + access_token
                        + "&refresh_token=" + refresh_token
                        + "&genres=" + "Tomatoes"
                        + "&playlist_type=" + playlist_type
                        + "&desired_warmup=" + desired_warmup
                        + "&desired_cool_down=" + desired_cool_down
                        + "&age=" + age
                        + "&workout_length=" + workout_length
                        + "&current_bpm=" + current_bpm).responseMap().toString();

        String expected = "{result=Failure: Tomatoes is not a valid genre," +
                " request=http://localhost:3232/generate-playlist, playlist_id=null}";

        Assertions.assertEquals(expected, response);
    }

    @Test
    public void passingInvalidPlaylistTypeReturnsFailureResponse() throws IOException {

        String response = makeRequest(
                "generate-playlist?access_token=" + access_token
                        + "&refresh_token=" + refresh_token
                        + "&genres=" + genres
                        + "&playlist_type=" + "Invalid"
                        + "&desired_warmup=" + desired_warmup
                        + "&desired_cool_down=" + desired_cool_down
                        + "&age=" + age
                        + "&workout_length=" + workout_length
                        + "&current_bpm=" + current_bpm).responseMap().toString();

        String expected = "{result=Failure: playlist_type must be \"working_out\" or \"winding_down\"," +
                " request=http://localhost:3232/generate-playlist, playlist_id=null}";

        Assertions.assertEquals(expected, response);
    }

    @Test
    public void passingInvalidWarmupReturnsFailureResponse() throws IOException {

        String response = makeRequest(
                "generate-playlist?access_token=" + access_token
                        + "&refresh_token=" + refresh_token
                        + "&genres=" + genres
                        + "&playlist_type=" + playlist_type
                        + "&desired_warmup=" + "Invalid"
                        + "&desired_cool_down=" + desired_cool_down
                        + "&age=" + age
                        + "&workout_length=" + workout_length
                        + "&current_bpm=" + current_bpm).responseMap().toString();

        String expected = "{result=Failure: Warmup must be \"start_quickly\" or \"start_with_warmup\"," +
                " request=http://localhost:3232/generate-playlist, playlist_id=null}";

        Assertions.assertEquals(expected, response);
    }

    @Test
    public void passingInvalidCoolDownReturnsFailureResponse() throws IOException {

        String response = makeRequest(
                "generate-playlist?access_token=" + access_token
                        + "&refresh_token=" + refresh_token
                        + "&genres=" + genres
                        + "&playlist_type=" + playlist_type
                        + "&desired_warmup=" + desired_warmup
                        + "&desired_cool_down=" + "Invalid"
                        + "&age=" + age
                        + "&workout_length=" + workout_length
                        + "&current_bpm=" + current_bpm).responseMap().toString();

        String expected = "{result=Failure: Cool down must be \"short_cool_down\" or \"long_cool_down\"," +
                " request=http://localhost:3232/generate-playlist, playlist_id=null}";

        Assertions.assertEquals(expected, response);
    }

    @Test
    public void passingInvalidAgeReturnsFailureResponse() throws IOException {

        String response = makeRequest(
                "generate-playlist?access_token=" + access_token
                        + "&refresh_token=" + refresh_token
                        + "&genres=" + genres
                        + "&playlist_type=" + playlist_type
                        + "&desired_warmup=" + desired_warmup
                        + "&desired_cool_down=" + desired_cool_down
                        + "&age=" + "169"
                        + "&workout_length=" + workout_length
                        + "&current_bpm=" + current_bpm).responseMap().toString();

        String expected = "{result=Failure: Age must be greater than 12 and less than 101," +
                " request=http://localhost:3232/generate-playlist, playlist_id=null}";

        Assertions.assertEquals(expected, response);
    }

    @Test
    public void passingInvalidWorkoutLengthReturnsFailureResponse() throws IOException {

        String response = makeRequest(
                "generate-playlist?access_token=" + access_token
                        + "&refresh_token=" + refresh_token
                        + "&genres=" + genres
                        + "&playlist_type=" + playlist_type
                        + "&desired_warmup=" + desired_warmup
                        + "&desired_cool_down=" + desired_cool_down
                        + "&age=" + age
                        + "&workout_length=" + "188"
                        + "&current_bpm=" + current_bpm).responseMap().toString();

        String expected = "{result=Failure: workout_length must be in the range 15-180," +
                " request=http://localhost:3232/generate-playlist, playlist_id=null}";

        Assertions.assertEquals(expected, response);
    }

    @Test
    public void passingInvalidBpmReturnsFailureResponse() throws IOException {

        String response = makeRequest(
                "generate-playlist?access_token=" + access_token
                        + "&refresh_token=" + refresh_token
                        + "&genres=" + genres
                        + "&playlist_type=" + playlist_type
                        + "&desired_warmup=" + desired_warmup
                        + "&desired_cool_down=" + desired_cool_down
                        + "&age=" + age
                        + "&workout_length=" + workout_length
                        + "&current_bpm=" + "420").responseMap().toString();

        String expected = "{result=Failure: BPM must be between the range of 45-140," +
                " request=http://localhost:3232/generate-playlist, playlist_id=null}";

        Assertions.assertEquals(expected, response);
    }


    //TODO: This will return an array of song playlist_id once that functionality is implemented, this test needs to reflect that
    @Test
    public void passingValidArgsReturnsSuccessResponse() throws IOException{
        String response = makeRequest(
                "generate-playlist?access_token=" + access_token
                        + "&refresh_token=" + refresh_token
                        + "&genres=" + genres
                        + "&playlist_type=" + playlist_type
                        + "&desired_warmup=" + desired_warmup
                        + "&desired_cool_down=" + desired_cool_down
                        + "&age=" + age
                        + "&workout_length=" + workout_length
                        + "&current_bpm=" + current_bpm).responseMap().toString();

        String expected = "{result=Success, request=http://localhost:3232/generate-playlist, playlist_id=null}";

        Assertions.assertEquals(expected, response);
    }


}

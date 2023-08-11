package Server;

import static spark.Spark.after;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import spark.Spark;

import java.net.URI;

/**
 * Top-level class for this demo. Contains the main() method which starts Spark and runs the various handlers.
 *
 * We have two endpoints in this demo. They need to share state (a menu).
 * This is a great chance to use dependency injection, as we do here with the menu set. If we needed more endpoints,
 * more functionality classes, etc. we could make sure they all had the same shared state.
 */
public class Server {

    private static final String client_id = "c48e6da922b24fb782bb5d83356b247f";
    private static final String client_secret = "0420a04c99804dc8b7b681b56a813495";
    protected static final URI redirectUri = SpotifyHttpManager.makeUri("https://dalton-simonson.github.io/heartBeats-Front#/music");

    public static String code = "";

    public static final SpotifyApi spotify_api = new SpotifyApi.Builder()
            .setClientId(client_id)
            .setClientSecret(client_secret)
            .setRedirectUri(redirectUri)
            .build();

    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 5173; // Change this to the port number you want to use locally
    }

    public static void main(String[] args) {

//        System.out.println("Num Args: " + args.length);
//
//        for (String arg : args) {
//            System.out.println(arg);
//        }

        int port = Integer.parseInt(args[0]);


        Spark.port(port);

        /*
            Setting CORS headers to allow cross-origin requests from the client; this is necessary for the client to
            be able to make requests to the server.

            By setting the Access-Control-Allow-Origin header to "*", we allow requests from any origin.
            This is not a good idea in real-world applications, since it opens up your server to cross-origin requests
            from any website. Instead, you should set this header to the origin of your client, or a list of origins
            that you trust.

            By setting the Access-Control-Allow-Methods header to "*", we allow requests with any HTTP method.
            Again, it's generally better to be more specific here and only allow the methods you need, but for
            this demo we'll allow all methods.

            We recommend you learn more about CORS with these resources:
                - https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS
                - https://portswigger.net/web-security/cors
         */
        after((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "*");
        });

        // Setting up the handlers for the endpoints
        Spark.get("generate-playlist", new GeneratePlaylistHandler());
        Spark.get("login", new LoginHandler());
        Spark.get("register-user-code", new UserCodeRegistrationHandler());
        Spark.init();
        Spark.awaitInitialization();
        System.out.println("Server started");
    }
}
package Server;

import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import spark.Request;
import spark.Response;
import spark.Route;

import java.net.URI;

public class LoginHandler implements Route {

    private  String scope =
            "playlist-read-private, playlist-read-collaborative, playlist-modify-private, playlist-modify-public," +
                    " user-read-private, user-read-email, user-top-read, user-read-recently-played," +
                    " user-library-modify, user-library-read, user-follow-read";

    public LoginHandler(){

    }

    @Override
    public Object handle(Request request, Response response) throws Exception {

        AuthorizationCodeUriRequest authorizationCodeUriRequest =
                Server.spotify_api.authorizationCodeUri()
                        .scope(scope)
                        .show_dialog(true)
                        .build();

        final URI uri = authorizationCodeUriRequest.execute();

        return uri.toString();
    }
}

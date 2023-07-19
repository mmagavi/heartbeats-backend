package Server;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import spark.Request;
import spark.Response;
import spark.Route;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserCodeRegistrationHandler implements Route {

    public UserCodeRegistrationHandler(){

    }

    @Override
    public Object handle(Request request, Response response) throws Exception {

        String url = request.url();
        String code = request.queryParams("code");

        Server.code = code;

        String access_token = "";
        String refresh_token = "";

        AuthorizationCodeRequest authorizationCodeRequest = Server.spotify_api.authorizationCode(code).build();
        try {

            final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();

            access_token = authorizationCodeCredentials.getAccessToken();
            refresh_token = authorizationCodeCredentials.getRefreshToken();

            //System.out.println("Access token: " + access_token);
            //System.out.println("Refresh token: " + refresh_token);

        }catch (Exception ex){
            ex.printStackTrace();
        }

        return serialize(url, access_token, refresh_token);
    }

    /**
     * @return url, filepath, hasHeaders, and response, serialized as Json
     */
    private String serialize(String url, String access_token, String refresh_token) {
        try {
            // add to our response map!
            HashMap<String, Object> map = new HashMap<>();
            map.put("request", url);
            map.put("access_token", Objects.requireNonNullElse(access_token, "null"));
            map.put("refresh_token", Objects.requireNonNullElse(refresh_token, "null"));

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
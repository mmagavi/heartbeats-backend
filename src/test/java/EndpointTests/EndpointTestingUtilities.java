package EndpointTests;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import okio.Buffer;
import spark.Spark;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class EndpointTestingUtilities {
    /**
     * Helper to start a connection to a specific API endpoint/params
     * @param endpoint given endpoint with which to make a connection
     * @return the connection for the given URL, just after connecting
     * @throws IOException if the connection fails for some reason
     */
    static private HttpURLConnection tryRequest(String endpoint) throws IOException {
        // Configure the connection (but don't actually send the request yet)
        URL requestURL = new URL("http://localhost:3232/"+ endpoint);
        HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();

        clientConnection.connect();
        return clientConnection;
    }

    /**
     * Helper to make a request to the load endpoint and see results!
     * @param endpoint the endpoint to send a request to
     * @return a TestLoadResponse record, containing our apiCall and the response
     * and status code we got back
     * @throws IOException when working with HttpURLConnection or when deserializing
     */
    static public TestResponse makeRequest(String endpoint) throws IOException {
        // source for code: https://github.com/cs0320-s2023/sprint-3-ezhang29-mma32/
        // try request!
        HttpURLConnection con = tryRequest(endpoint);

        // read response!
        String responseString;
        try (Buffer buffer = new Buffer()) {

            int responseCode = con.getResponseCode();

            // System.out.println("Response code: " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String inputLine;
            StringBuilder builder = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                builder.append(inputLine);
            }

            in.close();
            responseString = builder.toString();

            con.disconnect();

            // read our response as List, then as a map!
            Type responseMap = Types.newParameterizedType(Map.class, String.class, Object.class);
            Moshi moshi = new Moshi.Builder().build();
            JsonAdapter<Map<String, Object>> adapter = moshi.adapter(responseMap);
            Map<String, Object> map = adapter.fromJson(responseString);

            // return!
            return new TestResponse(endpoint, map);
        }
    }

    /**
     * A record containing the apiCall we made, the statusCode we got back, and
     * the responseMap we got back
     * @param endpoint - the endpoint that was called
     * @param responseMap - the json of our response
     */
    public record TestResponse (String endpoint, Map<String, Object> responseMap) {}
}

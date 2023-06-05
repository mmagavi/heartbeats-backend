import { server } from "./App";

/**
 * Make a request to a url via fetch, await for it, then grab the json if its there
 * and put it in a nice map!
 * @param url - url to make request to
 * @returns a promise resolving to a Map containing 1 key SUCCESS or ERROR, with 
 * details for each as the value
 */
async function makeRequest(url: string): Promise<Map<string, string|object>> {
    // Simple GET request using fetch
    // make empty mape for responseMap
    var responseMap: Map<string, string> = new Map();
    try {
        console.log(`requesting ${server + url}`);
        // fetch and wait
        const response = await fetch(server + url);
        try {
            // fetch worked! let's grab json!
            const data = await response.json();
            responseMap.set("SUCCESS", data);
        } catch (error) {
            // couldn't json!
            responseMap.set("ERROR", `didn't recieve JSON, "error = ${error}"`);
        }
    } catch (error) {
        // if fetch failed, we failed to make request!
        responseMap.set("ERROR", `failed to request from "${server + url}", error = "${error}"`);
    }
    // resolve to responseMap
    return new Promise((resolve) => resolve(responseMap));
}

/**
 * checkResponse checks our server's response
 * @param response the response we get from makeRequest() - a map containing
 * 1 key SUCCESS or ERROR, with details for each as the value
 * @returns either a string indicating failure or a Map representing the JSON
 * response!
 */
async function checkResponse(response: Map<string, string|object>): Promise<string | Map<string, string>> {
    let output: Promise<string | Map<string, string>> = new Promise(async (resolve) => {
        if (response.has("ERROR")) {
            // check for errors!
            resolve(`${response.get("ERROR")}`);
        } else if (response.has("SUCCESS")) {
            // check for success!
            const response_success: string | object | undefined = response.get("SUCCESS");
            if (typeof(response_success) === "object") {
                // type is object! what we want :)
                // turn it into dict and look for result!
                const resultMap: Map<string, string> = jsonObjectToMap(response_success);
                resolve(resultMap);
            } else {
                // type is not object! :(
                resolve(`response was SUCCESS but value was not object: ${response.get("SUCCESS")}`);                    
            }
        }
    });
    return output;
}

/**
 * Function to turn our JSON objects to string maps!
 * @param object
 */
function jsonObjectToMap(object: Object): Map<string, string> {
    // grab keys and values and make a dict!
    const resultKeys = Object.keys(object);
    const resultVals = Object.values(object);
    var resultMap: Map<string, string> = new Map();
    for (var i = 0; i < resultKeys.length; i++) {
        resultMap.set(resultKeys[i], resultVals[i])
    }
    return resultMap;
}

/**
 * Ping the server and check if we get a code!
 * @param setStatus function to set status after pinging
 */
function ping(setStatus: (status: string) => void) {
    // fetch no endpoint on server
    fetch(server)
        .then(
            // if we successfully get a response, we are online!
            success => setStatus("Online :)"),
            // if we fail to connect, we are offline!
            failure => {console.log(`ERROR: failed to ping ${server}`); setStatus("Offline :(")});
}

export { makeRequest, ping, checkResponse, jsonObjectToMap}
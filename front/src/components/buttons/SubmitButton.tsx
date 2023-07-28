import { SubmitButton_AriaLabel, SubmitButton_Role } from "../../accessibility/Aria";
import { checkResponse, makeRequest } from "../../requests";

/**
 * Props for submit Button
 * data - selection data to send to backend
 */
interface SubmitButtonProps {
    userCode : String; // access code
    genres : String;
    playlist_type : String;
    desired_intensity : String;
    age : number;
    workout_length: number;
    current_bpm: number;
    setResultsPage: (b : boolean) => void;
    setPlaylistID: (id : string | undefined) => void;
    setPlaylistType: (type: string) => void
}

interface ServerResponse{
    data: JSON
  }
  
  export function isServerResponse(geoJSON: any) : geoJSON is ServerResponse{
  
  if(geoJSON === undefined) return false;
//   if (!("access_token" in geoJSON)) return false;
//   if (!("refresh_token" in geoJSON)) return false;
//   if (!("request" in geoJSON)) return false;
  
  return true
  }

  let access_token : string | undefined = "";
  let refresh_token : string | undefined = "";

/**
 * Submit button component. Handles sending data to the createPlaylist endpoint
 * and then redirect to the results page.
 * @param props data to submit
 * @constructor
 */
function SubmitButton(props: SubmitButtonProps) {

    // log information & make api call
    async function logInfo() {

        let raw_args = window.location.search;
        let params = new URLSearchParams(raw_args);

        if(access_token === "" && refresh_token === ""){

            const token_response : string | Map<string, string> =
             await checkResponse(await makeRequest("register-user-code?code=" + params.get("code")))

            if (token_response instanceof Map) {
                access_token = token_response.get("access_token");
                refresh_token = token_response.get("refresh_token");
            }
        }
    
        let et = "";

        if(props.playlist_type === "exercise"){
            et = "working_out";
        }

        let playlist_request: string ="generate-playlist?access_token=" + access_token
        + "&refresh_token=" + refresh_token
        + "&playlist_type=" + props.playlist_type
        + "&intensity=" + props.desired_intensity
        + "&genres=" + props.genres
        + "&age=" + props.age
        + "&workout_length=" + props.workout_length

        console.log(props.playlist_type)

        const playlist_response: string | Map<string, string> =
        await checkResponse(await makeRequest(playlist_request))
        console.log(playlist_request)

        let playlist_id : string | undefined

        if (playlist_response instanceof Map) {
            playlist_id = playlist_response.get("playlist_id");

            props.setPlaylistID(playlist_id)
        }

        // Redirect to result page
        props.setResultsPage(true);
    }

    // return component!
    return (
        <button className="formSubmitButton" role={SubmitButton_Role} aria-label={SubmitButton_AriaLabel} tabIndex={0} onClick={logInfo}>
            Submit
        </button>
    )
}

export { SubmitButton }
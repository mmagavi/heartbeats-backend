import { SubmitButton_AriaLabel, SubmitButton_Role } from "../../accessibility/Aria";
import { checkResponse, makeRequest } from "../../requests";

interface MockSubmitButtonProps {
    userCode : String; // access code
    genres : String;
    playlist_type : String;
    desired_warmup : String;
    desired_cool_down : String;
    age : number;
    workout_length: number;
    current_bpm: number;
    setResultsPage: (b : boolean) => void;
    setPlaylistID: (id : string | undefined) => void;
    setPlaylistType: (type: string) => void
}

/**
 * MOCK Submit button component. Fakes playlist ID & redirects
 * and then redirect to the results page.
 * @param props data to submit
 * @constructor
 */
function MockSubmitButton(props : MockSubmitButtonProps) {

    // log information & make api call
    async function logInfo() {
        // set playlist ID
        props.setPlaylistID("playlist_id");
        // redirect page
        props.setResultsPage(true);
    }

    // return component!
    return (
        <div>
            <div>Values to submit are: </div>
            <div >{props.genres}</div>
            <div>{props.playlist_type}</div>
            <div>{props.desired_warmup}</div>
            <div>{props.desired_cool_down}</div>
            <div>{props.age}</div>
            <div>{props.workout_length}</div>
            <div>{props.current_bpm}</div>
        <button className="submitButton" role={SubmitButton_Role} aria-label={SubmitButton_AriaLabel} tabIndex={0} onClick={logInfo}>
            Submit
        </button>
        </div>
    )
}

export { MockSubmitButton }
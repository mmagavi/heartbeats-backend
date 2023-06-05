import {AboutText_Role, AboutText_AriaLabel} from "../accessibility/Aria";

/**
 * Props for About Page component.
 * status - should we display this page?
 */
interface aboutPageProps {
    status : boolean;
}

/**
 * AboutPage component. Describes the web app briefly
 * @param props - status: should we display this page?
 * @constructor
 */
function AboutPage(props : aboutPageProps){

    //if status is true, render div. else return empty div
    if (props.status){
        return (
            <div className="aboutBody" role={AboutText_Role} aria-label={AboutText_AriaLabel}>
                Welcome to heartBeats! This application is designed to generate a workout or wind-down playlist
                for you based on your heartbeat. Research has shown that gradually increasing the BPM of the music
                you are listening too can increase your heart rate and lead to a more productive and successful workout.
                HeartBeats is designed to help users wind down and relax or stay motivated to run!
                <br/><br/>
                We require users to sign in with Spotify so that we can create playlists tailored to your preferences.
                None of this information will be saved after you've logged out or closed the window.
            </div>)
    } else {
        return (<div/>)
    }
}

export {AboutPage}
import {AboutText_Role, AboutText_AriaLabel} from "../accessibility/Aria";

/**
 * AboutPage component. Describes the web app briefly
 * @constructor
 */
function AboutPage(){
    return (
        <div className="aboutBody" role={AboutText_Role} aria-label={AboutText_AriaLabel}>
            Welcome to heartBeats! Our application is designed to generate a workout or wind-down playlist
            for you based on your ideal heartbeat. Research has shown that gradually increasing the BPM of the music
            you are listening to can increase your heart rate and lead to a more productive and successful workout.
            HeartBeats is designed to help users wind down and relax or stay motivated to run!
            <br/><br/>
            We ask users to sign in with Spotify so that we can create playlists tailored to your preferences.
            Don't worry, none of your information is saved after you've logged out or closed the window :)
        </div>)
}

export {AboutPage}
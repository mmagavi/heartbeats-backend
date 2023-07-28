import {AboutText_Role, AboutText_AriaLabel} from "../accessibility/Aria";

/**
 * AboutPage component. Describes the web app briefly
 * @constructor
 */
function AboutPage(){
    return (
        <div className="aboutBody" role={AboutText_Role} aria-label={AboutText_AriaLabel}>
                <div className="aboutDiv">
                    <div className="aboutHeader"> Our mission </div><br/>
                    Welcome to heartBeats! Our application is designed to generate a workout or wind-down playlist
                    for you based on your ideal heartbeat. Research has shown that gradually increasing the BPM of the music
                    you are listening to can increase your heart rate and lead to a more productive and successful workout.
                    HeartBeats is designed to help users wind down and relax or stay motivated to run!
                </div>
            <div className="aboutDiv">
                    <div className="aboutHeader"> Who we are </div><br/>
                    heartBeats was originally created by a team of four students at Brown University for CSCI0320: Introduction to Software Engineering.
                    It has been extended for widespread use by two of the original creators, and is currently available completely free of charge!
                    This is the first trial version, so please feel free to reach out and send us any feedback at our email below.
                <a href="mailto:heartbeats.feedback@gmail.com?subject=Feedback" className="aboutEmail">heartbeats.feedback@gmail.com</a>
            </div>
            <div className="aboutDiv">
                <div className="aboutHeader"> Our data storage policy </div><br/>
                    We require users to sign in with Spotify so that we can create playlists tailored to your preferences.
                    None of this information is once after you've logged out or closed the window :)
                    You may need to sign in again if you are using our application for over an hour, as Spotify access tokens are only valid for 60 minutes.
            </div>
        </div>)
}

export {AboutPage}
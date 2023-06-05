import {LoginButton} from "../components/LoginButton";
import React, {useState} from "react";
import HomePage from "./HomePage";
import {LoginPage_AriaLabel} from "../accessibility/Aria";

/**
 * Props for LoginPage -
 * status: should we display this page right now?
 * userID: stored user access token
 * setUserID: update access code
 * loggedIn: is the user currently logged in?
 * setLoggedIn: set loggedIn status
 */
interface loginPageProps {
    status : boolean;
    userID : string;
    setUserID: (status: string) => void;
    loggedIn : boolean;
    setLoggedIn: (status: boolean) => void;
}

/**
 * LoginPage Component - features a button for user to log into spotify,
 * collects user access code on redirect to window & stores it
 * Then redirects the user to HomePage component to answer questions and
 * get a playlist
 * @param props - loginPageProps described above :)
 * @constructor
 */
function LoginPage(props : loginPageProps){
    // Should probably init this in APP.TSX so can access when deciding to render the music page ...?
    const [playlistID, setPlaylist] = useState("");

    // if window href includes code, redirect to HomePage
    if (props.status && window.location.href.includes("code=")){
        props.setLoggedIn(true);
        props.setUserID(window.location.href.substr(32));
        return(
            <HomePage status={true}
                      playlist={playlistID}
                      setPlaylist={(x: string) => setPlaylist(x)}
                      loggedIn={props.loggedIn}
                      setLoggedIn={props.setLoggedIn}
                      userID={props.userID}
                      setUserID={props.setUserID}
            />
        )
        // else if not logged in and status is true, display login body
    } else if (props.status && !props.loggedIn) {
        return (
            <div className="loginBody" aria-label={LoginPage_AriaLabel}>
                <div className="subtitle">Welcome to heartBeats!</div>
                <LoginButton setID={props.setUserID} setLI={props.setLoggedIn}/>
                <div className="subtext">Please login to your Spotify account so that we can find songs from your favorites and create custom playlists :)</div>
            </div>)
    } // else if status is false display nothing
    else {
        return (
            <div/>
        )
    }
}

export {LoginPage}
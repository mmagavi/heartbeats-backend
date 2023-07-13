import {LoginButton} from "../components/buttons/LoginButton";
import React from "react";
import {LoginPage_AriaLabel} from "../accessibility/Aria";

/**
 * Props for LoginPage -
 * status: should we display this page right now?
 * userCode: stored user access token
 * setUserCode: update access code
 * loggedIn: is the user currently logged in?
 * setLoggedIn: set loggedIn status
 */
interface loginPageProps {
    userCode : string;
    setUserCode: (status: string) => void;
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

    return (
        <div className="loginBody" aria-label={LoginPage_AriaLabel}>
            <div className="subtitle">💌&nbsp; Welcome to heartBeats!</div>
            <div className="subtext">Please login to your Spotify account so that we can find songs from your favorites and create custom playlists :)</div>
            <LoginButton setCode={props.setUserCode} setLI={props.setLoggedIn}/>
        </div>)
}

export {LoginPage}
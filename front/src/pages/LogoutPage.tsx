import React, {useState} from "react";
import {LogoutButton} from "../components/LogoutButton";
import {LoginPage} from "./LoginPage";
import {LogoutPage_AriaLabel} from "../accessibility/Aria";

/**
 * Props for LogoutPage -
 * status: should we display this page right now?
 * userID: stored user access token
 * setUserID: update access code
 * loggedIn: is the user currently logged in?
 * setLoggedIn: set loggedIn status
 */
interface logoutPageProps {
    status : boolean;
    userID : string;
    setUserID: (status: string) => void;
    loggedIn : boolean;
    setLoggedIn: (status: boolean) => void;
}

/**
 * LogoutPage component - contains a button for user to log out of their spotify
 * @param props logoutPageProps described above :)
 * @constructor
 */
function LogoutPage(props : logoutPageProps){
    // Should probably init this in APP.TSX so can access when deciding to render the music page ...?
    const [playlistID, setPlaylist] = useState("");

    // if status is true and user is not logged in, display login page.
    if (props.status && !props.loggedIn) {
        return (
            <LoginPage
                status={true}
                userID={props.userID}
                setUserID={props.setUserID}
                loggedIn={props.loggedIn}
                setLoggedIn={props.setLoggedIn}
            />)
    }
    // if status is true and user is logged in, display logout page
    else if (props.status && props.loggedIn) {
        return (
            <div className="loginBody" aria-label={LogoutPage_AriaLabel}>
                <br/><br/>
                You are logged in!
                <br/><br/>
                <LogoutButton setID={props.setUserID} setLI={props.setLoggedIn}/>
            </div>
        )
        // else if status is false return nothing
    } else {
        return (
            <div/>
        )
    }
}

export {LogoutPage}
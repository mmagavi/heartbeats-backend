import React, {useState} from "react";
import {LogoutButton} from "../components/buttons/LogoutButton";
import {LogoutPage_AriaLabel} from "../accessibility/Aria";

/**
 * Props for LogoutPage -
 * status: should we display this page right now?
 * userCode: stored user access token
 * setUserCode: update access code
 * loggedIn: is the user currently logged in?
 * setLoggedIn: set loggedIn status
 */
interface logoutPageProps {
    userCode : string;
    setUserCode: (status: string) => void;
    loggedIn : boolean;
    setLoggedIn: (status: boolean) => void;
}

/**
 * LogoutPage component - contains a button for user to log out of their spotify
 * @param props logoutPageProps described above :)
 * @constructor
 */
function LogoutPage(props : logoutPageProps){

    return (
        <div className="logoutBody" aria-label={LogoutPage_AriaLabel}>
            <br/><br/>
            Are you sure you would like to log out?
            <br/><br/>
            <LogoutButton setCode={props.setUserCode} setLI={props.setLoggedIn}/>
        </div>
    )
}

export {LogoutPage}
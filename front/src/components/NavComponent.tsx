import {Link} from "react-router-dom";
import React from "react";
import {AboutNav_AriaLabel, LoginNav_AriaLabel, LogoutNav_AriaLabel, MusicNav_AriaLabel} from "../accessibility/Aria";

/**
 * Props for navigation component
 */
interface NavComponentProps {
    loggedIn: boolean;
}

/**
 * Nav Component - renders the nav bar with either login or logout button
 * Based on props.loggedIn
 * @param props  loggedIn - is user currently logged in?
 * @constructor
 */
export function NavComponent(props: NavComponentProps) {
    // if user is logged in
    if (props.loggedIn) {
        return (
            <div className="navContainer">
                <Link className="navLink" to="music" aria-label={MusicNav_AriaLabel}>find music</Link>
                <Link className="navLink" to="about" aria-label={AboutNav_AriaLabel}>about</Link>
                <Link className="navLink" to="logout" aria-label={LogoutNav_AriaLabel}>logout</Link>
            </div>
        )
    } else {
        // if user is not logged in
        return (
            <div className="navContainer">
                <Link className="navLink" to="about" aria-label={AboutNav_AriaLabel}>about</Link>
                <Link className="navLink" to="login" aria-label={LoginNav_AriaLabel}>login</Link>
            </div>
        )
    }
}
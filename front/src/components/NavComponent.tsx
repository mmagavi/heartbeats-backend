import {Link} from "react-router-dom";
import React from "react";

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
            <div className="nav-container">
                <Link className="nav-link" to="music">find music</Link>
                <Link className="nav-link" to="about">about</Link>
                <Link className="nav-link" to="logout">logout</Link>
            </div>
        )
    } else {
        // if user is not logged in
        return (
            <div className="nav-container">
                <Link className="nav-link" to="about">about</Link>
                <Link className="nav-link" to="login">login</Link>
            </div>
        )
    }
}
import React, { useState } from "react";
import "../styles/App.css";
import Header from "./components/Header";
import Navbar from "react-bootstrap/Navbar";
import { Button } from "react-bootstrap";
import {LoginPage} from "./pages/LoginPage";
import {AboutPage} from "./pages/AboutPage";
import HomePage from "./pages/HomePage";
import MockHomePage from "./pages/MockHomePage";
import {LogoutPage} from "./pages/LogoutPage";
import {MusicNav_AriaLabel, AboutNav_AriaLabel, LogoutNav_AriaLabel} from "./accessibility/Aria";

/**
 * TODO: Link login page & about
 * Get value from slider & link to music box
 * Make other buttons store value which can be passed into the generate playlist button
 */

// hostname of our server!
export const server = "http://localhost:3232/";

/**
 * Component that holds most of the content of the page
 * ALMOST ENTIRELY THE SAME AS APP EXCEPT IT CALLS MOCK HOME PAGE WHICH CALLS MOCK SUBMIT BUTTON
 * @returns the website's entire JSX, including Header, and currently displayed page
 */
function MockApp() {
    const [currPage, setCurr] = useState("Login");
    const [userCode, setUserCode] = useState("");
    const [playlistID, setPlaylist] = useState("");
    const [loggedIn, setLoggedIn] = React.useState(false);

    function selectHomePage() {
        setCurr("Home");
    }
    function selectAboutPage() {
        setCurr("About");
    }
    function selectLogoutPage() {
        setCurr("Logout");
    }

    // render our page!
    return (
        <div className="main">
            <Navbar fixed="top" className="header">
                <Navbar.Brand>
                    <Header />
                </Navbar.Brand>
                <div className="nav-container">
                    <Button
                        className="nav-elem"
                        href="#nav-music"
                        onClick={(e) => selectHomePage()}
                        aria-label={MusicNav_AriaLabel}
                    >
                        find music
                    </Button>
                    <Button
                        className="nav-elem"
                        href="#nav-about"
                        onClick={(e) => selectAboutPage()}
                        aria-label={AboutNav_AriaLabel}
                    >
                        about
                    </Button>
                    <Button
                        className="nav-elem"
                        href="#nav-login"
                        onClick={(e) => selectLogoutPage()}
                        aria-label={LogoutNav_AriaLabel}
                    >
                        logout
                    </Button>
                </div>
            </Navbar>
            <LoginPage
                userCode={userCode}
                setUserCode={setUserCode}
                loggedIn={loggedIn}
                setLoggedIn={setLoggedIn}
            />
            <MockHomePage
                playlist={playlistID}
                setPlaylist={(x: string) => setPlaylist(x)}
                loggedIn={loggedIn}
                setLoggedIn={setLoggedIn}
                userCode={userCode}
                setUserCode={setUserCode}
            />
            <AboutPage/>
            <LogoutPage userCode={userCode} setUserCode={setUserCode} loggedIn={loggedIn} setLoggedIn={setLoggedIn}/>
        </div>
    );
}

export default MockApp;
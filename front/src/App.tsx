import React, { useState } from "react";
import "../styles/App.css";
import Header from "./components/Header";
import Navbar from "react-bootstrap/Navbar";
import {LoginPage} from "./pages/LoginPage";
import {AboutPage} from "./pages/AboutPage";
import HomePage from "./pages/HomePage";
import {LogoutPage} from "./pages/LogoutPage";
import {NavComponent} from "./components/NavComponent";
import {BrowserRouter as Router, Route, Link} from "react-router-dom";
import {Routes} from "react-router";
import {checkResponse, makeRequest} from "./requests";
import {redirect} from "react-router";

// hostname of our server!q
export const server = "http://localhost:3232/";

/**
 * Component that holds most of the content of the page
 * @returns the website's entire JSX, including Header, and currently displayed page
 */
function App(this: any) {
    const [userCode, setUserCode] = useState("");
    const [playlistID, setPlaylist] = useState("");
    const [loggedIn, setLoggedIn] = React.useState(false);

    if (window.location.href.includes("code=") && !(window.location.href.includes("music"))) {
        //TODO: setUserCode
        let raw_args = window.location.search;
        let params = new URLSearchParams(raw_args);

        if (typeof(params.get("code")) != null ) {
            setUserCode(String(params.get("code")));
            //setLoggedIn(true);

            console.log(String(params.get("code")));
            //console.log("params.get(code) is: " + params.get("code"));
            console.log("line 33 app user code is:" + userCode);

            // redirect user
            // window.location.replace(window.location.href.concat("/#/music"));
        }
    }

    // render our page!
    return (
        <div className="main">
            <Router>
                <Navbar fixed="top" className="header">
                    <Navbar.Brand>
                        <Header />
                    </Navbar.Brand>
                    <div className="nav-container">
                        <NavComponent loggedIn={loggedIn}/>
                    </div>
                </Navbar>
                <Routes>
                    <Route path="" element={<LoginPage
                        userCode={userCode}
                        setUserCode={setUserCode}
                        loggedIn={loggedIn}
                        setLoggedIn={setLoggedIn}
                    />}/>
                    <Route path="login" element={<LoginPage
                        userCode={userCode}
                        setUserCode={setUserCode}
                        loggedIn={loggedIn}
                        setLoggedIn={setLoggedIn}
                    />}/>
                    <Route path="about" element={<AboutPage/>}/>
                    <Route path="music" element={<HomePage
                        playlist={playlistID}
                        setPlaylist={(x) => setPlaylist(x)}
                        loggedIn={loggedIn}
                        setLoggedIn={setLoggedIn}
                        userCode={userCode}
                        setUserCode={setUserCode}
                    />}/>
                    <Route path="logout" element={<LogoutPage
                        userCode={userCode}
                        setUserCode={setUserCode}
                        loggedIn={loggedIn}
                        setLoggedIn={setLoggedIn}
                        />}/>
                </Routes>
            </Router>
        </div>
    );
}

export default App;
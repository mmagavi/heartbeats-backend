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
import {ReviewPage} from "./pages/ReviewPage";

// hostname of our server!
export const server = "http://localhost:3232/";

/**
 * Component that holds most of the content of the page
 * @returns the website's entire JSX, including Header, and currently displayed page
 */
function App(this: any) {
    const [userCode, setUserCode] = useState("");
    const [playlistID, setPlaylist] = useState("");
    const [loggedIn, setLoggedIn] = React.useState(false);

    // render our page!
    return (
        <div className="main">
            <Router>
                <Navbar fixed="top" className="header">
                    <Navbar.Brand>
                        {/*<Header />*/}
                    </Navbar.Brand>
                    <div className="navContainer">
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
                    <Route path="review" element={<ReviewPage
                        loggedIn={loggedIn}
                        setLoggedIn={setLoggedIn}
                    />}/>
                </Routes>
            </Router>
        </div>
    );
}

export default App;
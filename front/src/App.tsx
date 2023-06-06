import React, { useState } from "react";
import "../styles/App.css";
import Header from "./components/Header";
import Navbar from "react-bootstrap/Navbar";
import {LoginPage} from "./pages/LoginPage";
import {AboutPage} from "./pages/AboutPage";
import HomePage from "./pages/HomePage";
import {LogoutPage} from "./pages/LogoutPage";
import PageNotFound from "./pages/PageNotFound";
import {MusicNav_AriaLabel, AboutNav_AriaLabel, LogoutNav_AriaLabel} from "./accessibility/Aria";
import {HashRouter as Router, Route, Link} from "react-router-dom";
import {Routes, RouteProps} from "react-router";

// hostname of our server!
export const server = "http://localhost:3232/";

/**
 * Component that holds most of the content of the page
 * @returns the website's entire JSX, including Header, and currently displayed page
 */
function App() {
    const [currPage, setCurr] = useState("Login");
    const [userID, setUserID] = useState("");
    const [playlistID, setPlaylist] = useState("");
    const [loggedIn, setLoggedIn] = React.useState(false);

    // render our page!
    return (
        <div className="main">
            <Router>
                <Navbar fixed="top" className="header">
                    <Navbar.Brand>
                        <Header />
                    </Navbar.Brand>
                    <div className="nav-container">
                        <Link to="/login">login</Link>
                        <Link to="/music">find music</Link>
                        <Link to="/about">about</Link>
                        <Link to="/logout">logout</Link>
                    </div>
                </Navbar>
                <Routes>
                    <Route path="/login" element={<LoginPage
                         status={currPage === "Login"}
                         userID={userID}
                         setUserID={setUserID}
                         loggedIn={loggedIn}
                         setLoggedIn={setLoggedIn}
                     />}/>
                    <Route path="/about" element={<AboutPage
                        status={currPage === "About"}
                    />}/>
                    <Route path="/music" element={<HomePage
                         status={currPage === "Home"}
                         playlist={playlistID}
                         setPlaylist={(x) => setPlaylist(x)}
                         loggedIn={loggedIn}
                         setLoggedIn={setLoggedIn}
                         userID={userID}
                         setUserID={setUserID}
                     />}/>
                    <Route path="/logout" element={<LogoutPage
                         status={currPage === "Logout"}
                         userID={userID}
                         setUserID={setUserID}
                         loggedIn={loggedIn}
                         setLoggedIn={setLoggedIn}
                    />}/>
                </Routes>
            </Router>
        </div>
    );
}

export default App;
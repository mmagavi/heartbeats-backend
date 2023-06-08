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
            window.location.replace(window.location.href.concat("#/music"));
        }
    }

//    http://localhost:5173/?code=AQAXyAF6ztkgY2hQjyflpPJJP65N8mLpz2ZB2lR1kMkqQe_1I_3u63Av16eKvEPnhmYFavFBn55apU9BkFetWGF-VlSOlhmEYj9fjx4_6JR3ZgWGKFYP_6ppIRXbh1QQn_k47-mDXt0pumz2ElUZ4dpgUwqpNF8L6VQxlqaxMdl1WMPpuMUQXimQt4_pWb4GNROZud2oyOr0cQkCxCc-8ikP7u4xh2h7AgDJaynQW4JlfNrdcczzqV0UrxibKlCW3IdUiiUyMRtJUQeIyf0eodYBhyGKXVcOiVWmx6BpDv60pOPGyzewnHfHbmtXxaVTPRTGOZVswzjjCjgwaR05KvUbA3KWGses_YX46qhzpYRxedL9Ru2pU-KQr2OnZr7yOMr1ZJ96dBqbmuoxRgm4GXz2TMU5D8UEQZZ7u_I0EsZmumv184iPrBMOdRW6ip-3QgTp6VH7Cmt8MKHoWX5NPTdd5KHFbG2XKFfRqw/#/music
// http://localhost:5173/?code=AQBQShSez3mBLBUfco6b5C2tnVdDwJaXhiczKOFPJ1TLyx_SUy1NGlzovBOX-8j8zPEuDMTkcS34qJZDL4bZqYctlOdU4G7e1IIQzXAEJd5tqoaY22oojrs2EHhJSA2ur-psQDfD9of7cMCr6rWzbZg26pH4K7B6ruhXjhpLAZoRS2ZG4itGWRXXr83k3dFLWhDpatziUMBxqDzyryuJHBRvO7QZIBK8ezXit83nFk0ecXHOtI6TjP7zG5Jz8GK8eKw8IiBgCjjKn0wSrZLUL4V8-Xpta16AjzVDx2mo3BRm0hiCbfYU5RvHCoJLCpQFuMrqtl6SLW372duqs92lBbnJZm6lXVo-4NbbYa7eADze-eomIulpQMCOECAHGiMmPrtwBB4z6wkU8kbER-ibPqqtIiqXximrd59F5gTPZX-rQWXoHZ_TqXCYsp7FQpjxC26JoayxG5VsjrpxDvjHuuOc4npgFy4LjnwSZA//#/music
//   #%2Fmusic
//   /#/music

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
import React from "react";
import ReactDOM from "react-dom/client";
import "../styles/index.css";
import App from "./App";

// You probably shouldn't modify this file :)
// This is the entry point that React uses to render your app.

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);

/**
 *
 * FRONT END TODOS:
 *
 * Fix cannot render App while updating another component error
 * Fix all questions must have unique IDs error
 *
 * Pass all the variables into review button so we get a comprehensive data set
 * Popup when user clicks on playlist to show them the playlist
 * Popup when user selects too many genres
 *
 * Aria-labels, tab indexes, accessibility
 *
 * Testing ....
 */
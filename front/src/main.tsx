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
 * TODO:
 *
 * Console log errors:
 * Fix cannot render App while updating another component error
 * Fix all questions must have unique IDs error
 *
 * Email feedback:
 * Pass more variables into review button?
 * Add more info to email header?
 *
 * Accessibility:
 * Aria-labels, tab indexes, accessibility
 *
 * Testing?
 */
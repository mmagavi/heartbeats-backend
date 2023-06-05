import '@testing-library/dom'
import '@testing-library/jest-dom'
import React from 'react';
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom";
import App from "../src/App";
import "../styles/App.css";


/**
 * BASIC TESTING FOR FRONTEND WITH NO LOGIN MOCKING - ELEMENTS ARE RENDERED PROPERLY, BUTTONS WORK, PAGE
 * NAVIGATION WORKING
 */
// render elements and verify they show up!
test("test that elements render properly", async() => {

    // setup
    let loginButton: HTMLElement;
    let loginText: HTMLElement;
    let loginPageNav : HTMLElement;
    let musicPageNav : HTMLElement;
    let aboutPageNav : HTMLElement;
    let workoutButton : HTMLElement;
    let winddownButton : HTMLElement;
    let backButton : HTMLElement;

    window.HTMLElement.prototype.scrollIntoView = function() {};
    render(<App />);
    loginButton = screen.getByText("Log in to my Spotify");
    loginText = screen.getByText("Welcome to heartBeats!");
    loginPageNav = screen.getByText("logout");
    musicPageNav = screen.getByText("find music");
    aboutPageNav = screen.getByText("about");

    // Initial start page renders elements
    expect(loginButton).toBeInTheDocument();
    expect(loginText).toBeInTheDocument();
    expect(loginPageNav).toBeInTheDocument();
    expect(musicPageNav).toBeInTheDocument();
    expect(aboutPageNav).toBeInTheDocument();

    // Navigate to about page
    await userEvent.click(aboutPageNav);
    expect(loginButton).not.toBeInTheDocument();
    expect(loginText).not.toBeInTheDocument();
    expect(loginPageNav).toBeInTheDocument();
    expect(musicPageNav).toBeInTheDocument();
    expect(aboutPageNav).toBeInTheDocument();

    // Navigate to music page
    await userEvent.click(musicPageNav);
    // GET NEW BUTTON ELEMENTS
    workoutButton = screen.getByText("working out");
    winddownButton = screen.getByText("winding down");
    expect(loginPageNav).toBeInTheDocument();
    expect(musicPageNav).toBeInTheDocument();
    expect(aboutPageNav).toBeInTheDocument();
    expect(loginButton).not.toBeInTheDocument();
    expect(loginText).not.toBeInTheDocument();
    expect(workoutButton).toBeInTheDocument();
    expect(winddownButton).toBeInTheDocument();

    // Click workout button & check that this navigates us to a new page with more buttons
    // More tests for these buttons once finalized
    await userEvent.click(workoutButton);
    backButton = screen.getByRole("backButton");
    expect(backButton).toBeInTheDocument();
    expect(loginPageNav).toBeInTheDocument();
    expect(musicPageNav).toBeInTheDocument();
    expect(aboutPageNav).toBeInTheDocument();
    expect(workoutButton).not.toBeInTheDocument();
    expect(winddownButton).not.toBeInTheDocument();

    await userEvent.click(backButton);
    workoutButton = screen.getByText("working out");
    winddownButton = screen.getByText("winding down");
    expect(workoutButton).toBeInTheDocument();
    expect(winddownButton).toBeInTheDocument();

    // Navigate to about page from this page
    await userEvent.click(aboutPageNav);
    expect(loginButton).not.toBeInTheDocument();
    expect(loginText).not.toBeInTheDocument();
    expect(loginPageNav).toBeInTheDocument();
    expect(musicPageNav).toBeInTheDocument();
    expect(aboutPageNav).toBeInTheDocument();
});


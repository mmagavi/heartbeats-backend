import '@testing-library/dom'
import '@testing-library/jest-dom'
import React from 'react';
import {fireEvent, render, screen} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom";
import App from "../src/App";
import "../styles/App.css";
import {MockSubmitButton} from "../src/components/MockSubmitButton";

// Test using the wind-down playlist generator
test("Using the winddown playlist generator page",async () => {

    // setup
    let loginButton: HTMLElement;
    let loginText: HTMLElement;
    let loginPageNav: HTMLElement;
    let musicPageNav: HTMLElement;
    let aboutPageNav: HTMLElement;
    let workoutButton: HTMLElement;
    let winddownButton: HTMLElement;
    let submitButton: HTMLElement;
    let heartRateSlider: HTMLElement;
    let ageSlider: HTMLElement;
    let lengthSlider: HTMLElement;

    let choices: HTMLElement[];
    let questions: HTMLElement[];

    let goFastButton: HTMLElement;
    let warmupButton: HTMLElement;
    let lessWindDown: HTMLElement;
    let moreWindDown: HTMLElement;

    let pop: HTMLElement;
    let malay: HTMLElement;
    let metal: HTMLElement;
    let dance: HTMLElement;

    window.HTMLElement.prototype.scrollIntoView = function () {
    };
    render(<App/>);
    loginButton = screen.getByText("Log in to my Spotify");
    loginText = screen.getByText("Welcome to heartBeats!");
    loginPageNav = screen.getByText("logout");
    musicPageNav = screen.getByText("find music");
    aboutPageNav = screen.getByText("about");

    expect(loginButton).toBeInTheDocument();
    expect(loginText).toBeInTheDocument();
    expect(loginPageNav).toBeInTheDocument();
    expect(musicPageNav).toBeInTheDocument();
    expect(aboutPageNav).toBeInTheDocument();

    // Navigate to music page, make sure both options and
    // back button are there
    await userEvent.click(musicPageNav);
    workoutButton = screen.getByText("working out");
    winddownButton = screen.getByText("winding down");
    expect(workoutButton).toBeInTheDocument();
    expect(winddownButton).toBeInTheDocument();
    // click workout button
    await userEvent.click(winddownButton);

    // Now we should get a page of questions
    // Check that all the choices & questions are rendered (correct number)
    choices = screen.getAllByRole("Choice");
    expect(choices.length).toBe(60);
    questions = screen.getAllByRole("Question");
    expect(questions.length).toBe(6);

    goFastButton = screen.getByText("Gotta go fast!");
    lessWindDown = screen.getByText("Less Winddown");
    moreWindDown = screen.getByText("More Winddown");

    expect(goFastButton).toBeInTheDocument();
    expect(lessWindDown).toBeInTheDocument();
    expect(moreWindDown).toBeInTheDocument();


    // click go fast then warmup to make sure value changes
    // AND click wind down button
    await userEvent.click(goFastButton);
    await userEvent.click(moreWindDown);

    // get the sliders and attempt to select different values
    heartRateSlider = screen.getByRole("Slider1");
    ageSlider = screen.getByRole("Slider2");
    lengthSlider = screen.getByRole("Slider3");
    expect(heartRateSlider).toBeInTheDocument();
    expect(ageSlider).toBeInTheDocument();
    expect(lengthSlider).toBeInTheDocument();
    await userEvent.click(heartRateSlider);
    await userEvent.click(ageSlider);
    await userEvent.click(lengthSlider);

    // expect genre buttons to be in the document
    pop = screen.getByText("pop");
    malay = screen.getByText("malay");
    metal = screen.getByText("metal");
    dance = screen.getByText("dance");
    expect(pop).toBeInTheDocument();
    expect(malay).toBeInTheDocument();
    expect(metal).toBeInTheDocument();
    expect(dance).toBeInTheDocument();

    // click & un-click buttons
    await userEvent.click(pop);
    await userEvent.click(metal);
    await userEvent.click(dance);
    await userEvent.click(dance);
    await userEvent.click(malay);
    await userEvent.click(metal);
    // should have selected: malay & pop

    // click a fake submit button
    //expect(ageSlider).not.toBeInTheDocument();
});


// Test using workout playlist generator
test("Using the workout playlist generator page",async () => {

    // setup
    let loginButton: HTMLElement;
    let loginText: HTMLElement;
    let loginPageNav: HTMLElement;
    let musicPageNav: HTMLElement;
    let aboutPageNav: HTMLElement;
    let workoutButton: HTMLElement;
    let winddownButton: HTMLElement;
    let backButton: HTMLElement;
    let submitButton: HTMLElement;

    let choices: HTMLElement[];
    let questions: HTMLElement[];

    let goFastButton: HTMLElement;
    let warmupButton: HTMLElement;
    let lessWindDown: HTMLElement;
    let moreWindDown: HTMLElement;
    let heartRateSlider: HTMLElement;
    let ageSlider: HTMLElement;
    let lengthSlider: HTMLElement;

    window.HTMLElement.prototype.scrollIntoView = function () {
    };
    render(<App/>);
    loginButton = screen.getByText("Log in to my Spotify");
    loginText = screen.getByText("Welcome to heartBeats!");
    loginPageNav = screen.getByText("logout");
    musicPageNav = screen.getByText("find music");
    aboutPageNav = screen.getByText("about");

    expect(loginButton).toBeInTheDocument();
    expect(loginText).toBeInTheDocument();
    expect(loginPageNav).toBeInTheDocument();
    expect(musicPageNav).toBeInTheDocument();
    expect(aboutPageNav).toBeInTheDocument();

    // Navigate to music page, make sure both options and
    // back button are there
    await userEvent.click(musicPageNav);
    workoutButton = screen.getByText("working out");
    winddownButton = screen.getByText("winding down");
    expect(workoutButton).toBeInTheDocument();
    expect(winddownButton).toBeInTheDocument();
    // click workout button
    await userEvent.click(workoutButton);

    // Now we should get a page of questions
    // Check that all the choices & questions are rendered (correct number)
    choices = screen.getAllByRole("Choice");
    expect(choices.length).toBe(60);
    questions = screen.getAllByRole("Question");
    expect(questions.length).toBe(6);

    goFastButton = screen.getByText("Gotta go fast!");
    warmupButton = screen.getByText("Give me a warmup!");
    lessWindDown = screen.getByText("Less Winddown");
    moreWindDown = screen.getByText("More Winddown");

    expect(goFastButton).toBeInTheDocument();
    expect(warmupButton).toBeInTheDocument();
    expect(lessWindDown).toBeInTheDocument();
    expect(moreWindDown).toBeInTheDocument();

    // click go fast then warmup to make sure value changes
    // AND click wind down button
    await userEvent.click(goFastButton);
    await userEvent.click(warmupButton);
    await userEvent.click(moreWindDown);

    // get the sliders and attempt to select different values
    heartRateSlider = screen.getByRole("Slider1");
    ageSlider = screen.getByRole("Slider2");
    lengthSlider = screen.getByRole("Slider3");
    expect(heartRateSlider).toBeInTheDocument();
    expect(ageSlider).toBeInTheDocument();
    expect(lengthSlider).toBeInTheDocument();
    await userEvent.click(heartRateSlider);
    await userEvent.click(ageSlider);
    await userEvent.click(lengthSlider);
    // do not select genres

    // check if submit button renders!!
    submitButton = screen.getByText("Submit");
    expect(submitButton).toBeInTheDocument();
    // now click submit button
    await userEvent.click(submitButton);
    // basic functionality
    //expect(ageSlider).not.toBeInTheDocument();
});
import { useState } from "react";
import QuestionsPage from "./QuestionsPage";
import ResultPage from "./ResultPage";
import { ExerciseQuestions } from "../questions/ExerciseQuestions";
import { questionsType } from "../questions/QuestionsType";
import {SubmitButton} from "../components/buttons/SubmitButton";
import ModeQuestionComponent from "../components/ModeQuestionComponent";

/**
 * Props for the HomePage component -
 * status: should we display this page right now?
 * playlist: generated playlist
 * setPlaylist: set generated playlist
 * loggedIn: is the user currently logged in?
 * setLoggedIn: set loggedIn
 * userCode: user's access code
 * setUserCode: set userCode
 */
interface homePageProps {
  playlist: string;
  setPlaylist: (_: string) => void;
  loggedIn: boolean;
  setLoggedIn: (status: boolean) => void;
  userCode : string;
  setUserCode: (status: string) => void;
}

/**
 * HomePage component. This page contains questions, and resultsPage for
 * playlist generation.
 * @param props - homePageProps described above :)
 * @constructor
 */
export default function HomePage(props: homePageProps) {
  const [resultsPage, setResultsPage] = useState(false);
  const [playlistType, setPlaylistType] = useState<string>("");
  const [genres, setGenres] = useState<string>("");
  const [desiredAge, setDesiredAge] = useState<number>(-1);
  const [desiredIntensity, setDesiredIntensity] = useState<string>("");
  const [desiredLength, setDesiredLength] = useState<number>(-1);
  const [currentBPM, setCurrentBPM] = useState<number>(-1);
  const [playlist_id, setPlaylistID] = useState<string | undefined>("")

  // once we reach the home/music page, set logged in to true
  if (window.location.href.includes("code=")) {
    let raw_args = window.location.search;
    let params = new URLSearchParams(raw_args);
    if (typeof(params.get("code")) != null ) {

      // TODO:
      // causing an error message: react-dom.development.js:86 Warning:
      // Cannot update a component (`App`) while rendering a different component (`HomePage`).
      props.setUserCode(String(params.get("code")));
      props.setLoggedIn(true);

      console.log(String(params.get("code")));
      console.log("line 33 app user code is:" + props.userCode);
    }
  }
  console.log("logged in status: " + props.loggedIn);
  console.log("user ID is: " + props.userCode);

  // reset results
  function reset() {
    setPlaylistType("");
    setResultsPage(false);
    setGenres("");
  }

  // back button
  const backButton = (
      <button className="backButton" role="backButton" tabIndex={0} onClick={(_) => setPlaylistType("")}>
        ️ ← &nbsp; Back
      </button>
  );

  function getSubmitButton(clear: () => void) {
    if (props.playlist == "") {
      return null;
    } else {
      return (
          <SubmitButton userCode={props.userCode}
                        genres={genres}
                        playlist_type={playlistType}
                        desired_intensity={desiredIntensity}
                        age={desiredAge}
                        workout_length={desiredLength}
                        current_bpm={currentBPM}
                        setResultsPage={setResultsPage}
                        setPlaylistID={setPlaylistID}
                        setPlaylistType={setPlaylistType}/>
      );
    }
  }

  // options for types of playlists
  const playlistChoices: { [key: string]: questionsType[] } = {
    // each question set page needs a way to change the playlist code
    exercise: ExerciseQuestions,
  };

  const playlistQuestion = {
    question:
        "Select a playlist mode:",
    id: 1,
    choices: [
      {
        text: "Standard",
        img: "standardMode",
        val: "classic",
        key: 200,
      },
      {
        text: "Wind-Down",
        img: "windDownMode",
        val: "relax",
        key: 201,
      },
      {
        text: "Standard Interval",
        img: "standardIntervalMode",
        val: "interval_one",
        key: 202,
      },
      {
        text: "Pyramid Interval",
        img: "pyramidIntervalMode",
        val: "interval_two",
        key: 203,
      },
    ],
    setChoice: (_: number, s: string) => {
      setPlaylistType(s);
    },
    getChoice: (_: number, v: any) => {
      return playlistType === v;
    },
  };

  // returns the question asking if the user wants exercise or relaxation
  // if player already chose, displays questions for that type of playlist
  // otherwise displays the stated question
  const questionPage = resultsPage ? ( // if completed quiz
      <ResultPage playlistID={playlist_id} reset={reset}/>
  ) : playlistType == "" ? ( // first screen
      <div className="mainBody">
        <ModeQuestionComponent {...playlistQuestion}
                           setDesiredAge={setDesiredAge}
                           setDesiredLength={setDesiredLength}
                           setDesiredBPM={setCurrentBPM}
                               setDesiredIntensity={setDesiredIntensity}/>
      </div>
  ) : (
      // questions
      <div className="mainBody">
        {backButton}

        <QuestionsPage
            setPlaylist={props.setPlaylist}
            submitButton={getSubmitButton}
            questionsRaw={playlistChoices["exercise"]}
            setGenres={setGenres}
            setDesiredIntensity={setDesiredIntensity}
            setDesiredAge={setDesiredAge}
            setDesiredLength={setDesiredLength}
            setDesiredBPM={setCurrentBPM}
        />
      </div>
  );

  console.log("questions page....");
  console.log(playlistType);
  return questionPage;
}
import ModeQuestionComponent from "../components/QuestionComponent";
import { useState } from "react";
import QuestionsPage from "./QuestionsPage";
import ResultPage from "./ResultPage";
import { ExerciseQuestions } from "../questions/ExerciseQuestions";
import { questionsType } from "../questions/QuestionsType";
import {MockSubmitButton} from "../components/MockSubmitButton";

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
interface mockHomePageProps {
    playlist: string;
    setPlaylist: (_: string) => void;
    loggedIn: boolean;
    setLoggedIn: (status: boolean) => void;
    userCode : string;
    setUserCode: (status: string) => void;
}

/**
 * ALMOST ENTIRELY A HOMEPAGE EXCEPT IT CALLS MOCK SUBMIT BUTTON
 * HomePage component. This page contains questions, and resultsPage for
 * playlist generation.
 * @param props - homePageProps described above :)
 * @constructor
 */
export default function MockHomePage(props: mockHomePageProps) {
    const [resultsPage, setResultsPage] = useState(false);
    const [playlistType, setPlaylistType] = useState<string>("");
    const [genres, setGenres] = useState<string>("");
    const [desiredWarmup, setDesiredWarmup] = useState<string>("");
    const [desiredCoolDown, setDesiredCoolDown] = useState<string>("");
    const [desiredAge, setDesiredAge] = useState<number>(-1);
    const [desiredLength, setDesiredLength] = useState<number>(-1);
    const [currentBPM, setCurrentBPM] = useState<number>(-1);
    const [playlist_id, setPlaylistID] = useState<string | undefined>("")

    // reset results
    function reset() {
        setPlaylistType("");
        setResultsPage(false);
        setGenres("");
    }

    // back button
    const backButton = (
        <button className="backButton" role="backButton" onClick={(_) => setPlaylistType("")}>
            ️ ⬅️ &nbsp; Back
        </button>
    );

    // submit button
    // TODO: replace with a SubmitButton component
    function getSubmitButton(clear: () => void) {
        if (props.playlist == "") {
            return null;
        } else {
            return (
                <MockSubmitButton
                    userCode={props.userCode}
                    genres={genres}
                    playlist_type={playlistType}
                    desired_warmup={desiredWarmup}
                    desired_cool_down={desiredCoolDown}
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

    // structure of the questions
    const playlistQuestion = {
        question: "What kind of playlist are you looking for?",
        id: 1,
        choices: [
            {
                text: "working out",
                img: "https://s3-us-west-1.amazonaws.com/contentlab.studiod/getty/31a4debc7443411195df509e38a5f9a3.jpg",
                val: "exercise",
                key: 500,
            },
            {
                text: "winding down",
                img: "https://discovery.sndimg.com/content/dam/images/discovery/fullset/2020/4/2/nightsky2_getty.jpg.rend.hgtvcom.406.305.suffix/1585862428885.jpeg",
                val: "relax",
                key: 501,
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
                               setDesiredWarmup={setDesiredWarmup}
                               setDesiredCoolDown={setDesiredCoolDown}
                               setDesiredAge={setDesiredAge}
                               setDesiredLength={setDesiredLength}
                               setDesiredBPM={setCurrentBPM}/>
        </div>
    ) : (
        // questions
        <div className="mainBody">
            {backButton}

            <QuestionsPage
                setPlaylist={props.setPlaylist}
                submitButton={getSubmitButton}
                questionsRaw={playlistChoices[playlistType]}
                setGenres={setGenres}
                setDesiredWarmup={setDesiredWarmup}
                setDesiredCoolDown={setDesiredCoolDown}
                setDesiredAge={setDesiredAge}
                setDesiredLength={setDesiredLength}
                setDesiredBPM={setCurrentBPM}
            />
        </div>
    );

    console.log(playlistType);
    return questionPage;
}
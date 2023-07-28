import { useState } from "react";
import QuestionComponent from "../components/QuestionComponent";
import "../../styles/quiz.css";
import { questionsType } from "../questions/QuestionsType";
import {Genres} from "../questions/Genres";
import GenreComponent from "../components/GenreComponent";
import Footer from "../components/Footer";

/**
 * Props for questions page component - setPlaylist, submitButton,
 * and questionsRaw to generate the questions
 */
interface QuestionsPageProps {
  setPlaylist: (playlistID: string) => void;
  submitButton: (submit: () => void) => null | JSX.Element;
  questionsRaw: questionsType[];
  setGenres: (genres: (e: any) => any) => void;
  setDesiredAge: (age : number) => void;
  setDesiredLength: (length : number) => void;
  setDesiredBPM: (bpm : number) => void;
  setDesiredIntensity: (warmup: string) => void;
}

/**
 * Questions page component - list of questions based on questions[] passed in & submit button
 * @param props QuestionPageProps: set the playlist id for results page, submitButton
 * and questionsRaw
 * @constructor
 */
export default function QuestionsPage(props: QuestionsPageProps) {

  let genre_list = {
    "afrobeat" : false, "alt-rock": false, "alternative" : false, "blues" : false, "brazil" : false,
    "british"  : false, "chill" : false, "classical" : false, "club" : false, "country" : false, "dance" : false,
    "disco" : false, "dubstep" : false, "edm" : false, "emo" : false, "folk" : false, "french" : false,
    "funk" : false, "garage" : false, "german" : false, "grunge" : false, "hardcore" : false, "hip-hop" : false,
    "indian" : false, "indie" : false, "indie-pop" : false, "industrial" : false, "j-pop" : false, "jazz" : false,
    "k-pop" : false, "latino" : false, "malay" : false, "mandopop" : false, "metal" : false, "new-age" : false,
    "new-release" : false, "opera" : false, "party" : false, "philippines-opm" : false, "pop" : false,
    "punk" : false, "punk-rock" : false, "r-n-b" : false, "rainy-day" : false, "reggae" : false, "rock" : false,
    "sad" : false, "samba" : false, "sertanejo" : false, "sleep" : false, "soul" : false, "spanish" : false,
    "synth-pop" : false, "techno" : false, "work-out" : false, "world-music" : false
  };

  const [ans, setAns] = useState<{ [key: number]: any }>({});
  const [chosenGenre, setChosenGenre] = useState<{ [key: string] : boolean }>(genre_list);

  const questionsRaw = props.questionsRaw;
  function setChoice(id: number, val: any) {
    console.log("set choice to " + val);
    setAns({ ...ans, [id]: val });
  }

  function chooseThree(){
    let count = 0
    for (let genre in chosenGenre){
      if (chosenGenre[genre]) count++
    }
    return count >= 3;
  }

  // set a genre as part of the list of genres
  function setGenre(genre: string) {
    console.log(chosenGenre)
    if (genre in chosenGenre) {
      console.log(genre);
      if (chosenGenre[genre]) {
        setChosenGenre({...chosenGenre, [genre] : !chosenGenre[genre]});
        props.setGenres(e => e.replace(genre + ",", ""));
      }
      else {
        if (chooseThree()) {
          //popup error message
          window.alert("Cannot select more than 3 genres! Sorry :(");
          return;
        }
        setChosenGenre({...chosenGenre, [genre] : !chosenGenre[genre]});
        props.setGenres(e => e.concat(genre).concat(","));
      }
    }
  }

  // get choice ...
  function getChoice(id: number, val: any) {
    return ans[id] === val;
  }

  // get genre ...
  function getGenre(val: string){
    return chosenGenre[val]
  }

  // map of questions
  const questions = props.questionsRaw.map((q) => (
      <QuestionComponent {...q} setChoice={setChoice} getChoice={getChoice} setDesiredAge={props.setDesiredAge} setDesiredIntensity={props.setDesiredIntensity} setDesiredLength={props.setDesiredLength} setDesiredBPM={props.setDesiredBPM}/>
  ));

  // create a genre component with setGenre, getGenre, and set genres on submit
  // const genres = <GenreComponent {...Genres} setGenre={setGenre} getGenre={getGenre}/>
  const genres = <GenreComponent {...Genres} setGenre={setGenre} getGenre={getGenre}/>

  // placeholder
  function getCode() {
    return "4i4NrXEtkbUkINI9HZtwjU";
  }

  function submit() {
    console.log(ans);
    setAns({});

    let raw_args = window.location.search
    let params = new URLSearchParams(raw_args)

    // fetch("http://localhost:3232/register-user-code?code=" + params.get("code"))
  }

  // checks if answers are all answered. If so, gets code.
  let submitButton = null;
  if (Object.keys(ans).length == questionsRaw.length) {
    // submit button
    props.setPlaylist(getCode());
    submitButton = props.submitButton(submit);
  }
  // render questions, genre question, and submit button
  return (
    <div className="questionsPage">
      {questions}
      {genres}
      {submitButton}
      <Footer/>
    </div>
  );
}

import "../../styles/quiz.css";
import ChoiceButton from "./buttons/ChoiceButton";
import SliderUI from "./SliderUI";
import {Question_Role} from "../accessibility/Aria";

/**
 * props for non-genre questions - id number, question, choices, setChoice function
 * which sets the current choice and getChoice which gets the current selection
 */
interface questionProps {
  id: number;
  question: string;
  choices?: choice[];
  setChoice: (n: number, v: any) => void; // stand in for now
  getChoice: (n: number, v: any) => boolean;
  setDesiredAge: (age : number) => void;
  setDesiredLength: (length : number) => void;
  setDesiredBPM: (bpm : number) => void;
  setDesiredIntensity: (warmup: string) => void;
}

/**
 * Choice interface: text, background image, value
 */
interface choice {
  text: string;
  img: string;
  val: string;
  key: number;
}

/**
 *
 * @param props props for question: id, question, choices, setChoice,
 * getChoice
 * @constructor
 */
export default function QuestionComponent(props: questionProps) {
  function checkChoice(v: any) {
    return props.getChoice(props.id, v);
  }

  function setNumHelper(n : number) {
    props.setChoice(props.id, n);
    if (props.id == 3) {
      props.setDesiredLength(n);
      //console.log("set desired length to " + n);
    } else if (props.id == 2) {
      props.setDesiredAge(n);
      //console.log("set desired age to " + n);
    }
  }

  const choices =
    props.choices instanceof Array ? (
      props.choices.map((c) => (
        <ChoiceButton
          {...c}
          id={props.id}
          setValue={props.setChoice}
          checkChoice={checkChoice}
          setDesiredAge={props.setDesiredAge}
          setDesiredLength={props.setDesiredLength}
          setDesiredBPM={props.setDesiredBPM}
          setDesiredIntensity={props.setDesiredIntensity}
        />
      ))
    ) : (
        <div className="center">
      <div className="slider">
        <SliderUI
          currentVal={90}
          // setCurrentVal={(n) => props.setChoice(props.id, n)}
            setCurrentVal={n => setNumHelper(n)}
          id={props.id}
        />
      </div>
        </div>
    );

  return (
    <div className="questionItem">
      <div className="question" role={Question_Role} aria-label={props.question} tabIndex={0}>{props.question}</div>
      <div className="choicesList">{choices}</div>
    </div>
  );
}

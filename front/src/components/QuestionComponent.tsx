import "../../styles/quiz.css";
import ChoiceButton from "./ChoiceButton";
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
  setDesiredWarmup: (warmup: string) => void;
  setDesiredCoolDown: (coolDown: string) => void;
  setDesiredAge: (age : number) => void;
  setDesiredLength: (length : number) => void;
  setDesiredBPM: (bpm : number) => void;
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

  // TODO: Update!!
  function setNumHelper(n : number) {
    props.setChoice(props.id, n);
    if (props.id == 3) {
      props.setDesiredBPM(n);
    } else if (props.id == 4) {
      props.setDesiredAge(n);
    } else if (props.id == 5) {
      props.setDesiredLength(n);
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
          setDesiredWarmup={props.setDesiredWarmup}
          setDesiredCoolDown={props.setDesiredCoolDown}
          setDesiredAge={props.setDesiredAge}
          setDesiredLength={props.setDesiredLength}
          setDesiredBPM={props.setDesiredBPM}
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
      <div className="question" role={Question_Role} aria-label={props.question}>{props.question}</div>
      <div className="choicesList">{choices}</div>
    </div>
  );
}

import "../../styles/quiz.css";
import {Choice_Role} from "../accessibility/Aria";

/**
 * Props for ChoiceButton.
 * Button id, value, background image, text to display, setValue function to
 * change the value of the currently selected choice. checkChoice function checks
 * if this is the currently selected choice.
 */
interface choiceProps {
  id: number;
  val: any;
  img: string;
  text: string;
  setValue: (i: number, val: any) => void;
  checkChoice: (val: any) => boolean;
  setDesiredWarmup: (warmup: string) => void;
  setDesiredCoolDown: (coolDown: string) => void;
  setDesiredAge: (age : number) => void;
  setDesiredLength: (length : number) => void;
  setDesiredBPM: (bpm : number) => void;
}

/**
 * ChoiceButton component
 * Returns a choice radio button as part of a question in ChoiceComp.
 * @param props - id, val, img, text, setValue, checkChoice
 * @constructor
 */
export default function ChoiceButton(props: choiceProps) {
  const checked = props.checkChoice(props.val);

  // style CSS for question display text
  const styleCSS = {
    letterSpacing: checked ? "5px" : undefined,
    color: checked ? "#FF0000" : "white",
  };

  // on-click function
  function onChangeHelper() {
    props.setValue(props.id, props.val)
    if (props.id == 1) {
      if (props.val == "quick") {
        props.setDesiredWarmup("start_quickly");
      }
      else if (props.val == "full") {
        props.setDesiredWarmup("start_with_warmup");
      }
    } if (props.id == 2) {
      if (props.val == "quick") {
        props.setDesiredCoolDown("short_cool_down");
      }
      else if (props.val == "slow") {
        props.setDesiredCoolDown("long_cool_down");
      }
    } if (props.id == 3) {
      // CHANGE THIS !!
      props.setDesiredAge(props.val);
    }
  }

  // return button
  return (
    <div className="choice" style={styleCSS}>
      <label key={props.id + ":" + props.val} aria-label={props.text} role={Choice_Role}>
        <input
          type="radio"
          value={props.val}
          checked={checked}
          onChange={onChangeHelper}
          name={"" + props.id}
          />
        &#x2665;
          <div className="questionText"><p>{props.text}</p></div>
      </label>
    </div>
  );
}

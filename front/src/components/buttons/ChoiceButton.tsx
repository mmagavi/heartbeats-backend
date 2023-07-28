import "../../../styles/quiz.css";
import {Choice_Role} from "../../accessibility/Aria";

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
  setDesiredAge: (age : number) => void;
  setDesiredLength: (length : number) => void;
  setDesiredBPM: (bpm : number) => void;
  setDesiredIntensity: (warmup: string) => void;
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
    // letterSpacing: checked ? "5px" : undefined,
    color: checked ? "#FF0000" : "white",
    background: checked ? "rgba(98, 95, 95, 0.88)" : undefined,
  };

  // on-click function
  function onChangeHelper() {
    props.setValue(props.id, props.val)
    if (props.id == 1) {
      if (props.val == "low") {
        props.setDesiredIntensity("low");
      } else if (props.val == "medium") {
        props.setDesiredIntensity("medium");
      } else if (props.val == "high") {
        props.setDesiredIntensity("high");
      }
    } if (props.id == 3) {
      // CHANGE THIS !!
      props.setDesiredAge(props.val);
    }
  }

  // return button
  return (
        <label className="choice" style={styleCSS} tabIndex={0} key={props.id + ":" + props.val} aria-label={props.text} role={Choice_Role}>
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
  );
}
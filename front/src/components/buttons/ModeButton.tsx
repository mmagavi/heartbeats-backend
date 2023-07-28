import "../../../styles/quiz.css";
import {Choice_Role} from "../../accessibility/Aria";
import standardMode from "../../images/standardMode.png";
import pyramidIntervalMode from "../../images/pyramidIntervalMode.png";
import standardIntervalMode from "../../images/standardIntervalMode.png";
import windDownMode from "../../images/windDownMode.png";

/**
 * Props for ModeButton.
 * Button id, value, background image, text to display, setValue function to
 * change the value of the currently selected choice. checkChoice function checks
 * if this is the currently selected choice.
 */
interface modeProps {
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
 * ModeButton component for initial question
 * Returns a choice radio button as part of a question in ChoiceComp.
 * @param props - id, val, img, text, setValue, checkChoice
 * @constructor
 */
export default function ModeButton(props: modeProps) {
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
            if (props.val == "low") {
                props.setDesiredIntensity("beginner");
            } else if (props.val == "medium") {
                props.setDesiredIntensity("intermediate");
            } else if (props.val == "high") {
                props.setDesiredIntensity("advanced");
            }
        } if (props.id == 3) {
            // CHANGE THIS !!
            props.setDesiredAge(props.val);
        }
    }

    // get img src
    let img_src = standardMode;
    if (props.img == "standardIntervalMode") {
        img_src = standardIntervalMode;
    } else if (props.img == "pyramidIntervalMode") {
        img_src = pyramidIntervalMode;
    } else if (props.img == "windDownMode") {
        img_src = windDownMode;
    }

    // return button
    return (
        <div className="modeChoice" style={styleCSS} tabIndex={0}>
            <label className="modeChoiceBox" key={props.id + ":" + props.val} aria-label={props.text} role={Choice_Role} >
                <input
                    type="radio"
                    value={props.val}
                    checked={checked}
                    onChange={onChangeHelper}
                    name={"" + props.id}

                />
                <img src={img_src}  alt={props.text}/>
                <div className="questionText"><p>{props.text}</p></div>
            </label>
        </div>
    );
}

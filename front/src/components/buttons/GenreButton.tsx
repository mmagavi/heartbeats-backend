import { hover } from "@testing-library/user-event/dist/types/setup/directApi";
import "../../../styles/quiz.css";
import {Choice_Role} from "../../accessibility/Aria";

/**
 * Props for GenreButton.
 * Button id, value, text to display, setValue function to change the list
 * of currently selected choices. checkChoice function checks if this is one of
 * the currently selected choices.
 */
interface genreProps {
    id: number;
    val: string;
    text: string;
    setValue: (val: string) => void;
    checkChoice: (val: string) => boolean;
}

/**
 * GenreButton Component.
 * Returns a choice checkbox button as part of the list of genres to select
 * from.
 * @param props - id, val, text, setValue, checkChoice
 * @constructor
 */
export default function GenreButton(props: genreProps) {
    const checked = props.checkChoice(props.val);

    // style CSS for question display text
    const styleCSS = {
        //letterSpacing: checked ? "3px" : undefined,
        //background: checked ? "#da6560" : "grey",
        background: checked ? "rgba(175,175,175,0.88)" : "rgba(180,180,180,0.2)",
        padding: checked ? "0.7vw" : undefined,
        paddingLeft: checked ? "1.2vw" : undefined,
        paddingRight: checked ? "1.2vw" : undefined,
        borderRadius: checked ? "5vw" : "3vw",
        transition: "all 0.3s ease-in-out",
    };

    // return button
    return (
        <div className="genreChoice" tabIndex={0}>
            <label key={props.id + ":" + props.val} aria-label={props.text} role={Choice_Role}>
                <input
                    type="checkbox"
                    value={props.val}
                    checked={checked}
                    onChange={(_) => props.setValue(props.val)}
                    name={"" + props.id}
                />
                <div className="genreQuestionText" style={styleCSS}>{props.text}</div>
            </label>
        </div>
    );
}

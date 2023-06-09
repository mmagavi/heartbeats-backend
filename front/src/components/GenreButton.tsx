import { hover } from "@testing-library/user-event/dist/types/setup/directApi";
import "../../styles/quiz.css";
import {Choice_Role} from "../accessibility/Aria";

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
        letterSpacing: checked ? "3px" : undefined,
        background: checked ? "#f13837" : "grey",
        padding: checked ? "0.5vw" : undefined,
        paddingLeft: checked ? "0.5vw" : undefined,
        paddingRight: checked ? "0.5vw" : undefined,
        borderRadius: checked ? "27px" : "17px",
    };

    // return button
    return (
        <div className="genreChoice">
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

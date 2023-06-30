import "../../styles/quiz.css";
import GenreButton from "./buttons/GenreButton";
import {GenreQuestion_AriaLabel, Question_Role} from "../accessibility/Aria";

/**
 * Choice interface - a choice contains a display text string
 * and a value string
 */
interface choice {
    text: string;
    val: string;
    key: number;
}

/**
 * Props for GenreComponent component
 * Includes question is, question display text, list of choices to include,
 * setGenre function which adds or removes a genre from the genre list, and
 * getGenre which gets a genre value from the list
 */
interface questionProps {
    id: number;
    question: string;
    choices?: choice[];
    setGenre: (v: string) => void; // stand in for now
    getGenre: (g: string) => boolean;
}

/**
 * Genre Component returns the Genre question and choices
 * @param props - id, question, choices, setGenre function, getGenre function
 * @constructor
 */
export default function GenreComponent(props: questionProps) {
    // check choice helper
    function checkChoice(v: any) {
        return props.getGenre(v);
    }

    // map choices to GenreButtons
    const choices =
        props.choices instanceof Array ? (
            props.choices.map((c) => (
                <GenreButton
                    {...c}
                    id={props.id}
                    setValue={props.setGenre}
                    checkChoice={checkChoice}
                />
            ))
        ) : (
            <div className="center">
                <div className="slider">
                </div>
            </div>
        );

    // Return question-item
    return (
        <div className="questionItem" aria-label={GenreQuestion_AriaLabel}>
            <div className="question" aria-label={props.question} role={Question_Role} tabIndex={0}>{props.question}</div>
            <div className="genreList">{choices}</div>
        </div>
    );
}

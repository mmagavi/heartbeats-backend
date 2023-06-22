import {AboutText_Role, AboutText_AriaLabel} from "../accessibility/Aria";
import {ReviewSubmitButton} from "../components/buttons/ReviewSubmitButton";
import {useState} from "react";
import {InputBox_AriaLabel} from "../accessibility/Aria";


/**
* Props for review page component -
 * will be passed into submit button for data collection
*/
interface ReviewPageProps {
        loggedIn: boolean;
        setLoggedIn: (status: boolean) => void;
}

/**
 * OBSOLETE CURRENTLY.
 * ReviewPage component. Describes the web app briefly
 * @constructor
 */
function ReviewPage(props: ReviewPageProps){
    const [input, setInput] = useState<string>("");
    const [rating, setRating] = useState<number>(0);

    props.setLoggedIn(true);

    return (
        <div className="reviewBody" role={AboutText_Role} aria-label={AboutText_AriaLabel}>
            Please us some leave feedback on your playlist and your overall experience!
                <div className="rating">
                        <input id="rating1" type="radio" name="rating" value="1"/>
                        <label htmlFor="rating1">★</label>
                        <input id="rating2" type="radio" name="rating" value="2"/>
                        <label htmlFor="rating2">★</label>
                        <input id="rating3" type="radio" name="rating" value="3"/>
                        <label htmlFor="rating3">★</label>
                        <input id="rating4" type="radio" name="rating" value="4"/>
                        <label htmlFor="rating4">★</label>
                        <input id="rating5" type="radio" name="rating" value="5"/>
                        <label htmlFor="rating5">★</label>
                </div>
            <br/>
            <textarea className="inputDiv" rows={5} cols={60} name="text" placeholder="Enter text" aria-label={InputBox_AriaLabel} />
            <br/>
            <ReviewSubmitButton/>
            <br/>
        </div>)
}

export {ReviewPage}
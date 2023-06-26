import { SubmitButton_AriaLabel, SubmitButton_Role } from "../../accessibility/Aria";

interface ReviewSubmitButtonProps {
    // needs to get all the user data
}

/**
 * NOT CURRENTLY IN USE. ONLY USED BY REVIEW PAGE WHICH WE ARE CURRENTLY NOT USING...
 * JUST USING A MAILTO HREF INSTEAD
 */

/**
 * Review submit button component. Submits a review.
 * @param props user data to store
 * @constructor
 */
function ReviewSubmitButton(props : ReviewSubmitButtonProps) {

    // redirect to review page
    async function redirect() {

        //TODO: store user data
        // in href?
        // popup...
        // button to return to old page?

        console.log("submitting review...");
        console.log("rating: ");
        console.log("review: ");
    }

    // return component!
    return (
        <button className="reviewSubmitButton" role={SubmitButton_Role} aria-label={SubmitButton_AriaLabel} tabIndex={0} onClick={redirect}>
            Submit feedback :)
        </button>
    )
}

export { ReviewSubmitButton }
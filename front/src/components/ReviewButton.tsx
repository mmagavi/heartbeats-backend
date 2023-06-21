import { SubmitButton_AriaLabel, SubmitButton_Role } from "../accessibility/Aria";

interface ReviewButtonProps {
}

/**
 * Review button component. Redirects to the review page.
 * @param props user data to store
 * @constructor
 */
function ReviewButton(props : ReviewButtonProps) {

    // redirect to review page
    async function redirect() {
        //TODO: store user data
        //in href?
        window.location.href = "http://localhost:5173/review";
    }

    // return component!
    return (
        <button className="refreshButton" role={SubmitButton_Role} aria-label={SubmitButton_AriaLabel} tabIndex={0} onClick={redirect}>
            Leave a review!
        </button>
    )
}

export { ReviewButton }
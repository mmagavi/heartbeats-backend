import { ReviewButton_AriaLabel, ReviewButton_Role } from "../../accessibility/Aria";

/**
 * Review button props
 */
interface ReviewButtonProps {
    //TODO: playlist type? More data to add to subject line?
}

/**
 * Review button component. Opens up an email window so users can send us feedback
 * @param props user data to store
 * @constructor
 */
function ReviewButton(props : ReviewButtonProps) {
    // TODO: add more data to subject line

    // return button!!
    return (
        <a href="mailto:heartbeats.feedback@gmail.com?subject=Feedback on my HeartBeats Playlist">
        <button className="resultPageButton" role={ReviewButton_Role} aria-label={ReviewButton_AriaLabel} tabIndex={0}>
            ✉️ Send us feedback :)
        </button>
        </a>
    )
}

export { ReviewButton }
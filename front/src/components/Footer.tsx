import { Footer_AriaLabel } from "../accessibility/Aria";


/**
 * Component that represents the page title!
 * @returns the component JSX to be rendered with our title inside
 */
function Footer() {
    // return our component!
    return <div className="footer"
                aria-label={Footer_AriaLabel}>
        * Please note that the submit button will not show up until you have answered every question! Also note that playlists containing a wide variety of genres may not transition as smoothly.
    </div>;
}

export default Footer;

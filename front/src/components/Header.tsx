import { Header_AriaLabel, Header_Role } from "../accessibility/Aria";


/**
 * Component that represents the page title!
 * @returns the component JSX to be rendered with our title inside 
 */
function Header() {
  // return our component!
  return <div role={Header_Role}
          className="title" 
          aria-label={Header_AriaLabel}
          tabIndex={0}>
      💌 &nbsp;heartBeats
         </div>;
}

export default Header;

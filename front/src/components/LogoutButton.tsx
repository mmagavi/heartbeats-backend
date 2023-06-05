import { LogoutButton_AriaLabel, LogoutButton_Role } from "../accessibility/Aria";

/**
 * props for logout button - setID to set user access token and
 * set logged in function
 */
interface LogoutStatusProps {
    setID: (status: string) => void;
    setLI: (status: boolean) => void;
}


/**
 * Constructor for logout button
 * @param props props for this component - setLI and setID
 * @returns component JSX which is a logout button!
 */
function LogoutButton(props: LogoutStatusProps) {

    // button click helper
    function logout() {
        props.setLI(false);
        props.setID("");
        window.location.href = "http://localhost:5173/home?";
    }

    // return component!
    return (
        <button className="LoginButton" role={LogoutButton_Role} aria-label={LogoutButton_AriaLabel} tabIndex={0} onClick={logout}>
            Log Out
        </button>
    )
}

export { LogoutButton }
import { LoginButton_AriaLabel, LoginButton_Role } from "../accessibility/Aria";

/**
 * props for login status - setCode (set the stored id) and setLI (set logged in)
 */
interface LoginStatusProps {
    setCode: (status: string) => void;
    setLI: (status: boolean) => void;
}


/**
 * Constructor for Login Button, returns a login button which takes you to the
 * spotify login page and allows user to log into their spotify
 * @param props props for this component - setCode, setLI
 * @returns component which is a login button!
 */
function LoginButton(props: LoginStatusProps) {
    // handle button click
    async function handleLoginClick(){
        await fetch("http://localhost:3232/login")
        .then((response) => response.text())
        .then(response => {window.location.replace(response);})
    }

    // return component!
    return (
        <button className="LoginButton" role={LoginButton_Role} onClick={handleLoginClick} aria-label={LoginButton_AriaLabel} tabIndex={0}>
            Log in to my Spotify
        </button>
    )
}

export { LoginButton }
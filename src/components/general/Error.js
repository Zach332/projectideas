import ChromeDinoGame from "react-chrome-dino";
import { Helmet } from "react-helmet-async";
import { Globals } from "../../GlobalData";
import { Link } from "react-router-dom";

export default function Error({ pageTitle, errorMessage, showHome = true }) {
    const isMobile = window.innerWidth <= 768;
    return (
        <div>
            <Helmet>
                <title>
                    {pageTitle} | {Globals.Title}
                </title>
            </Helmet>
            <h1>{pageTitle}</h1>
            <p>{errorMessage}</p>
            {!isMobile && (
                <div>
                    {showHome ? (
                        <p>
                            You can go to our <Link to="/">home page</Link>, or
                            enjoy the game below (space to start/jump).
                        </p>
                    ) : (
                        <p>
                            You can also enjoy the game below (space to
                            start/jump).
                        </p>
                    )}
                    <ChromeDinoGame />
                </div>
            )}
        </div>
    );
}

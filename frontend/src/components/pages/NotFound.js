import React from "react";
import ChromeDinoGame from "react-chrome-dino";
import { Helmet } from "react-helmet-async";
import { Globals } from "../../GlobalData";
import { Link } from "react-router-dom";

export default function NotFound() {
    const isMobile = window.innerWidth <= 768;
    return (
        <div>
            <Helmet>
                <title>Not Found | {Globals.Title}</title>
            </Helmet>
            <h1>Page Not Found</h1>
            <p>The link you used does not appear to be valid.</p>
            {!isMobile && (
                <div>
                    <p>
                        You can go to our <Link to="/">home page</Link>, or
                        enjoy the game below (space to start/jump).
                    </p>
                    <ChromeDinoGame />
                </div>
            )}
        </div>
    );
}

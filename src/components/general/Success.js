import { useEffect } from "react";
import CheckMark from "../../check.svg";
import { useKeyPress } from "../hooks/Keys";
import { useHistory } from "react-router-dom";

export default function Success({ link, message }) {
    let history = useHistory();
    const enterPress = useKeyPress("Enter");
    const isMobile = window.innerWidth <= 768;

    useEffect(() => {
        if (enterPress) {
            history.push(link);
        }
    }, [enterPress]);

    const onClick = () => {
        history.push(link);
    };

    return (
        <div>
            <img
                src={CheckMark}
                className="mx-auto d-block p-4"
                alt="Success"
            />
            <div className="text-center">
                <button
                    type="button"
                    className="btn btn-link btn-lg"
                    onClick={onClick}
                >
                    {message} {isMobile ? "" : "(press enter)"}
                    <svg
                        width="1em"
                        height="1em"
                        viewBox="0 0 16 16"
                        className="bi ms-1 bi-arrow-return-left"
                        fill="currentColor"
                        xmlns="http://www.w3.org/2000/svg"
                    >
                        <path
                            fillRule="evenodd"
                            d="M14.5 1.5a.5.5 0 0 1 .5.5v4.8a2.5 2.5 0 0 1-2.5 2.5H2.707l3.347 3.346a.5.5 0 0 1-.708.708l-4.2-4.2a.5.5 0 0 1 0-.708l4-4a.5.5 0 1 1 .708.708L2.707 8.3H12.5A1.5 1.5 0 0 0 14 6.8V2a.5.5 0 0 1 .5-.5z"
                        />
                    </svg>
                </button>
            </div>
        </div>
    );
}

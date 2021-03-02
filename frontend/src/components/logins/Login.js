import React from "react";
import GoogleLogin from "react-google-login";
import { toQuery } from "../utils/Routing";
import GitHubSymbol from "../../GitHub-Mark.png";
import GitHubSymbolLight from "../../GitHub-Mark-Light.png";
import GoogleLogo from "../../GoogleLogo.svg";
import { useGlobalState } from "../../State";

export default function Login() {
    const [theme] = useGlobalState("theme");

    const search = toQuery({
        client_id: process.env.REACT_APP_GITHUB_CLIENT_ID,
        scope: "user:email",
        redirect_uri: process.env.REACT_APP_GITHUB_REDIRECT_URI,
    });

    const onCLick = () => {
        window.location.href =
            "https://github.com/login/oauth/authorize?" + search;
    };

    return (
        <div className="container">
            <img
                src={GoogleLogo}
                className="mx-auto d-block m-4"
                style={{ width: 80 }}
                alt=""
            />
            <div className="text-center pb-5">
                <GoogleLogin
                    clientId="449086482050-t6e9tflhou1r9b905s42pvjtbvac23hl.apps.googleusercontent.com"
                    uxMode="redirect"
                    redirectUri={window.location.href + "/oauth2/code/google"}
                    render={(renderProps) => (
                        <button
                            type="btn btn-primary"
                            className="btn btn-outline-primary btn-lg"
                            onClick={renderProps.onClick}
                            disabled={renderProps.disabled}
                        >
                            Login with Google
                            <svg
                                width="1em"
                                height="1em"
                                viewBox="0 0 16 16"
                                className="bi bi-box-arrow-in-right"
                                fill="currentColor"
                                xmlns="http://www.w3.org/2000/svg"
                            >
                                <path
                                    fillRule="evenodd"
                                    d="M6 3.5a.5.5 0 0 1 .5-.5h8a.5.5 0 0 1 .5.5v9a.5.5 0 0 1-.5.5h-8a.5.5 0 0 1-.5-.5v-2a.5.5 0 0 0-1 0v2A1.5 1.5 0 0 0 6.5 14h8a1.5 1.5 0 0 0 1.5-1.5v-9A1.5 1.5 0 0 0 14.5 2h-8A1.5 1.5 0 0 0 5 3.5v2a.5.5 0 0 0 1 0v-2z"
                                ></path>
                                <path
                                    fillRule="evenodd"
                                    d="M11.854 8.354a.5.5 0 0 0 0-.708l-3-3a.5.5 0 1 0-.708.708L10.293 7.5H1.5a.5.5 0 0 0 0 1h8.793l-2.147 2.146a.5.5 0 0 0 .708.708l3-3z"
                                ></path>
                            </svg>
                        </button>
                    )}
                    cookiePolicy={"single_host_origin"}
                />
            </div>
            {theme.mode === "light" ? (
                <img
                    src={GitHubSymbol}
                    className="mx-auto d-block m-4"
                    style={{ width: 80 }}
                    alt=""
                />
            ) : (
                <img
                    src={GitHubSymbolLight}
                    className="mx-auto d-block m-4"
                    style={{ width: 80 }}
                    alt=""
                />
            )}
            <div className="col-md-12 text-center">
                <button
                    type="btn btn-primary"
                    onClick={onCLick}
                    className="btn btn-outline-primary btn-lg"
                >
                    Login with GitHub
                    <svg
                        width="1em"
                        height="1em"
                        viewBox="0 0 16 16"
                        className="bi bi-box-arrow-in-right"
                        fill="currentColor"
                        xmlns="http://www.w3.org/2000/svg"
                    >
                        <path
                            fillRule="evenodd"
                            d="M6 3.5a.5.5 0 0 1 .5-.5h8a.5.5 0 0 1 .5.5v9a.5.5 0 0 1-.5.5h-8a.5.5 0 0 1-.5-.5v-2a.5.5 0 0 0-1 0v2A1.5 1.5 0 0 0 6.5 14h8a1.5 1.5 0 0 0 1.5-1.5v-9A1.5 1.5 0 0 0 14.5 2h-8A1.5 1.5 0 0 0 5 3.5v2a.5.5 0 0 0 1 0v-2z"
                        ></path>
                        <path
                            fillRule="evenodd"
                            d="M11.854 8.354a.5.5 0 0 0 0-.708l-3-3a.5.5 0 1 0-.708.708L10.293 7.5H1.5a.5.5 0 0 0 0 1h8.793l-2.147 2.146a.5.5 0 0 0 .708.708l3-3z"
                        ></path>
                    </svg>
                </button>
            </div>
        </div>
    );
}

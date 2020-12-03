import React from "react";
import GoogleLogin from "react-google-login";
import axios from "axios";
import Success from "../Success";
import XMark from "../../x.svg";
import { toQuery } from "../utils/Routing";
import { login, Status } from "../../State";
import GitHubSymbol from "../../GitHub-Mark.png";
import GoogleLogo from "../../GoogleLogo.svg";

export default function Login() {
    const [status, setStatus] = React.useState(Status.NotSubmitted);

    const search = toQuery({
        client_id: process.env.REACT_APP_GITHUB_CLIENT_ID,
        scope: "user:email",
        redirect_uri: process.env.REACT_APP_GITHUB_REDIRECT_URI,
    });

    const onCLick = () => {
        window.location.href =
            "https://github.com/login/oauth/authorize?" + search;
    };

    const responseGoogle = (response) => {
        axios
            .post("/api/login/email", {
                email: response.profileObj.email,
            })
            .then((response) => {
                login(
                    response.data.username,
                    response.data.id,
                    response.data.isAdmin
                );
                setStatus(Status.Success);
            })
            .catch((err) => {
                onFailure(err);
            });
    };

    const onFailure = (error) => {
        console.log("Failure to retrieve code from login data: " + error);
        setStatus(Status.Failure);
    };

    if (status == Status.Success) {
        return <Success />;
    } else if (status == Status.Failure) {
        return (
            <div className="text-center">
                <img
                    src={XMark}
                    className="mx-auto w-25 d-block py-4"
                    alt="Login failed"
                />
                <h2>Login failed</h2>
                <a
                    className="btn btn-primary mt-4 btn-lg"
                    href="/login"
                    role="button"
                >
                    Try again
                </a>
            </div>
        );
    }

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
                    onSuccess={responseGoogle}
                    onFailure={responseGoogle}
                    cookiePolicy={"single_host_origin"}
                />
            </div>
            <img
                src={GitHubSymbol}
                className="mx-auto d-block m-4"
                style={{ width: 80 }}
                alt=""
            />
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

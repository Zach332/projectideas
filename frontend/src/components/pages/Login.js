import { toQuery, toParams } from "../utils/Routing";
import GitHubSymbol from "../../GitHub-Mark.png";
import GitHubSymbolLight from "../../GitHub-Mark-Light.png";
import GoogleLogo from "../../GoogleLogo.svg";
import { useGlobalState } from "../../State";
import { Helmet } from "react-helmet-async";
import { Globals } from "../../GlobalData";

export default function Login() {
    const [theme] = useGlobalState("theme");
    const params = toParams(location.search.replace(/^\?/, ""));

    const githubParams = toQuery({
        client_id: process.env.REACT_APP_GITHUB_CLIENT_ID,
        scope: "user:email",
        redirect_uri: process.env.REACT_APP_GITHUB_REDIRECT_URI,
        state: params.redirect,
    });

    const googleParams = toQuery({
        client_id:
            "449086482050-t6e9tflhou1r9b905s42pvjtbvac23hl.apps.googleusercontent.com",
        scope: "https://www.googleapis.com/auth/userinfo.email",
        response_type: "token",
        redirect_uri: process.env.REACT_APP_URL + "/login/oauth2/code/google",
        state: params.redirect,
    });

    const onClickGithub = () => {
        window.location.href =
            "https://github.com/login/oauth/authorize?" + githubParams;
    };

    const onClickGoogle = () => {
        window.location.href =
            "https://accounts.google.com/o/oauth2/v2/auth?" + googleParams;
    };

    return (
        <div className="container">
            <Helmet>
                <title>Login | {Globals.Title}</title>
                <meta
                    name="description"
                    content="Login to projectideas to post project ideas, join project teams, comment, upvote and more."
                ></meta>
            </Helmet>
            <img
                src={GoogleLogo}
                className="mx-auto d-block m-4"
                style={{ width: 80 }}
                alt=""
            />
            <div className="col-md-12 text-center">
                <button
                    type="btn btn-primary"
                    className="btn btn-outline-primary btn-lg"
                    onClick={onClickGoogle}
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
                    onClick={onClickGithub}
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

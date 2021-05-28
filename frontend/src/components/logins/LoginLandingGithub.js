import { useEffect, useState } from "react";
import XMark from "../../x.svg";
import { toParamsRedirect } from "../utils/Routing";
import axios from "axios";
import { login, Status } from "../../State";
import Spinner from "../general/Spinner";
import { Link, useHistory } from "react-router-dom";
import HomeSuccess from "./../general/HomeSuccess";
import { useToasts } from "react-toast-notifications";

export default function LoginLandingGithub() {
    const history = useHistory();
    const [status, setStatus] = useState(Status.Loading);
    const { addToast } = useToasts();

    useEffect(() => {
        const params = toParamsRedirect(
            window.location.search.replace(/^\?/, "")
        );
        onSuccess(params);
    }, []);

    const onSuccess = (data) => {
        if (!data.code) {
            onFailure(new Error("'code' not found"));
        }
        axios
            .post(process.env.REACT_APP_API + "login/github", {
                code: data.code,
            })
            .then((response) => {
                login(
                    response.data.username,
                    response.data.id,
                    response.data.admin
                );
                setStatus(Status.Success);
                if (data.state != undefined) {
                    addToast("You have logged in successfully.", {
                        appearance: "success",
                        autoDismiss: true,
                    });
                    let redirectUrl = new URL(
                        process.env.REACT_APP_URL + data.state
                    );
                    history.push({
                        pathname: redirectUrl.pathname,
                        search: redirectUrl.search,
                    });
                } else {
                    history.push("/login/oauth2/code/github");
                }
            })
            .catch((err) => {
                onFailure(err);
            });
    };
    const onFailure = (error) => {
        console.log("Failure to retrieve code from login data: " + error);
        setStatus(Status.Failure);
    };

    let result;
    if (status == Status.Success) {
        result = <HomeSuccess />;
    } else if (status == Status.Failure) {
        result = (
            <div className="text-center">
                <img
                    src={XMark}
                    className="mx-auto d-block m-4"
                    width="215px"
                    height="215px"
                    alt="Login failed"
                />
                <h2>Login failed</h2>
                <Link
                    className="btn btn-primary mt-4 btn-lg"
                    to="/login"
                    role="button"
                >
                    Try again
                </Link>
            </div>
        );
    } else if (status == Status.Loading) {
        result = <Spinner />;
    }

    return <div>{result}</div>;
}

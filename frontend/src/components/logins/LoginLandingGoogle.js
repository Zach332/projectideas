import { useEffect, useState } from "react";
import XMark from "../../x.svg";
import { toParams } from "../utils/Routing";
import axios from "axios";
import { login, Status } from "../../State";
import Spinner from "../general/Spinner";
import { Link, useHistory } from "react-router-dom";
import HomeSuccess from "./../general/HomeSuccess";

export default function LoginLandingGoogle() {
    const history = useHistory();
    const [status, setStatus] = useState(Status.Loading);

    useEffect(() => {
        const params = toParams(window.location.hash.replace(/^#/, ""));
        onSuccess(params);
    }, []);

    const onSuccess = (data) => {
        if (!data.access_token) {
            onFailure(new Error("Access token not found"));
        }
        axios
            .post("/api/login/google", {
                token: data.access_token,
            })
            .then((response) => {
                login(
                    response.data.username,
                    response.data.id,
                    response.data.admin
                );
                setStatus(Status.Success);
                history.push("/login/oauth2/code/google");
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
                    className="mx-auto w-25 d-block py-4"
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

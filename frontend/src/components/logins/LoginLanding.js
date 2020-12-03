import React, { useEffect } from "react";
import Success from "../Success";
import XMark from "../../x.svg";
import { toParams } from "../utils/Routing";
import axios from "axios";
import { login, Status } from "../../State";
import Spinner from "../general/Spinner";

export default function LoginLanding() {
    const [status, setStatus] = React.useState(Status.Loading);

    useEffect(() => {
        const params = toParams(window.location.search.replace(/^\?/, ""));
        onSuccess(params);
    }, []);

    const onSuccess = (data) => {
        if (!data.code) {
            onFailure(new Error("'code' not found"));
        }
        axios
            .post("/api/login/github", {
                code: data.code,
            })
            .then((response) => {
                login(
                    response.data.username,
                    response.data.id,
                    response.data.admin
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

    let result;
    if (status == Status.Success) {
        result = <Success />;
    } else if (status == Status.Failure) {
        result = (
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
    } else if (status == Status.Loading) {
        result = <Spinner />;
    }

    return <div>{result}</div>;
}

import axios from "axios";
import { useEffect, useState } from "react";
import { Status } from "../../State";
import Spinner from "../general/Spinner";
import Success from "../general/Success";
import XMark from "../../x.svg";
import { toParams } from "../utils/Routing";

export default function Invite() {
    const [status, setStatus] = useState(Status.Loading);
    const [message, setMessage] = useState("");
    const [projectId, setProjectId] = useState("");
    const params = toParams(location.search.replace(/^\?/, ""));

    useEffect(() => {
        axios
            .post(
                process.env.REACT_APP_API +
                    "projects/invites/" +
                    params.id +
                    "/accept"
            )
            .then((response) => {
                setProjectId(response.data);
                setStatus(Status.Success);
                setMessage(
                    "You were successfully added to the project. Go to your new project"
                );
            })
            .catch((err) => {
                if (err.response.status == 404) {
                    setStatus(Status.Failure);
                    setMessage(
                        "The invite link you used is not valid. Please ensure your invite link is up-to-date for the project you are trying to join."
                    );
                } else {
                    setStatus(Status.Failure);
                    setMessage(
                        "We were unable to add you to the project. Please ensure that you are logged in and not already a member of the project."
                    );
                }
            });
    }, []);

    if (status === Status.Success) {
        return (
            <div>
                <Success link={"/project/" + projectId} message={message} />
            </div>
        );
    }

    if (status == Status.Failure) {
        return (
            <div className="text-center">
                <img
                    src={XMark}
                    className="mx-auto d-block m-4"
                    width="215px"
                    height="215px"
                    alt="Invite failed"
                />
                <p>{message}</p>
            </div>
        );
    }

    return <Spinner />;
}

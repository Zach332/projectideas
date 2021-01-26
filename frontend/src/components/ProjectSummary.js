import React from "react";
import axios from "axios";
import { useToasts } from "react-toast-notifications";
import Modal from "./Modal";

export default function ProjectSummary({ project }) {
    const { addToast } = useToasts();
    const [joinRequestMessage, setJoinRequestMessage] = React.useState("");
    var ideaLink = "/project/" + project.id;
    const MAX_LENGTH = 480;

    const sendJoinRequest = (event) => {
        axios
            .post("/api/projects/" + project.id + "/joinrequests")
            .then(() => {
                project.userHasRequestedToJoin = true;
                addToast("Your request was submitted.", {
                    appearance: "success",
                    autoDismiss: true,
                });
            })
            .catch((err) => {
                console.log("Error submitting request: " + err);
                addToast("Your request was not submitted. Please try again.", {
                    appearance: "error",
                });
            });
        event.preventDefault();
    };

    const handleJoinRequestChange = (event) => {
        setJoinRequestMessage(event.target.value);
    };

    const joinRequestForm = (
        <div className="mx-auto">
            <form className="py-4">
                <textarea
                    className="form-control"
                    value={joinRequestMessage}
                    id="content"
                    rows="8"
                    placeholder="Enter a message to request to join"
                    onChange={handleJoinRequestChange}
                ></textarea>
            </form>
        </div>
    );

    var joinRequestButton = <div></div>;
    if (project.userIsTeamMember) {
        joinRequestButton = <div></div>;
    } else if (project.userHasRequestedToJoin) {
        joinRequestButton = (
            <button
                className="btn btn-sm btn-success float-end"
                disabled={true}
                type="button"
                id="requestToJoin"
            >
                Join request sent
            </button>
        );
    } else if (project.lookingForMembers) {
        joinRequestButton = (
            <button
                className="btn btn-sm btn-primary float-end"
                type="button"
                id="requestToJoin"
                onClick={(event) => event.preventDefault()}
                data-bs-toggle="modal"
                data-bs-target={"#sendJoinRequest" + project.id}
            >
                Request to join
            </button>
        );
    }

    return (
        <div>
            <a
                href={ideaLink}
                className="list-group-item list-group-item-action flex-column align-items-start rounded border"
            >
                <div>{joinRequestButton}</div>
                <div className="d-flex justify-content-between">
                    <h5 className="mb-1">{project.name}</h5>
                </div>
                <p
                    className="mb-1"
                    style={{
                        wordBreak: "break-word",
                    }}
                >
                    {project.description.substring(0, MAX_LENGTH)}
                    {project.description.length > MAX_LENGTH && "..."}
                </p>
            </a>
            <Modal
                id={"sendJoinRequest" + project.id}
                title={"Send join request to " + project.name}
                body={joinRequestForm}
                submit="Send"
                onClick={sendJoinRequest}
            />
        </div>
    );
}

import React from "react";
import Modal from "../layout/Modal";

export default function ProjectGitHubLinkModal({
    id,
    gitHubLink,
    setGitHubLink,
    submitLink,
}) {
    const handleLinkChange = (event) => {
        setGitHubLink(event.target.value);
    };

    const joinRequestForm = (
        <div className="mx-auto">
            <form className="py-4">
                <input
                    className="form-control"
                    value={gitHubLink}
                    placeholder="Enter a link to this project's code repository"
                    onChange={handleLinkChange}
                ></input>
            </form>
        </div>
    );

    return (
        <div>
            <Modal
                id={id}
                title={"Edit repository link"}
                body={joinRequestForm}
                submit="Submit"
                onClick={submitLink}
            />
        </div>
    );
}

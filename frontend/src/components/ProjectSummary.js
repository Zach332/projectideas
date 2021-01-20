import React from "react";

export default function ProjectSummary({ project }) {
    var ideaLink = "/project/" + project.id;
    const MAX_LENGTH = 480;

    const toggleJoinRequest = (event) => {
        event.preventDefault();
    };

    return (
        <a
            href={ideaLink}
            className="list-group-item list-group-item-action flex-column align-items-start rounded border"
        >
            {project.lookingForMembers && (
                <button
                    className="btn btn-sm btn-primary float-end"
                    type="button"
                    id="requestToJoin"
                    onClick={toggleJoinRequest}
                >
                    Request to join
                </button>
            )}
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
    );
}

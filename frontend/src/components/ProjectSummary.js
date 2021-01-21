import React from "react";

export default function ProjectSummary({ project }) {
    var ideaLink = "/project/" + project.id;
    const MAX_LENGTH = 480;
    console.log(project);

    const sendJoinRequest = (event) => {
        event.preventDefault();
    };

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
                onClick={sendJoinRequest}
            >
                Request to join
            </button>
        );
    }

    return (
        <a
            href={ideaLink}
            className="list-group-item list-group-item-action flex-column align-items-start rounded border"
        >
            {joinRequestButton}
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

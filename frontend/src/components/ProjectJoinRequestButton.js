import React from "react";

export default function ProjectJoinRequestButton({ project }) {
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

    return <div>{joinRequestButton}</div>;
}

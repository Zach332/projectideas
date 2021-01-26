import React from "react";
import ProjectJoinRequestButton from "./ProjectJoinRequestButton";
import ProjectJoinRequestModal from "./ProjectJoinRequestModal";

export default function ProjectSummary({ project }) {
    var ideaLink = "/project/" + project.id;
    const MAX_LENGTH = 480;

    return (
        <div>
            <a
                href={ideaLink}
                className="list-group-item list-group-item-action flex-column align-items-start rounded border"
            >
                <div>
                    <ProjectJoinRequestButton project={project} />
                </div>
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
            <ProjectJoinRequestModal project={project} />
        </div>
    );
}

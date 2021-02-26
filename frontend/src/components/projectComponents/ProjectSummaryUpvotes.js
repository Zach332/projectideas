import React from "react";
import Upvotes from "../postComponents/Upvotes";
import ProjectSummary from "./ProjectSummary";

export default function ProjectSummaryUpvotes({ project }) {
    return (
        <div className="d-flex align-items-center">
            <Upvotes post={project} />
            <ProjectSummary project={project} />
        </div>
    );
}

import React from "react";
import Upvotes from "../postComponents/Upvotes";
import ProjectSummary from "./ProjectSummary";

export default function ProjectSummaryUpvotes({ project, setRerender }) {
    return (
        <div className="d-flex align-items-center">
            <Upvotes post={project} postType="project" />
            <ProjectSummary project={project} setRerender={setRerender} />
        </div>
    );
}

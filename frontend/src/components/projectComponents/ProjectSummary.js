import ProjectJoinRequestButton from "./ProjectJoinRequestButton";
import ProjectJoinRequestModal from "./ProjectJoinRequestModal";
import { Link } from "react-router-dom";
import { useState } from "react";

export default function ProjectSummary({ project }) {
    const [currentProject, setCurrentProject] = useState(project);

    const submitRequest = () => {
        setCurrentProject((currentProject) => ({
            ...currentProject,
            userHasRequestedToJoin: true,
        }));
    };

    var projectLink = "/project/" + currentProject.id;
    const MAX_LENGTH = 480;

    return (
        <div className="w-100">
            <Link
                to={projectLink}
                className="list-group-item list-group-item-action flex-column align-items-start rounded border"
            >
                <div className="d-flex flex-wrap justify-content-between">
                    <h5 className="mb-1">{currentProject.name}</h5>
                    <ProjectJoinRequestButton project={currentProject} />
                </div>
                <p className="mb-1">
                    {currentProject.description.substring(0, MAX_LENGTH)}
                    {currentProject.description.length > MAX_LENGTH && "..."}
                </p>
            </Link>
            <ProjectJoinRequestModal
                project={currentProject}
                submitRequest={submitRequest}
            />
        </div>
    );
}

import ProjectJoinRequestButton from "./ProjectJoinRequestButton";
import ProjectJoinRequestModal from "./ProjectJoinRequestModal";
import { Link } from "react-router-dom";

export default function ProjectSummary({ project, setRerender }) {
    const submitRequest = () => {
        setRerender((rerender) => rerender + 1);
    };

    var projectLink = "/project/" + project.id;
    const MAX_LENGTH = 480;

    return (
        <div className="w-100">
            <Link
                to={projectLink}
                className="list-group-item list-group-item-action flex-column align-items-start rounded border"
            >
                <div className="d-flex flex-wrap justify-content-between">
                    <h5 className="mb-1">{project.name}</h5>
                    <ProjectJoinRequestButton project={project} />
                </div>
                <p className="mb-1">
                    {project.description.substring(0, MAX_LENGTH)}
                    {project.description.length > MAX_LENGTH && "..."}
                </p>
            </Link>
            <ProjectJoinRequestModal
                project={project}
                submitRequest={submitRequest}
            />
        </div>
    );
}

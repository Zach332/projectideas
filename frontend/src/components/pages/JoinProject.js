import React, { useEffect } from "react";
import { useParams } from "react-router-dom";
import { Status } from "../../State";
import NotFound from "./NotFound";
import axios from "axios";
import IdeaSummary from "../ideaComponents/IdeaSummary";
import ProjectSummary from "../projectComponents/ProjectSummary";

export default function JoinProject() {
    const [idea, setIdea] = React.useState([]);
    const [rerender, setRerender] = React.useState(0);
    const [projects, setProjects] = React.useState([]);
    const [status, setStatus] = React.useState(Status.Loading);
    let params = useParams();

    useEffect(() => {
        axios.get("/api/ideas/" + params.id).then((response) => {
            if (!response.data) {
                setStatus(Status.NotFound);
            } else {
                setIdea(response.data);
            }
        });
        axios
            .get(
                "/api/ideas/" +
                    params.id +
                    "/projects?lookingForMembersOnly=true"
            )
            .then((response) => {
                setProjects(response.data);
            });
    }, [rerender]);

    const createProject = () => {
        window.location.href = "/create/idea/" + idea.id;
    };

    if (status === Status.NotFound) {
        return <NotFound />;
    }

    const existingProjects =
        projects.length > 0 ? (
            <div className="mt-4">
                <h4>Existing teams looking for members:</h4>
                <div className="container mx-auto">
                    {projects.map((project) => (
                        <div className="my-2" key={project.id}>
                            <ProjectSummary
                                project={project}
                                setRerender={setRerender}
                            />
                        </div>
                    ))}
                </div>
            </div>
        ) : (
            <div>
                No teams are looking for new members right now. Create a new
                one!
            </div>
        );

    return (
        <div>
            <h1>Join or start a project for idea:</h1>
            <div className="m-3">
                <IdeaSummary idea={idea} />
            </div>
            <button
                type="button"
                onClick={createProject}
                className="btn btn-primary btn-md mt-3"
            >
                Create new project
            </button>
            {existingProjects}
        </div>
    );
}

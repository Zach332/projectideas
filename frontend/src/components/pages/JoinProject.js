import React, { useEffect } from "react";
import { useParams } from "react-router-dom";
import { Status } from "../../State";
import NotFound from "./NotFound";
import axios from "axios";
import IdeaSummary from "./../IdeaSummary";

export default function JoinProject() {
    const [idea, setIdea] = React.useState([]);
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
            .get("/api/ideas/" + params.id + "/projects", {
                lookingForMembersOnly: true,
            })
            .then((response) => {
                setProjects(response.data);
                console.log(projects);
            });
    }, []);

    const createProject = () => {
        window.location.href = "/create/idea/" + idea.id;
    };

    if (status === Status.NotFound) {
        return <NotFound />;
    }

    const existingProjects =
        projects.length > 0 ? (
            <div></div>
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
            {existingProjects}
            <button
                type="button"
                onClick={createProject}
                className="btn btn-primary btn-md mt-3"
            >
                Create new project
            </button>
        </div>
    );
}

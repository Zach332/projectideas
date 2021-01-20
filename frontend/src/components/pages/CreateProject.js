import React, { useEffect } from "react";
import { useParams } from "react-router-dom";
import { Status } from "../../State";
import NotFound from "./NotFound";
import axios from "axios";
import IdeaSummary from "./../IdeaSummary";
import { useToasts } from "react-toast-notifications";
import { useLeavePageWarning } from "../hooks/LeavePageWarning";

export default function CreateProject() {
    const [idea, setIdea] = React.useState([]);
    const [status, setStatus] = React.useState(Status.Loading);
    const [project, setProject] = React.useState({
        name: "",
        description: "",
        lookingForMembers: true,
    });
    let params = useParams();
    const { addToast } = useToasts();
    useLeavePageWarning(project.name != "" || project.description != "");

    useEffect(() => {
        axios.get("/api/ideas/" + params.id).then((response) => {
            if (!response.data) {
                setStatus(Status.NotFound);
            } else {
                setIdea(response.data);
            }
        });
    }, []);

    const handleSubmit = (event) => {
        axios
            .post("/api/ideas/" + idea.id + "/projects", {
                name: project.name,
                description: project.description,
            })
            .then(() => {
                setStatus(Status.Success);
            })
            .catch((err) => {
                console.log("Error creating project: " + err);
                setStatus(Status.Failure);
                addToast("Your project was not created. Please try again.", {
                    appearance: "error",
                });
            });
        event.preventDefault();
    };

    const handleInputChange = (event) => {
        const target = event.target;
        const name = target.id;
        setProject((project) => ({
            ...project,
            [name]: target.value,
        }));
    };

    if (status === Status.NotFound) {
        return <NotFound />;
    }

    return (
        <div>
            <h1>Start a project for idea:</h1>
            <div className="m-3">
                <IdeaSummary idea={idea} />
            </div>
            <form className="py-4" onSubmit={handleSubmit}>
                <div className="form-group">
                    <label htmlFor="name">Project name</label>
                    <input
                        type="text"
                        className="form-control"
                        id="name"
                        onChange={handleInputChange}
                    />
                </div>
                <div className="form-group mt-2 mb-3">
                    <label htmlFor="description">
                        Description - if you are looking for new members, add
                        any logistical details or team goals
                    </label>
                    <textarea
                        className="form-control"
                        id="description"
                        rows="5"
                        onChange={handleInputChange}
                    ></textarea>
                </div>
                <div className="form-check form-switch">
                    <input
                        className="form-check-input"
                        type="checkbox"
                        id="lookingForMembers"
                        onChange={handleInputChange}
                        checked
                    />
                    <label
                        className="form-check-label"
                        htmlFor="lookingForMembers"
                    >
                        Look for new members - your idea will show up as an
                        option if a user wants to join a team
                    </label>
                </div>
                <button
                    type="submit"
                    disabled={project.name === ""}
                    className="btn btn-primary mt-4"
                >
                    Create project
                </button>
            </form>
        </div>
    );
}

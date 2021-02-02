import React, { useEffect } from "react";
import { useParams } from "react-router-dom";
import { Status, useGlobalState } from "../../State";
import LoginWarning from "../logins/LoginWarning";
import NotFound from "./NotFound";
import Success from "../general/Success";
import axios from "axios";
import IdeaSummary from "../ideaComponents/IdeaSummary";
import { useToasts } from "react-toast-notifications";
import { useLeavePageWarning } from "../hooks/LeavePageWarning";
import TagPicker from "../tagComponents/TagPicker";

export default function CreateProject() {
    const [user] = useGlobalState("user");
    const [idea, setIdea] = React.useState([]);
    const [status, setStatus] = React.useState(Status.Loading);
    const [project, setProject] = React.useState({
        name: "",
        description: "",
        lookingForMembers: true,
        publicProject: true,
        tags: [],
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
                lookingForMembers: project.lookingForMembers,
                publicProject: project.publicProject,
                tags: project.tags,
            })
            .then(() => {
                setStatus(Status.Success);
                setProject({
                    name: "",
                    description: "",
                    lookingForMembers: true,
                });
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

    const flipLookingForMembers = () => {
        setProject((project) => ({
            ...project,
            lookingForMembers: !project.lookingForMembers,
            publicProject: true,
        }));
    };

    const flipPublicProject = () => {
        setProject((project) => ({
            ...project,
            publicProject: !project.publicProject,
        }));
    };

    if (status === Status.NotFound) {
        return <NotFound />;
    }

    if (status === Status.Success) {
        return (
            <div>
                <Success />
            </div>
        );
    }

    if (!user.loggedIn) {
        return <LoginWarning />;
    }

    return (
        <div>
            <h1>Start a project based on:</h1>
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
                        relevant logistical constraints or team goals
                    </label>
                    <textarea
                        className="form-control"
                        id="description"
                        rows="5"
                        onChange={handleInputChange}
                    ></textarea>
                </div>
                <div className="form-check form-switch mb-3">
                    <input
                        className="form-check-input"
                        type="checkbox"
                        id="lookingForMembers"
                        onClick={flipLookingForMembers}
                        defaultChecked
                    />
                    <label
                        className="form-check-label"
                        htmlFor="lookingForMembers"
                    >
                        Look for new members - your project will appear as an
                        option if a user wants to join a team
                    </label>
                </div>
                {!project.lookingForMembers && (
                    <div className="form-check form-switch mb-3">
                        <input
                            className="form-check-input"
                            type="checkbox"
                            id="publicProject"
                            onClick={flipPublicProject}
                            checked={project.publicProject}
                        />
                        <label
                            className="form-check-label"
                            htmlFor="publicProject"
                        >
                            Public - your project will appear when people browse
                            projects (anyone with the link can still see private
                            projects)
                        </label>
                    </div>
                )}
                <TagPicker
                    post={project}
                    setPost={setProject}
                    postType="project"
                />
                <br></br>
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

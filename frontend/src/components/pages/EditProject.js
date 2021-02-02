import React from "react";
import { Status } from "../../State";
import axios from "axios";
import { useToasts } from "react-toast-notifications";
import { useLeavePageWarning } from "../hooks/LeavePageWarning";
import TagPicker from "../tagComponents/TagPicker";

export default function EditProject({ originalProject, setStatus }) {
    const [edited, setEdited] = React.useState(false);
    const [project, setProject] = React.useState(originalProject);
    const { addToast } = useToasts();
    useLeavePageWarning(edited);

    const handleSubmit = (event) => {
        axios
            .put("/api/projects/" + project.id, {
                name: project.name,
                description: project.description,
                lookingForMembers: project.lookingForMembers,
                publicProject: project.publicProject,
                tags: project.tags,
                githubLink: project.githubLink,
            })
            .then(() => {
                addToast("Your idea was updated successfully.", {
                    appearance: "success",
                });
                setStatus(Status.Success);
            })
            .catch((err) => {
                console.log("Error updating project: " + err);
                addToast("Your project was not updated. Please try again.", {
                    appearance: "error",
                });
            });
        event.preventDefault();
    };

    const handleInputChange = (event) => {
        setEdited(true);
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
        }));
    };

    return (
        <div>
            <h1>Update project</h1>
            <form className="py-4" onSubmit={handleSubmit}>
                <div className="form-group">
                    <label htmlFor="name">Project name</label>
                    <input
                        type="text"
                        className="form-control"
                        value={project.name}
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
                        value={project.description}
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
                        checked={project.lookingForMembers}
                    />
                    <label
                        className="form-check-label"
                        htmlFor="lookingForMembers"
                    >
                        Look for new members - your project will appear as an
                        option if a user wants to join a team
                    </label>
                </div>
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
                    Update project
                </button>
            </form>
        </div>
    );
}

import { useState } from "react";
import { createPatch } from "rfc6902";
import { Status } from "../../State";
import axios from "axios";
import { useToasts } from "react-toast-notifications";
import { useLeavePageWarning } from "../hooks/LeavePageWarning";
import TagPicker from "../postComponents/TagPicker";
import { Helmet } from "react-helmet-async";
import { Globals } from "../../GlobalData";
import { Prompt } from "react-router-dom";

export default function EditProject({
    project,
    setStatus,
    setRerender,
    lastModified,
}) {
    const [edited, setEdited] = useState(false);
    const originalProject = JSON.parse(JSON.stringify(project));
    const [editedProject, setProject] = useState(project);
    const { addToast } = useToasts();
    useLeavePageWarning(edited);

    const handleSubmit = (event) => {
        axios
            .patch(
                "/api/projects/" + originalProject.id,
                createPatch(originalProject, editedProject),
                {
                    headers: {
                        "Content-Type": "application/json-patch+json",
                        "If-Unmodified-Since": lastModified,
                    },
                }
            )
            .then(() => {
                addToast("Your idea was updated successfully.", {
                    appearance: "success",
                    autoDismiss: true,
                });
                setStatus(Status.Loaded);
                setRerender((rerender) => rerender + 1);
            })
            .catch((err) => {
                console.log("Error updating project: " + err);
                if (err.response.status == 412) {
                    addToast(
                        "This project has been edited by another user since you began editing. " +
                            "Please save your changes elsewhere, refresh the page, and try again.",
                        {
                            appearance: "error",
                        }
                    );
                } else {
                    addToast(
                        "Your project was not updated. Please try again.",
                        {
                            appearance: "error",
                        }
                    );
                }
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
            publicProject: true,
        }));
    };

    const flipPublicProject = () => {
        setProject((project) => ({
            ...project,
            publicProject: !project.publicProject,
        }));
    };

    return (
        <div>
            <Helmet>
                <title>Edit Project | {Globals.Title}</title>
            </Helmet>
            <Prompt
                when={edited}
                message="You have unsaved changes; are you sure you want to leave?"
            />
            <h1>Update project</h1>
            <form className="py-4" onSubmit={handleSubmit}>
                <div className="form-group">
                    <label htmlFor="name">Project name</label>
                    <input
                        type="text"
                        className="form-control"
                        value={editedProject.name}
                        id="name"
                        onChange={handleInputChange}
                    />
                </div>
                {editedProject.name.length > 175 && (
                    <div>Your project name is too long.</div>
                )}
                <div className="form-group mt-2 mb-3">
                    <label htmlFor="description">
                        Description - if you are looking for new members, add
                        relevant logistical constraints or team goals
                    </label>
                    <textarea
                        className="form-control"
                        id="description"
                        value={editedProject.description}
                        rows="5"
                        onChange={handleInputChange}
                    ></textarea>
                </div>
                <div className="form-check form-switch mb-3">
                    <input
                        className="form-check-input"
                        type="checkbox"
                        id="lookingForMembers"
                        onChange={flipLookingForMembers}
                        checked={editedProject.lookingForMembers}
                    />
                    <label
                        className="form-check-label"
                        htmlFor="lookingForMembers"
                    >
                        Look for new members - your project will appear as an
                        option if a user wants to join a team
                    </label>
                </div>
                {!editedProject.lookingForMembers && (
                    <div className="form-check form-switch mb-3">
                        <input
                            className="form-check-input"
                            type="checkbox"
                            id="publicProject"
                            onChange={flipPublicProject}
                            checked={editedProject.publicProject}
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
                    post={editedProject}
                    setPost={setProject}
                    postType="project"
                />
                <br></br>
                <button
                    type="submit"
                    disabled={
                        editedProject.name === "" ||
                        editedProject.name.length > 175
                    }
                    className="btn btn-primary mt-4"
                >
                    Update project
                </button>
            </form>
        </div>
    );
}

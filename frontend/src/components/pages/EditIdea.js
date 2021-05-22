import axios from "axios";
import { createPatch } from "rfc6902";
import { useState } from "react";
import { useLeavePageWarning } from "../hooks/LeavePageWarning";
import { Status } from "../../State";
import { useToasts } from "react-toast-notifications";
import IdeaCard from "../ideaComponents/IdeaCard";
import TagPicker from "../postComponents/TagPicker";
import { Helmet } from "react-helmet-async";
import { Globals } from "../../GlobalData";
import { Link, Prompt } from "react-router-dom";

export default function EditIdea({ idea, setStatus, setRerender }) {
    const { addToast } = useToasts();
    const originalIdea = JSON.parse(JSON.stringify(idea));
    const [editedIdea, setIdea] = useState(idea);
    const [edited, setEdited] = useState(false);

    useLeavePageWarning(edited);

    const handleInputChange = (event) => {
        const target = event.target;
        const name = target.id;
        setEdited(true);
        setIdea((idea) => ({
            ...idea,
            [name]: target.value,
        }));
    };

    const handleSubmit = (event) => {
        axios
            .patch(
                "https://projectideas.herokuapp.com/api/ideas/" +
                    originalIdea.id,
                createPatch(originalIdea, editedIdea),
                {
                    headers: {
                        "Content-Type": "application/json-patch+json",
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
                console.log("Error updating idea: " + err);
                addToast("Your idea was not updated. Please try again.", {
                    appearance: "error",
                });
            });
        event.preventDefault();
    };

    return (
        <div className="mx-auto">
            <Helmet>
                <title>Edit Idea | {Globals.Title}</title>
            </Helmet>
            <Prompt
                when={edited}
                message="You have unsaved changes; are you sure you want to leave?"
            />
            <form className="py-4" onSubmit={handleSubmit}>
                <div className="form-group">
                    <label htmlFor="title">Title</label>
                    <input
                        type="text"
                        value={editedIdea.title}
                        className="form-control"
                        id="title"
                        onChange={handleInputChange}
                    />
                </div>
                {editedIdea.title.length > 175 && (
                    <div>Your title is too long.</div>
                )}
                <div className="form-group mt-2 mb-3">
                    <label htmlFor="content">Details</label>
                    <textarea
                        value={editedIdea.content}
                        className="form-control"
                        rows="10"
                        id="content"
                        onChange={handleInputChange}
                    ></textarea>
                </div>
                <TagPicker
                    post={editedIdea}
                    setPost={setIdea}
                    postType="idea"
                />
                <br></br>
                <button
                    type="submit"
                    disabled={
                        editedIdea.title === "" || editedIdea.title.length > 175
                    }
                    className="btn btn-primary"
                >
                    Update Idea
                </button>
            </form>
            <p>
                Basic{" "}
                <Link to="/markdown" target="_blank">
                    Markdown
                </Link>{" "}
                is supported. A preview of your idea is below.
            </p>
            <IdeaCard idea={editedIdea} />
        </div>
    );
}

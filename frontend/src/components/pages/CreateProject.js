import React, { useEffect } from "react";
import { useParams } from "react-router-dom";
import { Status } from "../../State";
import NotFound from "./NotFound";
import Success from "../Success";
import axios from "axios";
import IdeaSummary from "./../IdeaSummary";
import { useToasts } from "react-toast-notifications";
import { useLeavePageWarning } from "../hooks/LeavePageWarning";
import { toQuery } from "../utils/Routing";

export default function CreateProject() {
    const [idea, setIdea] = React.useState([]);
    const [status, setStatus] = React.useState(Status.Loading);
    const [tagSuggestions, setTagSuggestions] = React.useState([]);
    const [newTag, setNewTag] = React.useState("");
    const [invalidCharacter, setInvalidCharacter] = React.useState(false);
    const [project, setProject] = React.useState({
        name: "",
        description: "",
        lookingForMembers: true,
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

    useEffect(() => {
        axios.get("/api/tags/standard/project").then((response) => {
            setTagSuggestions(response.data);
        });
    }, []);

    const filterTagSuggestions = (suggestions) => {
        return suggestions.filter((tag) => !project.tags.includes(tag));
    };

    const addTag = (tagName) => {
        if (
            project.tags.length < 5 &&
            tagName.length > 0 &&
            tagName.length < 30
        ) {
            setProject((project) => ({
                ...project,
                tags: project.tags.includes(tagName)
                    ? project.tags
                    : project.tags.concat(tagName),
            }));
            setNewTag("");
            axios.get("/api/tags/standard/project").then((response) => {
                setTagSuggestions(response.data);
            });
        }
    };

    const removeTag = (tagName) => {
        setProject((project) => ({
            ...project,
            tags: project.tags.filter((tag) => tag != tagName),
        }));
    };

    const updateNewTag = (event) => {
        setNewTag(event.target.value);
        setInvalidCharacter(false);
        if (event.target.value === "") {
            axios.get("/api/tags/standard/project").then((response) => {
                setTagSuggestions(response.data);
            });
        } else {
            axios
                .get(
                    "/api/tags/suggested/project?" +
                        toQuery({ search: event.target.value })
                )
                .then((response) => {
                    setTagSuggestions(response.data);
                });
        }
    };

    const createTag = () => {
        addTag(newTag);
    };

    const handleSubmit = (event) => {
        axios
            .post("/api/ideas/" + idea.id + "/projects", {
                name: project.name,
                description: project.description,
                lookingForMembers: project.lookingForMembers,
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

    const validateTagKeyPress = (event) => {
        // enter key
        if (event.keyCode === 13) {
            createTag();
            event.preventDefault();
        } else if (
            (event.key >= "a" && event.key <= "z") ||
            event.key === "-"
        ) {
            return;
        } else if (
            (event.keyCode >= 58 && event.keyCode <= 90) ||
            (event.keyCode >= 160 && event.keyCode <= 165)
        ) {
            event.preventDefault();
            setInvalidCharacter(true);
        }
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
                        relevant logistical constraints or team goals
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
                {
                    <div className="mt-4">
                        {project.tags.map((tag) => (
                            <span
                                className="badge btn rounded-pill bg-dark me-2"
                                onClick={() => removeTag(tag)}
                                key={tag}
                            >
                                {tag}{" "}
                                <svg
                                    xmlns="http://www.w3.org/2000/svg"
                                    width="16"
                                    height="16"
                                    fill="currentColor"
                                    className="bi bi-x"
                                    viewBox="0 0 16 16"
                                >
                                    <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z" />
                                </svg>
                            </span>
                        ))}
                        {project.tags.length > 0 &&
                            project.tags.length + "/" + 5}
                    </div>
                }
                <div className="mt-1">
                    <br></br>
                </div>
                <div className="row g-3">
                    <div className="col-auto">
                        <span>Add tag</span>
                    </div>
                    <div className="col-auto">
                        <input
                            type="text"
                            className="form-control"
                            onKeyDown={validateTagKeyPress}
                            value={newTag}
                            onChange={updateNewTag}
                            placeholder="new tag"
                        />
                    </div>
                    <div className="col-auto">
                        <div
                            className="btn btn-primary mb-3"
                            onClick={createTag}
                        >
                            Add
                        </div>
                    </div>
                    {(newTag.length > 29 || invalidCharacter) && (
                        <div className="col-auto text-red">
                            Tags must be less than 30 characters, and include
                            only lowercase letters, numbers, or dashes
                        </div>
                    )}
                </div>
                {filterTagSuggestions(tagSuggestions).map((tag) => (
                    <span
                        className="badge btn rounded-pill bg-secondary me-2"
                        onClick={() => addTag(tag)}
                        key={tag}
                    >
                        {tag}
                    </span>
                ))}
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

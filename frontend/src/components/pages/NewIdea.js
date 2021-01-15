import React, { useEffect } from "react";
import axios from "axios";
import Success from "../Success";
import LoginWarning from "../logins/LoginWarning";
import {
    useGlobalState,
    Status,
    newIdeaPersistenceKey,
    post,
} from "../../State";
import { useToasts } from "react-toast-notifications";
import IdeaCard from "../IdeaCard";

export default function NewIdea() {
    const { addToast } = useToasts();
    const [idea, setIdea] = React.useState({
        title: "",
        content: "",
        tags: [],
    });
    const [tagSuggestions, setTagSuggestions] = React.useState([]);
    const [newTag, setNewTag] = React.useState("");
    const [status, setStatus] = React.useState(Status.NotSubmitted);
    const [user] = useGlobalState("user");
    const [savedIdea] = useGlobalState("newIdea");

    const handleInputChange = (event) => {
        const target = event.target;
        const name = target.id;
        setIdea((idea) => ({
            ...idea,
            [name]: target.value,
        }));
    };

    const filterTagSuggestions = (suggestions) => {
        return suggestions.filter((tag) => !idea.tags.includes(tag));
    };

    useEffect(() => {
        localStorage.setItem(newIdeaPersistenceKey, JSON.stringify(idea));
    }, [idea]);

    useEffect(() => {
        setIdea(savedIdea);
        axios.get("/api/tags/standard/idea").then((response) => {
            setTagSuggestions(response.data);
        });
    }, []);

    const addTag = (tagName) => {
        if (idea.tags.length < 5 && tagName.length > 0 && tagName.length < 30) {
            setIdea((idea) => ({
                ...idea,
                tags: idea.tags.includes(tagName)
                    ? idea.tags
                    : idea.tags.concat(tagName),
            }));
            setNewTag("");
        }
    };

    const removeTag = (tagName) => {
        setIdea((idea) => ({
            ...idea,
            tags: idea.tags.filter((tag) => tag != tagName),
        }));
    };

    const updateNewTag = (event) => {
        setNewTag(event.target.value);
    };

    const createTag = () => {
        addTag(newTag);
    };

    const handleSubmit = (event) => {
        axios
            .post("/api/ideas", {
                title: idea.title,
                content: idea.content,
                tags: idea.tags,
            })
            .then(() => {
                setStatus(Status.Success);
                post();
                setIdea({ title: "", content: "", tags: [] });
            })
            .catch((err) => {
                console.log("Error submitting post: " + err);
                setStatus(Status.Failure);
                addToast("Your post was not submitted. Please try again.", {
                    appearance: "error",
                });
            });
        event.preventDefault();
    };

    if (!user.loggedIn) {
        return <LoginWarning />;
    }

    if (status === Status.NotSubmitted || status === Status.Failure) {
        return (
            <div className="mx-auto">
                <form className="py-4" onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="title">Title</label>
                        <input
                            type="text"
                            value={idea.title}
                            className="form-control"
                            id="title"
                            onChange={handleInputChange}
                        />
                    </div>
                    <div className="form-group mt-2 mb-3">
                        <label htmlFor="content">Details</label>
                        <textarea
                            value={idea.content}
                            className="form-control"
                            id="content"
                            rows="10"
                            onChange={handleInputChange}
                        ></textarea>
                    </div>
                    {idea.tags && (
                        <div>
                            {idea.tags.map((tag) => (
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
                            {idea.tags.length > 0 && idea.tags.length + "/" + 5}
                        </div>
                    )}
                    <div className="mt-1">
                        <br></br>
                    </div>
                    <form className="row g-3">
                        <div className="col-auto">
                            <span>Add tag</span>
                        </div>
                        <div className="col-auto">
                            <input
                                type="text"
                                className="form-control"
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
                        {newTag.length > 29 && (
                            <div className="col-auto text-red">
                                Tag too long
                            </div>
                        )}
                    </form>
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
                        disabled={idea.title === ""}
                        className="btn btn-primary mt-4"
                    >
                        Post Idea
                    </button>
                </form>
                <p>
                    Basic{" "}
                    <a href="/markdown" target="_blank">
                        Markdown
                    </a>{" "}
                    is supported. A preview of your idea is below.
                </p>
                <IdeaCard title={idea.title} content={idea.content} />
            </div>
        );
    } else if (status === Status.Success) {
        return (
            <div>
                <Success />
            </div>
        );
    }
}

import { useEffect, useState } from "react";
import axios from "axios";
import { toQuery } from "../utils/Routing";

export default function TagPicker({ post, setPost, postType }) {
    const [tagSuggestions, setTagSuggestions] = useState([]);
    const [newTag, setNewTag] = useState("");
    const [invalidCharacter, setInvalidCharacter] = useState(false);

    useEffect(() => {
        axios.get("/api/tags/standard/" + postType).then((response) => {
            setTagSuggestions(response.data);
        });
    }, []);

    const filterTagSuggestions = (suggestions) => {
        return suggestions.filter((tag) => !post.tags.includes(tag));
    };

    const addTag = (tagName) => {
        if (post.tags.length < 5 && tagName.length > 0 && tagName.length < 30) {
            setPost((post) => ({
                ...post,
                tags: post.tags.includes(tagName)
                    ? post.tags
                    : post.tags.concat(tagName),
            }));
            setNewTag("");
            axios.get("/api/tags/standard/" + postType).then((response) => {
                setTagSuggestions(response.data);
            });
        }
    };

    const removeTag = (tagName) => {
        setPost((post) => ({
            ...post,
            tags: post.tags.filter((tag) => tag != tagName),
        }));
    };

    const updateNewTag = (event) => {
        setNewTag(event.target.value.toLowerCase());
        setInvalidCharacter(false);
        if (event.target.value === "") {
            axios.get("/api/tags/standard/" + postType).then((response) => {
                setTagSuggestions(response.data);
            });
        } else {
            axios
                .get(
                    "/api/tags/suggested/" +
                        postType +
                        "?" +
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

    const submitOnEnter = (event) => {
        if (event.keyCode === 13) {
            createTag();
            event.preventDefault();
        }
    };

    return (
        <div>
            {post.tags && (
                <div>
                    {post.tags.map((tag) => (
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
                    {post.tags.length > 0 && post.tags.length + "/" + 5}
                </div>
            )}
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
                        onKeyDown={submitOnEnter}
                        value={newTag}
                        onChange={updateNewTag}
                        placeholder="new tag"
                    />
                </div>
                <div className="col-auto">
                    <div className="btn btn-primary mb-3" onClick={createTag}>
                        Add
                    </div>
                </div>
                {(newTag.length > 29 || invalidCharacter) && (
                    <div className="col-auto text-red">
                        Tags must be less than 30 characters
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
        </div>
    );
}

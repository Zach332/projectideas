import React, { useEffect } from "react";
import axios from "axios";
import Success from "../general/Success";
import LoginWarning from "../logins/LoginWarning";
import {
    useGlobalState,
    Status,
    newIdeaPersistenceKey,
    post,
} from "../../State";
import { useToasts } from "react-toast-notifications";
import IdeaCard from "../ideaComponents/IdeaCard";
import TagPicker from "../tagComponents/TagPicker";
import { Helmet } from "react-helmet";
import { Globals } from "../../GlobalData";

export default function CreateIdea() {
    const { addToast } = useToasts();
    const [idea, setIdea] = React.useState({
        title: "",
        content: "",
        tags: [],
    });

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

    useEffect(() => {
        localStorage.setItem(newIdeaPersistenceKey, JSON.stringify(idea));
    }, [idea]);

    useEffect(() => {
        setIdea(savedIdea);
    }, []);

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
                <Helmet>
                    <title>Create Idea | {Globals.Title}</title>
                </Helmet>
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
                    <TagPicker post={idea} setPost={setIdea} postType="idea" />
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

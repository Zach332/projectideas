import { useEffect, useState } from "react";
import axios from "axios";
import LoginWarning from "../logins/LoginWarning";
import {
    useGlobalState,
    Status,
    newIdeaPersistenceKey,
    post,
} from "../../State";
import { useToasts } from "react-toast-notifications";
import IdeaCard from "../ideaComponents/IdeaCard";
import TagPicker from "../postComponents/TagPicker";
import { Helmet } from "react-helmet-async";
import { Globals } from "../../GlobalData";
import { Link } from "react-router-dom";
import Success from "./../general/Success";

export default function CreateIdea() {
    const { addToast } = useToasts();
    const [idea, setIdea] = useGlobalState("newIdea");

    const [status, setStatus] = useState(Status.NotSubmitted);
    const [user] = useGlobalState("user");

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

    const handleSubmit = (event) => {
        axios
            .post(process.env.REACT_APP_API + "ideas", {
                title: idea.title,
                content: idea.content,
                tags: idea.tags,
            })
            .then((response) => {
                setStatus(Status.Success);
                post();
                setIdea({
                    id: response.data,
                    title: "",
                    content: "",
                    tags: [],
                });
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
                    {idea.title.length > 175 && (
                        <div>Your title is too long.</div>
                    )}
                    <div className="form-group mt-2 mb-3">
                        <label htmlFor="content">Details</label>
                        <textarea
                            value={idea.content}
                            className="form-control"
                            rows="10"
                            id="content"
                            onChange={handleInputChange}
                        ></textarea>
                    </div>
                    <TagPicker post={idea} setPost={setIdea} postType="idea" />
                    <br></br>
                    <button
                        type="submit"
                        disabled={idea.title === "" || idea.title.length > 175}
                        className="btn btn-primary mt-4"
                    >
                        Post Idea
                    </button>
                </form>
                <p>
                    Basic{" "}
                    <Link to="/markdown" target="_blank">
                        Markdown
                    </Link>{" "}
                    is supported. A preview of your idea is below.
                </p>
                <IdeaCard idea={idea} />
            </div>
        );
    } else if (status === Status.Success) {
        return (
            <div>
                <Success
                    link={"/idea/" + idea.id}
                    message="Go to your new idea"
                />
            </div>
        );
    }
}

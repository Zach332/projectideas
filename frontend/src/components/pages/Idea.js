import React, { useEffect } from "react";
import Success from "../Success";
import { useLeavePageWarning } from "../hooks/LeavePageWarning";
import { useParams } from "react-router-dom";
import axios from "axios";
import IdeaCard from "../IdeaCard";
import NotFound from "./NotFound";
import EditIdea from "./EditIdea";
import Comments from "../Comments";
import LoginWarning from "../logins/LoginWarning";
import Modal from "../Modal";
import { useGlobalState, Status } from "../../State";
import { useToasts } from "react-toast-notifications";
import { formatTime } from "../../TimeFormatter";

export default function Idea() {
    const { addToast } = useToasts();
    const [status, setStatus] = React.useState(Status.Loading);
    const [idea, setIdea] = React.useState([]);
    const [message, setMessage] = React.useState("");
    const [user] = useGlobalState("user");
    let params = useParams();

    useLeavePageWarning(message != "");

    useEffect(() => {
        axios.get("/api/ideas/" + params.id).then((response) => {
            if (!response.data) {
                setStatus(Status.NotFound);
            } else {
                setIdea(response.data);
            }
        });
    }, []);

    const deleteIdea = () => {
        axios
            .delete("/api/ideas/" + params.id)
            .then(() => {
                setStatus(Status.Success);
                addToast("Your idea was deleted.", {
                    appearance: "success",
                    autoDismiss: true,
                });
            })
            .catch((err) => {
                console.log("Error deleting idea: " + err);
                addToast("Your idea was not deleted. Please try again.", {
                    appearance: "error",
                });
            });
    };

    const handleMessageChange = (event) => {
        setMessage(event.target.value);
    };

    const sendMessage = () => {
        axios
            .post("/api/messages/" + idea.authorUsername, {
                content: message,
            })
            .then(() => {
                addToast("Your message was sent.", {
                    appearance: "success",
                    autoDismiss: true,
                });
                setMessage("");
            })
            .catch((err) => {
                console.log("Error submitting message: " + err);
                addToast("Your message was not sent. Please try again.", {
                    appearance: "error",
                });
            });
    };

    const edit = () => {
        setStatus(Status.NotSubmitted);
    };

    const saveIdea = () => {
        axios
            .post("/api/ideas/" + idea.id + "/save", {})
            .then(() => {
                addToast("Idea saved to My Projects on your Profile.", {
                    appearance: "success",
                    autoDismiss: true,
                });
                setIdea((idea) => ({
                    ...idea,
                    savedByUser: true,
                }));
            })
            .catch((err) => {
                console.log("Error saving idea: " + err);
                addToast("The idea was not saved. Please try again.", {
                    appearance: "error",
                });
            });
    };

    const unsaveIdea = () => {
        axios
            .post("/api/ideas/" + idea.id + "/unsave", {})
            .then(() => {
                addToast("Idea unsaved.", {
                    appearance: "success",
                    autoDismiss: true,
                });
                setIdea((idea) => ({
                    ...idea,
                    savedByUser: false,
                }));
            })
            .catch((err) => {
                console.log("Error unsaving idea: " + err);
                addToast("The idea was not unsaved. Please try again.", {
                    appearance: "error",
                });
            });
    };

    if (status === Status.NotFound) {
        return <NotFound />;
    }

    if (status === Status.Success) {
        return <Success />;
    }

    if (status === Status.NotSubmitted) {
        return <EditIdea originalIdea={idea} setStatus={setStatus} />;
    }

    let more;
    if (user.username === idea.authorUsername || user.admin) {
        more = (
            <li className="list-group-item">
                <div className="dropdown">
                    <button
                        className="btn btn-secondary dropdown-toggle"
                        type="button"
                        id="dropdownMenuButton"
                        data-bs-toggle="dropdown"
                        aria-haspopup="true"
                        aria-expanded="false"
                    >
                        More
                    </button>
                    <div
                        className="dropdown-menu"
                        aria-labelledby="dropdownMenuButton"
                    >
                        <a className="dropdown-item" onClick={edit}>
                            Edit idea
                        </a>
                        <a
                            className="dropdown-item text-danger"
                            data-bs-toggle="modal"
                            data-bs-target="#deleteConfirmation"
                        >
                            Delete idea
                        </a>
                    </div>
                </div>
            </li>
        );
    }

    const messageForm = user.loggedIn ? (
        <div className="mx-auto">
            <form className="py-4">
                <textarea
                    className="form-control"
                    value={message}
                    id="content"
                    rows="8"
                    placeholder="Your message"
                    onChange={handleMessageChange}
                ></textarea>
            </form>
        </div>
    ) : (
        <LoginWarning />
    );

    return (
        <div className="container-fluid">
            <div className="row justify-content-center">
                <div className="col-lg-8 col-md-8 col-sm-auto mb-2">
                    <IdeaCard title={idea.title} content={idea.content} />
                </div>
                <div className="col-md-auto col-sm-auto">
                    <ul className="card list-group list-group-flush">
                        <li className="list-group-item">
                            By {idea.authorUsername}
                            <br></br>
                            {formatTime(idea.timePosted)}
                        </li>
                        <li className="list-group-item">
                            <button
                                type="button"
                                data-bs-toggle="modal"
                                data-bs-target="#sendMessage"
                                className="btn btn-outline-secondary btn-md"
                            >
                                Message author
                            </button>
                        </li>
                        {idea.savedByUser ? (
                            <li className="list-group-item">
                                <button
                                    type="button"
                                    className="btn btn-danger btn-md"
                                    onClick={unsaveIdea}
                                >
                                    Unsave
                                </button>
                            </li>
                        ) : (
                            <li className="list-group-item">
                                <button
                                    type="button"
                                    className="btn btn-primary btn-md"
                                    onClick={saveIdea}
                                >
                                    Save
                                </button>
                            </li>
                        )}
                        {more}
                    </ul>
                </div>
            </div>
            <div className="row justify-content-center">
                <Comments ideaId={params.id} />
            </div>
            <Modal
                id="deleteConfirmation"
                title="Delete Idea"
                body="Are you sure you want to delete this idea? The data cannot be recovered."
                submit="Delete"
                onClick={deleteIdea}
            />
            <Modal
                id="sendMessage"
                title={"Send message to " + idea.authorUsername}
                body={messageForm}
                submit="Send"
                onClick={sendMessage}
                customFooter={user.loggedIn ? null : <div></div>}
            />
        </div>
    );
}

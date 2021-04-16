import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { toQuery } from "../utils/Routing";
import axios from "axios";
import IdeaCard from "../ideaComponents/IdeaCard";
import NotFound from "./NotFound";
import EditIdea from "./EditIdea";
import Comments from "../commentComponents/Comments";
import Modal from "../layout/Modal";
import SendMessageModal from "../messageComponents/SendMessageModal";
import { useGlobalState, Status } from "../../State";
import { useToasts } from "react-toast-notifications";
import { formatTime } from "../utils/TimeFormatter";
import LoadingDiv from "./../general/LoadingDiv";
import { Helmet } from "react-helmet-async";
import { Globals } from "../../GlobalData";
import { useHistory } from "react-router-dom";
import HomeSuccess from "./../general/HomeSuccess";

export default function Idea() {
    let history = useHistory();
    const { addToast } = useToasts();
    const [status, setStatus] = useState(Status.Loading);
    const [idea, setIdea] = useState({ title: "" });
    const [user] = useGlobalState("user");
    let params = useParams();

    useEffect(() => {
        axios
            .get("/api/ideas/" + params.id)
            .then((response) => {
                if (!response.data) {
                    setStatus(Status.NotFound);
                } else {
                    setStatus(Status.Loaded);
                    setIdea(response.data);
                }
            })
            .catch(() => {
                setStatus(Status.NotFound);
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

    const edit = () => {
        setStatus(Status.NotSubmitted);
    };

    const saveIdea = () => {
        axios
            .post("/api/ideas/" + idea.id + "/save", {})
            .then(() => {
                addToast("Idea saved to Saved Projects on your Profile.", {
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

    const joinProject = () => {
        history.push("/join/idea/" + idea.id);
    };

    const searchTag = (tagName) => {
        history.push("/tags?" + toQuery({ type: "idea", tag: tagName }));
    };

    if (status === Status.NotFound) {
        return <NotFound />;
    }

    if (status === Status.Success) {
        return <HomeSuccess />;
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

    return (
        <LoadingDiv isLoading={status == Status.Loading}>
            <Helmet>
                <title>
                    {idea.title} | {Globals.Title}
                </title>
            </Helmet>
            <div className="container-fluid">
                <div className="row justify-content-center">
                    <div className="col-lg-8 col-md-8 col-sm-auto mb-2">
                        <IdeaCard idea={idea} />
                    </div>
                    <div className="col-md-3 col-sm-auto">
                        <ul className="card list-group list-group-flush">
                            <li className="list-group-item">
                                <div className="d-flex flex-wrap">
                                    <div
                                        className="me-auto"
                                        style={{
                                            wordBreak: "break-word",
                                            minWidth: 75,
                                        }}
                                    >
                                        By {idea.authorUsername}
                                        <br></br>
                                        {formatTime(idea.timeCreated)}
                                    </div>
                                    {user.loggedIn && (
                                        <div className="d-flex align-items-center">
                                            {idea.savedByUser ? (
                                                <button
                                                    type="button"
                                                    className="btn btn-danger btn-md"
                                                    onClick={unsaveIdea}
                                                >
                                                    Unsave
                                                </button>
                                            ) : (
                                                <button
                                                    type="button"
                                                    className="btn btn-primary btn-md"
                                                    onClick={saveIdea}
                                                >
                                                    Save
                                                </button>
                                            )}
                                        </div>
                                    )}
                                </div>
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
                            <li className="list-group-item">
                                <button
                                    type="button"
                                    className="btn btn-info btn-md"
                                    onClick={joinProject}
                                >
                                    Join or start a project
                                </button>
                            </li>
                            {more}
                            {idea.tags && idea.tags.length > 0 && (
                                <li className="list-group-item mw-100">
                                    {idea.tags.map((tag) => (
                                        <span
                                            className="badge btn rounded-pill btn-primary me-2"
                                            onClick={() => searchTag(tag)}
                                            key={tag}
                                        >
                                            {tag}
                                        </span>
                                    ))}
                                </li>
                            )}
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
                <SendMessageModal
                    recipient={idea.authorUsername}
                    id="sendMessage"
                />
            </div>
        </LoadingDiv>
    );
}

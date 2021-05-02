import { useEffect, useState } from "react";
import { login, logout, useGlobalState } from "../../State";
import LoginWarning from "../logins/LoginWarning";
import axios from "axios";
import { useToasts } from "react-toast-notifications";
import IdeaSummary from "../ideaComponents/IdeaSummary";
import { Status } from "./../../State";
import LoadingDiv from "../general/LoadingDiv";
import { Helmet } from "react-helmet-async";
import { Globals } from "../../GlobalData";
import MyProjects from "./MyProjects";
import { toQuery } from "../utils/Routing";
import Spinner from "../general/Spinner";

export default function Profile() {
    const { addToast } = useToasts();
    const [user] = useGlobalState("user");
    const [userData, setUserData] = useState([]);
    const [savedIdeas, setSavedIdeas] = useState([]);
    const [myIdeas, setMyIdeas] = useState([]);
    const [rerender, setRerender] = useState(0);
    const [changingUsername, setChangingUsername] = useState(false);
    const [usernameLoading, setUsernameLoading] = useState(false);
    const [status, setStatus] = useState({
        userData: Status.Loading,
        savedIdeas: Status.Loading,
        myIdeas: Status.Loading,
    });
    const [lastPage, setLastPage] = useState({
        savedIdeas: true,
        myIdeas: true,
    });
    const [page, setPage] = useState({
        savedIdeas: 1,
        myIdeas: 1,
    });

    useEffect(() => {
        if (user.loggedIn) {
            setStatus({
                userData: Status.Loading,
                savedIdeas: Status.Loading,
                myIdeas: Status.Loading,
            });
            axios.get("/api/users/" + user.id).then((response) => {
                setUserData(response.data);
                setStatus((status) => {
                    return { ...status, userData: Status.Loaded };
                });
            });
            axios
                .get(
                    "/api/users/" +
                        user.id +
                        "/savedIdeas?" +
                        toQuery({ page: page.savedIdeas })
                )
                .then((response) => {
                    setSavedIdeas(response.data.ideaPreviews);
                    setLastPage((lastPage) => ({
                        ...lastPage,
                        savedIdeas: response.data.lastPage,
                    }));
                    setStatus((status) => {
                        return { ...status, savedIdeas: Status.Loaded };
                    });
                });
            axios
                .get(
                    "/api/users/" +
                        user.id +
                        "/postedideas?" +
                        toQuery({ page: page.myIdeas })
                )
                .then((response) => {
                    setMyIdeas(response.data.ideaPreviews);
                    setLastPage((lastPage) => ({
                        ...lastPage,
                        myIdeas: response.data.lastPage,
                    }));
                    setStatus((status) => {
                        return { ...status, myIdeas: Status.Loaded };
                    });
                });
        }
    }, [rerender, page]);

    const removeIdeaFromSaved = (ideaId) => {
        axios
            .post("/api/ideas/" + ideaId + "/unsave", {})
            .then(() => {
                addToast("Idea removed from Saved Projects.", {
                    appearance: "success",
                    autoDismiss: true,
                });
                setRerender((rerender) => rerender + 1);
            })
            .catch((err) => {
                console.log("Error removing idea: " + err);
                addToast("The idea was not removed. Please try again.", {
                    appearance: "error",
                });
            });
    };

    const handleInputChange = (event) => {
        const target = event.target;
        const name = target.id;
        setUserData((userData) => ({
            ...userData,
            [name]: target.value,
        }));
    };
    const handleSubmit = (event) => {
        setUsernameLoading(true);
        axios
            .put("/api/users/" + user.id, {
                username: userData.username,
                email: userData.email,
            })
            .then(() => {
                login(userData.username, user.id, user.admin);
                addToast("Username changed successfully", {
                    appearance: "success",
                    autoDismiss: true,
                });
                setChangingUsername(false);
                setUsernameLoading(false);
            })
            .catch((err) => {
                console.log("Error changing username: " + err);
                if (err.response.status == 422) {
                    addToast(
                        "This username is already in use by another user. Please choose another.",
                        {
                            appearance: "error",
                        }
                    );
                } else {
                    addToast("Error changing username. Please try again.", {
                        appearance: "error",
                    });
                }
            });
        event.preventDefault();
    };
    const handleChange = (event) => {
        setChangingUsername(true);
        event.preventDefault();
    };

    const onCLick = () => {
        logout();
        addToast("Logged out successfully", {
            appearance: "success",
            autoDismiss: true,
        });
    };

    const nextSaved = () => {
        setPage((page) => ({ ...page, savedIdeas: page.savedIdeas + 1 }));
    };

    const previousSaved = () => {
        setPage((page) => ({ ...page, savedIdeas: page.savedIdeas - 1 }));
    };

    const nextMyIdeas = () => {
        setPage((page) => ({ ...page, myIdeas: page.myIdeas + 1 }));
    };

    const previousMyIdeas = () => {
        setPage((page) => ({ ...page, myIdeas: page.myIdeas - 1 }));
    };

    if (!user.loggedIn) {
        return <LoginWarning />;
    }

    return (
        <div>
            <Helmet>
                <title>Profile | {Globals.Title}</title>
            </Helmet>
            <h1>Profile</h1>
            <ul className="nav nav-tabs" id="myTab" role="tablist">
                <li className="nav-item" role="presentation">
                    <a
                        className="nav-link active"
                        id="user-info-tab"
                        data-bs-toggle="tab"
                        href="#user-info"
                        role="tab"
                    >
                        User Information
                    </a>
                </li>
                <li className="nav-item" role="presentation">
                    <a
                        className="nav-link"
                        id="projects-tab"
                        data-bs-toggle="tab"
                        href="#projects"
                        role="tab"
                    >
                        My Projects
                    </a>
                </li>
                <li className="nav-item" role="presentation">
                    <a
                        className="nav-link"
                        id="saved-tab"
                        data-bs-toggle="tab"
                        href="#saved"
                        role="tab"
                    >
                        Saved Ideas
                    </a>
                </li>
                <li className="nav-item" role="presentation">
                    <a
                        className="nav-link"
                        id="my-ideas-tab"
                        data-bs-toggle="tab"
                        href="#my-ideas"
                        role="tab"
                    >
                        My Ideas
                    </a>
                </li>
            </ul>
            <div className="tab-content" id="myTabContent">
                <div
                    className="tab-pane fade show active"
                    id="user-info"
                    role="tabpanel"
                >
                    <LoadingDiv isLoading={status.userData === Status.Loading}>
                        <form
                            className="my-5 row row-cols-lg-auto g-3 align-items-center"
                            onSubmit={
                                changingUsername ? handleSubmit : handleChange
                            }
                        >
                            <div className="col-12">
                                <label htmlFor="username">Username</label>
                            </div>
                            <div className="mx-sm-3 col-12">
                                <input
                                    type="text"
                                    className="form-control"
                                    id="username"
                                    defaultValue={userData.username}
                                    onChange={handleInputChange}
                                    placeholder="AwesomeNewUsername"
                                    readOnly={!changingUsername}
                                />
                            </div>
                            {usernameLoading ? (
                                <Spinner />
                            ) : changingUsername ? (
                                <div className="col-12">
                                    <button
                                        type="submit"
                                        className="btn btn-primary"
                                        disabled={
                                            userData.username.length < 3 ||
                                            userData.username.length > 30
                                        }
                                    >
                                        Submit
                                    </button>
                                </div>
                            ) : (
                                <button
                                    type="submit"
                                    className="btn btn-primary"
                                >
                                    Change
                                </button>
                            )}
                        </form>
                        <form className="my-5 row row-cols-lg-auto g-3 align-items-center">
                            <div className="col-12">
                                <label htmlFor="email">Email</label>
                            </div>
                            <div className="mx-sm-3 col-12">
                                <input
                                    type="text"
                                    className="form-control"
                                    id="email"
                                    defaultValue={userData.email}
                                    readOnly
                                />
                            </div>
                            <label
                                htmlFor="email"
                                id="emailComment"
                                className="text-muted col-12"
                            >
                                Primary email from GitHub/Google
                            </label>
                        </form>
                    </LoadingDiv>
                    <button
                        type="button"
                        onClick={onCLick}
                        className="btn btn-danger btn-md"
                    >
                        Log Out
                    </button>
                </div>
                <div className="tab-pane fade" id="projects" role="tabpanel">
                    <h5 className="my-4">My projects</h5>
                    <MyProjects noHeading={true} />
                </div>
                <div className="tab-pane fade" id="saved" role="tabpanel">
                    <h5 className="my-4">My saved ideas</h5>
                    <LoadingDiv
                        isLoading={status.savedIdeas === Status.Loading}
                    >
                        {savedIdeas.length === 0 ? (
                            <p>You haven&apos;t saved any ideas.</p>
                        ) : (
                            savedIdeas.map((idea) => (
                                <div key={idea.id} className="container-flex">
                                    <div className="row my-2">
                                        <div className="col me-auto">
                                            <IdeaSummary idea={idea} />
                                        </div>
                                        <div className="col-auto my-auto">
                                            <button
                                                className="btn btn-sm"
                                                type="button"
                                                onClick={() =>
                                                    removeIdeaFromSaved(idea.id)
                                                }
                                            >
                                                <svg
                                                    xmlns="http://www.w3.org/2000/svg"
                                                    width="25"
                                                    height="25"
                                                    fill="currentColor"
                                                    className="bi bi-dash-circle"
                                                    viewBox="0 0 16 16"
                                                >
                                                    <path
                                                        fillRule="evenodd"
                                                        d="M8 15A7 7 0 1 0 8 1a7 7 0 0 0 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"
                                                    />
                                                    <path
                                                        fillRule="evenodd"
                                                        d="M4 8a.5.5 0 0 1 .5-.5h7a.5.5 0 0 1 0 1h-7A.5.5 0 0 1 4 8z"
                                                    />
                                                </svg>
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            ))
                        )}
                        <div className="d-flex">
                            <div className="me-auto p-2">
                                {page.savedIdeas > 1 && (
                                    <button
                                        type="btn btn-primary"
                                        className="btn btn-primary btn-md"
                                        onClick={previousSaved}
                                    >
                                        Previous
                                    </button>
                                )}
                            </div>
                            <div className="p-2">
                                {!lastPage.savedIdeas && savedIdeas.length > 0 && (
                                    <button
                                        type="btn btn-primary"
                                        className="btn btn-primary btn-md"
                                        onClick={nextSaved}
                                    >
                                        Next
                                    </button>
                                )}
                            </div>
                        </div>
                    </LoadingDiv>
                </div>
                <div className="tab-pane fade" id="my-ideas" role="tabpanel">
                    <h5 className="my-4">My posted ideas</h5>
                    <LoadingDiv
                        isLoading={status.savedIdeas === Status.Loading}
                    >
                        {myIdeas.length === 0 ? (
                            <p>You haven&apos;t posted any ideas.</p>
                        ) : (
                            myIdeas.map((idea) => (
                                <div className="my-2" key={idea.id}>
                                    <IdeaSummary idea={idea} />
                                </div>
                            ))
                        )}
                        <div className="d-flex">
                            <div className="me-auto p-2">
                                {page.myIdeas > 1 && (
                                    <button
                                        type="btn btn-primary"
                                        className="btn btn-primary btn-md"
                                        onClick={previousMyIdeas}
                                    >
                                        Previous
                                    </button>
                                )}
                            </div>
                            <div className="p-2">
                                {!lastPage.myIdeas && myIdeas.length > 0 && (
                                    <button
                                        type="btn btn-primary"
                                        className="btn btn-primary btn-md"
                                        onClick={nextMyIdeas}
                                    >
                                        Next
                                    </button>
                                )}
                            </div>
                        </div>
                    </LoadingDiv>
                </div>
            </div>
        </div>
    );
}

import React, { useEffect } from "react";
import { login, logout, useGlobalState } from "../../State";
import LoginWarning from "../logins/LoginWarning";
import axios from "axios";
import { useToasts } from "react-toast-notifications";
import IdeaSummary from "./../IdeaSummary";

export default function Profile() {
    const { addToast } = useToasts();
    const [user] = useGlobalState("user");
    const [userData, setUserData] = React.useState([]);
    const [myProjects, setMyProjects] = React.useState([]);
    const [rerender, setRerender] = React.useState(0);
    const [changingUsername, setChangingUsername] = React.useState(false);

    useEffect(() => {
        if (user.loggedIn) {
            axios.get("/api/users/" + user.id).then((response) => {
                setUserData(response.data);
            });
            axios
                .get("/api/users/" + user.id + "/savedIdeas")
                .then((response) => {
                    setMyProjects(response.data);
                });
        }
    }, [rerender]);

    const removeIdeaFromProjects = (ideaId) => {
        axios
            .post("/api/ideas/" + ideaId + "/unsave", {})
            .then(() => {
                addToast("Idea removed from My Projects.", {
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
            })
            .catch((err) => {
                console.log("Error changing username: " + err);
                if (err.response.status == 409) {
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

    let usernameForm;
    if (!changingUsername) {
        usernameForm = (
            <form
                className="my-5 row row-cols-lg-auto g-3 align-items-center"
                onSubmit={handleChange}
            >
                <div className="col-12">
                    <label
                        htmlFor="username"
                        className="col-sm-2 col-form-label"
                    >
                        Username
                    </label>
                </div>
                <div className="mx-sm-3 col-12">
                    <input
                        type="text"
                        className="form-control"
                        id="username"
                        defaultValue={userData.username}
                        readOnly
                    />
                </div>
                <button type="submit" className="btn btn-primary">
                    Change
                </button>
            </form>
        );
    } else {
        usernameForm = (
            <form
                className="my-5 row row-cols-lg-auto g-3 align-items-center"
                onSubmit={handleSubmit}
            >
                <div className="col-12">
                    <label
                        htmlFor="username"
                        className="col-sm-2 col-form-label"
                    >
                        Username
                    </label>
                </div>
                <div className="mx-sm-3 col-12">
                    <input
                        type="text"
                        className="form-control"
                        id="username"
                        placeholder="AwesomeNewUsername"
                        onChange={handleInputChange}
                    />
                </div>
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
            </form>
        );
    }

    const onCLick = () => {
        logout();
        addToast("Logged out successfully", {
            appearance: "success",
            autoDismiss: true,
        });
    };

    if (!user.loggedIn) {
        return <LoginWarning />;
    }

    return (
        <div>
            <h1>Profile</h1>
            {usernameForm}
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
            <button
                type="button"
                onClick={onCLick}
                className="btn btn-danger btn-md"
            >
                Log Out
            </button>
            <h2 className="mt-4">My projects</h2>
            {myProjects.length === 0 ? (
                <p>
                    You don&apos;t have any projects. Use the &quot;work on this
                    idea&quot; button to save an idea here.
                </p>
            ) : (
                myProjects.map((idea) => (
                    <div key={idea.id} className="container">
                        <div className="row">
                            <div className="col me-auto">
                                <IdeaSummary idea={idea} />
                            </div>
                            <div className="col-auto my-auto">
                                <button
                                    className="btn btn-sm"
                                    type="button"
                                    onClick={() =>
                                        removeIdeaFromProjects(idea.id)
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
            <h2 className="mt-4">My ideas</h2>
        </div>
    );
}

import { useEffect, useState } from "react";
import axios from "axios";
import { Status } from "./../../State";
import Spinner from "../general/Spinner";
import { useToasts } from "react-toast-notifications";
import { login, logout, useGlobalState } from "../../State";
import LoadingDiv from "./../general/LoadingDiv";

export default function UserData() {
    const { addToast } = useToasts();
    const [user] = useGlobalState("user");
    const [status, setStatus] = useState(Status.Loading);
    const [userData, setUserData] = useState([]);
    const [changingUsername, setChangingUsername] = useState(false);
    const [usernameLoading, setUsernameLoading] = useState(false);

    useEffect(() => {
        axios.get("/api/users/" + user.id).then((response) => {
            setUserData(response.data);
            setStatus(Status.Loaded);
        });
    }, []);

    const handleUsernameChange = (event) => {
        const target = event.target;
        const name = target.id;
        setUserData((userData) => ({
            ...userData,
            [name]: target.value,
        }));
    };
    const handleUsernameSubmit = (event) => {
        setUsernameLoading(true);
        axios
            .put("/api/users/" + user.id, {
                username: userData.username,
                notificationPreference: userData.notificationPreference,
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
    const handleUsernameModeChange = (event) => {
        setChangingUsername(true);
        event.preventDefault();
    };

    const onLogout = () => {
        logout();
        addToast("Logged out successfully", {
            appearance: "success",
            autoDismiss: true,
        });
    };

    const isPreference = (option) => {
        return userData.notificationPreference == option;
    };

    const changeNotificationPreference = (e) => {
        let newPreference = e.target.id;
        axios
            .put("/api/users/" + user.id, {
                username: userData.username,
                notificationPreference: newPreference,
            })
            .then(() => {
                setUserData((userData) => ({
                    ...userData,
                    notificationPreference: newPreference,
                }));
                addToast("Notification preferences changed successfully", {
                    appearance: "success",
                    autoDismiss: true,
                });
            })
            .catch((err) => {
                console.log("Error updating notification preferences: " + err);
                addToast(
                    "Error updating username preferences. Please try again.",
                    {
                        appearance: "error",
                    }
                );
            });
    };

    return (
        <div>
            <LoadingDiv isLoading={status === Status.Loading}>
                <form
                    className="my-5 row row-cols-lg-auto g-3 align-items-center"
                    onSubmit={
                        changingUsername
                            ? handleUsernameSubmit
                            : handleUsernameModeChange
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
                            onChange={handleUsernameChange}
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
                        <button type="submit" className="btn btn-primary">
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
                <h5>Email Notification Preference</h5>
                <form className="mb-5">
                    <div className="form-check">
                        <input
                            className="form-check-input"
                            type="radio"
                            id="AllNewMessages"
                            onChange={changeNotificationPreference}
                            checked={isPreference("AllNewMessages")}
                        />
                        <label className="form-check-label">
                            All new messages
                        </label>
                    </div>
                    <div className="form-check">
                        <input
                            className="form-check-input"
                            type="radio"
                            id="Default"
                            onChange={changeNotificationPreference}
                            checked={isPreference("Default")}
                        />
                        <label className="form-check-label">
                            Default - We only notify you about new messages if
                            we have not recently sent you an email
                        </label>
                    </div>
                    <div className="form-check">
                        <input
                            className="form-check-input"
                            type="radio"
                            id="Unsubscribed"
                            onChange={changeNotificationPreference}
                            checked={isPreference("Unsubscribed")}
                        />
                        <label className="form-check-label">Unsubscribed</label>
                    </div>
                </form>
            </LoadingDiv>
            <button
                type="button"
                onClick={onLogout}
                className="btn btn-danger btn-md"
            >
                Log Out
            </button>
        </div>
    );
}

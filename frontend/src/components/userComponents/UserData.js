import { useEffect, useState } from "react";
import axios from "axios";
import { Status } from "./../../State";
import Spinner from "../general/Spinner";
import { useToasts } from "react-toast-notifications";
import { login, logout, useGlobalState } from "../../State";
import LoadingDiv from "./../general/LoadingDiv";
import SubscriptionPreferences from "./SubscriptionPreferences";

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

    const changeNotificationPreference = (newPreference) => {
        return new Promise((resolve, reject) => {
            axios
                .put("/api/users/" + user.id, {
                    username: userData.username,
                    notificationPreference: newPreference,
                })
                .then(() => {
                    resolve();
                })
                .catch((err) => {
                    reject(err);
                });
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
                <SubscriptionPreferences
                    preference={userData.notificationPreference}
                    setPreference={(newPreference) =>
                        setUserData((userData) => ({
                            ...userData,
                            notificationPreference: newPreference,
                        }))
                    }
                    submitPreference={changeNotificationPreference}
                />
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

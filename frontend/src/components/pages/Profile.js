import React, { useEffect } from "react";
import { login, logout, useGlobalState } from "../../State";
import LoginWarning from "../logins/LoginWarning";
import axios from "axios";
import { useToasts } from "react-toast-notifications";

export default function Profile() {
    const { addToast } = useToasts();
    const [user] = useGlobalState("user");
    const [userData, setUserData] = React.useState([]);
    const [changingUsername, setChangingUsername] = React.useState(false);

    useEffect(() => {
        if (user.loggedIn) {
            axios.get("/api/users/" + user.id).then((response) => {
                setUserData(response.data);
            });
        }
    }, []);

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
            <form className="form-inline my-5" onSubmit={handleChange}>
                <label htmlFor="username" className="col-sm-2 col-form-label">
                    Username
                </label>
                <div className="mx-sm-3">
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
            <form className="form-inline my-5" onSubmit={handleSubmit}>
                <label htmlFor="username" className="col-sm-2 col-form-label">
                    Username
                </label>
                <div className="mx-sm-3">
                    <input
                        type="text"
                        className="form-control"
                        id="username"
                        placeholder="AwesomeNewUsername"
                        onChange={handleInputChange}
                    />
                </div>
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
            <form className="form-inline my-5">
                <label htmlFor="email" className="col-sm-2 col-form-label">
                    Email
                </label>
                <div className="mx-sm-3">
                    <input
                        type="text"
                        className="form-control"
                        id="email"
                        defaultValue={userData.email}
                        readOnly
                    />
                </div>
                <small id="emailComment" className="form-text text-muted">
                    Primary email from GitHub/Google
                </small>
            </form>
            <button
                type="button"
                onClick={onCLick}
                className="btn btn-danger btn-md"
            >
                Log Out
            </button>
        </div>
    );
}

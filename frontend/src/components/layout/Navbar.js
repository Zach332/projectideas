import React, { useEffect } from "react";
import axios from "axios";
import NameLogo from "../../namelogo.png";
import { userPersistenceKey, useGlobalState } from "../../State";
import { NavLink } from "react-router-dom";

export default function Navbar() {
    const [user] = useGlobalState("user");
    const [unreadMessages, setUnreadMessages] = React.useState(0);

    useEffect(() => {
        localStorage.setItem(userPersistenceKey, JSON.stringify(user));
    }, [user]);

    useEffect(() => {
        axios.get("/api/messages/numunread").then((response) => {
            setUnreadMessages(response.data);
        });
    });

    let login;
    let userLinks;
    if (user.loggedIn) {
        userLinks = (
            <ul className="navbar-nav ms-auto" id="navbarLogin">
                <NavLink
                    className="nav-item nav-link"
                    activeClassName="nav-item nav-link active"
                    to="/messages"
                >
                    <svg
                        width="1em"
                        height="1em"
                        viewBox="0 0 16 16"
                        className="bi bi-mailbox"
                        fill="currentColor"
                        xmlns="http://www.w3.org/2000/svg"
                    >
                        <path
                            fillRule="evenodd"
                            d="M4 4a3 3 0 0 0-3 3v6h6V7a3 3 0 0 0-3-3zm0-1h8a4 4 0 0 1 4 4v6a1 1 0 0 1-1 1H1a1 1 0 0 1-1-1V7a4 4 0 0 1 4-4zm2.646 1A3.99 3.99 0 0 1 8 7v6h7V7a3 3 0 0 0-3-3H6.646z"
                        />
                        <path
                            fillRule="evenodd"
                            d="M11.793 8.5H9v-1h5a.5.5 0 0 1 .5.5v1a.5.5 0 0 1-.5.5h-1a.5.5 0 0 1-.354-.146l-.853-.854z"
                        />
                        <path d="M5 7c0 .552-.448 0-1 0s-1 .552-1 0a1 1 0 0 1 2 0z" />
                    </svg>
                    {unreadMessages > 0 && (
                        <span className="badge bg-secondary align-top">
                            {unreadMessages}
                        </span>
                    )}
                </NavLink>
                <NavLink
                    className="nav-item nav-link"
                    activeClassName="nav-item nav-link active"
                    to="/profile"
                >
                    {user.username}
                </NavLink>
            </ul>
        );
    } else {
        login = (
            <div className="navbar-nav justify-content-end" id="navbarLogin">
                <li className="navbar-btn">
                    <a
                        href="/login"
                        className="btn btn-outline-success my-2 my-sm-0"
                    >
                        Login
                    </a>
                </li>
            </div>
        );
    }

    return (
        <div>
            <nav className="navbar navbar-expand-sm navbar-dark bg-dark justify-content-between mb-3 px-2">
                <a className="navbar-brand" href="/">
                    <img src={NameLogo} alt="" />
                </a>
                <button
                    className="navbar-toggler me-auto"
                    type="button"
                    data-bs-toggle="collapse"
                    data-bs-target="#navbarNav"
                    aria-controls="navbarNav"
                    aria-expanded="false"
                    aria-label="Toggle navigation"
                >
                    <span className="navbar-toggler-icon"></span>
                </button>
                <div className="collapse navbar-collapse" id="navbarNav">
                    <ul className="navbar-nav">
                        <NavLink
                            className="nav-item nav-link"
                            activeClassName="nav-item nav-link active"
                            to="/about"
                        >
                            About
                        </NavLink>
                    </ul>
                    {userLinks}
                </div>
                {login}
            </nav>
        </div>
    );
}

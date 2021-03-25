import React from "react";
import { NavLink } from "react-router-dom";
import { useGlobalState, setTheme } from "../../State";

export default function Footer() {
    const [theme] = useGlobalState("theme");

    const switchMode = () => {
        if (theme.mode === "dark") {
            setTheme("light");
        } else {
            setTheme("dark");
        }
    };

    return (
        <div style={{ marginTop: "auto" }}>
            <nav className="navbar navbar-expand-sm navbar-dark bg-dark justify-content-center mt-3">
                <div className="form-check form-switch me-4">
                    <input
                        className="form-check-input"
                        type="checkbox"
                        id="lookingForMembers"
                        onChange={switchMode}
                        checked={theme.mode === "dark"}
                    />
                    <label
                        className={
                            "form-check-label ms-2" +
                            (theme.mode === "light" ? " text-light" : "")
                        }
                        htmlFor="lookingForMembers"
                    >
                        Dark Mode
                    </label>
                </div>
                <div className="mx-3"></div>
                <ul className="navbar-nav ms-5">
                    <NavLink
                        className="nav-item nav-link"
                        activeClassName="nav-item nav-link active"
                        to="/about"
                    >
                        About
                    </NavLink>
                    <NavLink
                        className="nav-item nav-link"
                        activeClassName="nav-item nav-link active"
                        to="/privacy"
                    >
                        Privacy
                    </NavLink>
                </ul>
            </nav>
        </div>
    );
}

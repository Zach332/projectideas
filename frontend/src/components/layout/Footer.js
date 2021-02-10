import React from "react";
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
        <div>
            <div className="form-check form-switch mb-3">
                <input
                    className="form-check-input"
                    type="checkbox"
                    id="lookingForMembers"
                    onClick={switchMode}
                    checked={theme.mode === "dark"}
                />
                <label className="form-check-label" htmlFor="lookingForMembers">
                    Dark Mode
                </label>
            </div>
        </div>
    );
}

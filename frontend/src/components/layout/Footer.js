import React from "react";

export default function Footer({ mode, setMode }) {
    const [changes, setChanges] = React.useState(0);

    const switchMode = () => {
        if (mode === "dark") {
            setMode("light");
        } else {
            setMode("dark");
        }
        if (changes > 0) window.location.reload();
        setChanges((changes) => changes + 1);
    };

    return (
        <div>
            <div className="form-check form-switch mb-3">
                <input
                    className="form-check-input"
                    type="checkbox"
                    id="lookingForMembers"
                    onClick={switchMode}
                    value={mode === "dark"}
                />
                <label className="form-check-label" htmlFor="lookingForMembers">
                    Dark Mode
                </label>
            </div>
        </div>
    );
}

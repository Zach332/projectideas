import React, { useEffect } from "react";
import { useGlobalState, themePersistenceKey } from "../../State";

export default function StyleDiv({ children, ...props }) {
    const [theme] = useGlobalState("theme");
    const [changes, setChanges] = React.useState(0);

    useEffect(() => {
        localStorage.setItem(themePersistenceKey, JSON.stringify(theme));
        if (changes > 0) {
            window.location.reload();
        }
        setChanges((changes) => changes + 1);
    }, [theme]);

    if (theme.mode === "light") {
        require("../../scss/light.scss");
        return <div {...props}>{children}</div>;
    } else {
        require("../../scss/dark.scss");
        return <div {...props}>{children}</div>;
    }
}

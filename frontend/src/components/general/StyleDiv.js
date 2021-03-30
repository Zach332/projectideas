import { useEffect, useState } from "react";
import { useGlobalState, themePersistenceKey } from "../../State";

export default function StyleDiv({ children, ...props }) {
    const [theme] = useGlobalState("theme");
    const [changes, setChanges] = useState(0);

    useEffect(() => {
        localStorage.setItem(themePersistenceKey, JSON.stringify(theme));
        if (changes > 0) {
            window.location.reload();
        }
        setChanges((changes) => changes + 1);
    }, [theme]);

    if (theme.mode === "light") {
        import("../../css/light.scss");
        import("../../css/unhide.css");
        return <div {...props}>{children}</div>;
    } else {
        import("../../css/dark.scss");
        import("../../css/unhide.css");
        return <div {...props}>{children}</div>;
    }
}

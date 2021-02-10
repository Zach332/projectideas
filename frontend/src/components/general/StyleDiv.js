import React from "react";

export default function StyleDiv({ mode, children, ...props }) {
    if (mode === "light") {
        require("../../scss/light.scss");
        return <div {...props}>{children}</div>;
    } else {
        require("../../scss/dark.scss");
        return <div {...props}>{children}</div>;
    }
}

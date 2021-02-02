import React from "react";
import Spinner from "../general/Spinner";

export default function LoadingDiv({ isLoading, children, ...props }) {
    if (!isLoading) {
        return <div {...props}>{children}</div>;
    } else {
        return <Spinner />;
    }
}

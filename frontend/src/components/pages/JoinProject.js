import React from "react";
import { useParams } from "react-router-dom";

export default function JoinProject() {
    let params = useParams();

    return <div>{params.id}</div>;
}

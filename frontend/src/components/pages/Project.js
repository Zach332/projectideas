import React, { useEffect } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";
import NotFound from "./NotFound";
import { Status } from "../../State";

export default function Project() {
    const [status, setStatus] = React.useState(Status.Loading);
    const [project, setProject] = React.useState({});
    let params = useParams();

    useEffect(() => {
        axios.get("/api/projects/" + params.id).then((response) => {
            if (!response.data) {
                setStatus(Status.NotFound);
            } else {
                setProject(response.data);
            }
        });
    }, []);

    if (status === Status.NotFound) {
        return <NotFound />;
    }

    return (
        <div>
            <h1>{project.name}</h1>
            <p style={{ whiteSpace: "pre-wrap" }}>{project.description}</p>
        </div>
    );
}

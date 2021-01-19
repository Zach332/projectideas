import React, { useEffect } from "react";
import { useParams } from "react-router-dom";
import { Status } from "../../State";
import NotFound from "./NotFound";
import axios from "axios";
import IdeaSummary from "./../IdeaSummary";

export default function CreateProject() {
    const [idea, setIdea] = React.useState([]);
    const [status, setStatus] = React.useState(Status.Loading);
    let params = useParams();

    useEffect(() => {
        axios.get("/api/ideas/" + params.id).then((response) => {
            if (!response.data) {
                setStatus(Status.NotFound);
            } else {
                setIdea(response.data);
            }
        });
    }, []);

    if (status === Status.NotFound) {
        return <NotFound />;
    }

    return (
        <div>
            <h1>Start a project for idea:</h1>
            <div className="m-3">
                <IdeaSummary idea={idea} />
            </div>
        </div>
    );
}

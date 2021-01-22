import React, { useEffect } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";
import NotFound from "./NotFound";
import { Status } from "../../State";

export default function Project() {
    const [status, setStatus] = React.useState(Status.Loading);
    const [project, setProject] = React.useState({ teamMemberUsernames: [] });
    let params = useParams();

    useEffect(() => {
        axios.get("/api/projects/" + params.id).then((response) => {
            if (!response.data) {
                setStatus(Status.NotFound);
            } else {
                setProject(response.data);
                console.log(response.data);
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
            <table className="table">
                <thead className="thead-dark">
                    <tr>
                        <th scope="col">Members</th>
                    </tr>
                </thead>
                <tbody>
                    {project.teamMemberUsernames.map((username) => (
                        <tr key={username}>
                            <td>{username}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

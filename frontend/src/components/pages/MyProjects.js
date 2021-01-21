import React, { useEffect } from "react";
import axios from "axios";
import ProjectSummary from "./../ProjectSummary";
import { useGlobalState } from "../../State";

export default function MyProjects() {
    const [projects, setProjects] = React.useState([]);
    const [user] = useGlobalState("user");

    useEffect(() => {
        axios.get("/api/users/" + user.id + "/projects").then((response) => {
            setProjects(response.data);
        });
    }, []);

    const existingProjects =
        projects.length > 0 ? (
            <div className="mt-4">
                <div className="container mx-auto">
                    {projects.map((project) => (
                        <div className="my-2" key={project.id}>
                            <ProjectSummary project={project} />
                        </div>
                    ))}
                </div>
            </div>
        ) : (
            <div>
                You don&apos;t have any projects yet. Create or join one by
                clicking the button beside an idea!
            </div>
        );

    return (
        <div>
            <h1>My Projects</h1>
            {existingProjects}
        </div>
    );
}

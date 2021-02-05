import React, { useEffect } from "react";
import axios from "axios";
import ProjectSummary from "../projectComponents/ProjectSummary";
import { useGlobalState } from "../../State";
import { Status } from "./../../State";
import LoadingDiv from "./../general/LoadingDiv";
import { Helmet } from "react-helmet";
import { Globals } from "../../GlobalData";
import LoginWarning from "./../logins/LoginWarning";

export default function MyProjects({ noHeading }) {
    const [projects, setProjects] = React.useState([]);
    const [status, setStatus] = React.useState(Status.Loading);
    const [user] = useGlobalState("user");

    useEffect(() => {
        axios.get("/api/users/" + user.id + "/projects").then((response) => {
            setProjects(response.data);
            setStatus(Status.Loaded);
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

    if (!user.loggedIn) {
        return <LoginWarning />;
    }

    return (
        <LoadingDiv isLoading={status === Status.Loading}>
            {!noHeading && (
                <Helmet>
                    <title>My Projects | {Globals.Title}</title>
                </Helmet>
            )}
            {!noHeading && <h1>My Projects</h1>}
            {existingProjects}
        </LoadingDiv>
    );
}

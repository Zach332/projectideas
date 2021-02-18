import React, { useEffect } from "react";
import axios from "axios";
import ProjectSummary from "../projectComponents/ProjectSummary";
import { useGlobalState } from "../../State";
import { Status } from "./../../State";
import LoadingDiv from "./../general/LoadingDiv";
import { Helmet } from "react-helmet";
import { Globals } from "../../GlobalData";
import LoginWarning from "./../logins/LoginWarning";
import { toQuery } from "../utils/Routing";

export default function MyProjects({ noHeading }) {
    const [projects, setProjects] = React.useState([]);
    const [status, setStatus] = React.useState(Status.Loading);
    const [user] = useGlobalState("user");
    const [page, setPage] = React.useState(1);
    const [lastPage, setLastPage] = React.useState(true);

    useEffect(() => {
        setStatus(Status.Loading);
        axios
            .get(
                "/api/users/" + user.id + "/projects?" + toQuery({ page: page })
            )
            .then((response) => {
                setProjects(response.data.projectPreviews);
                setLastPage(response.data.lastPage);
                setStatus(Status.Loaded);
            });
    }, [page]);

    const next = () => {
        setPage((page) => page + 1);
    };

    const previous = () => {
        setPage((page) => page - 1);
    };

    const existingProjects =
        projects.length > 0 ? (
            <div className="mt-4">
                <div className="container-flex mx-auto">
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
            <div className="d-flex">
                <div className="me-auto p-2">
                    {page > 1 && (
                        <button
                            type="btn btn-primary"
                            className="btn btn-primary btn-md"
                            onClick={previous}
                        >
                            Previous
                        </button>
                    )}
                </div>
                <div className="p-2">
                    {!lastPage && projects.length > 0 && (
                        <button
                            type="btn btn-primary"
                            className="btn btn-primary btn-md"
                            onClick={next}
                        >
                            Next
                        </button>
                    )}
                </div>
            </div>
        </LoadingDiv>
    );
}

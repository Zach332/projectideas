import React, { useEffect } from "react";
import axios from "axios";
import { Status } from "../../State";
import { toParams, toQuery } from "../utils/Routing";
import LoadingDiv from "./../general/LoadingDiv";
import { Helmet } from "react-helmet";
import { Globals } from "../../GlobalData";
import ProjectSummary from "./../projectComponents/ProjectSummary";

export default function Projects() {
    const [projects, setProjects] = React.useState([]);
    const [status, setStatus] = React.useState(Status.Loading);
    const [lastPage, setLastPage] = React.useState(true);
    const params = toParams(window.location.search.replace(/^\?/, ""));
    if (!params.page) params.page = 1;

    useEffect(() => {
        axios
            .get("/api/projects?" + toQuery({ page: params.page }))
            .then((response) => {
                console.log(response.data);
                setProjects(response.data.projectPreviews);
                setLastPage(response.data.lastPage);
                setStatus(Status.Success);
            });
    }, []);

    const next = () => {
        window.location.href =
            "/projects?" + toQuery({ page: parseInt(params.page) + 1 });
    };

    const previous = () => {
        window.location.href =
            "/projects?" + toQuery({ page: parseInt(params.page) - 1 });
    };

    return (
        <LoadingDiv isLoading={status == Status.Loading}>
            <Helmet>
                <title>Projects | {Globals.Title}</title>
            </Helmet>
            <h1>Projects</h1>
            <div className="container mx-auto">
                {projects.map((project) => (
                    <div className="my-2" key={project.id}>
                        <ProjectSummary project={project} />
                    </div>
                ))}
            </div>
            <div className="d-flex">
                <div className="me-auto p-2">
                    {params.page > 1 && (
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

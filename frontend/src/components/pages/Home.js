import React, { useEffect } from "react";
import IdeaSummary from "./../IdeaSummary";
import axios from "axios";
import { Status } from "../../State";
import Spinner from "../general/Spinner";
import { toParams, toQuery } from "../utils/Routing";

export default function Home() {
    const [ideas, setIdeas] = React.useState([]);
    const [status, setStatus] = React.useState(Status.Loading);
    const [lastPage, setLastPage] = React.useState(true);
    const params = toParams(window.location.search.replace(/^\?/, ""));
    if (!params.page) params.page = 1;

    useEffect(() => {
        axios
            .get("/api/ideas?" + toQuery({ page: params.page }))
            .then((response) => {
                console.log(response.data);
                setIdeas(response.data.ideaPreviews);
                setLastPage(response.data.lastPage);
                setStatus(Status.Success);
            });
    }, []);

    const onCLick = () => {
        window.location.href = "/new-idea";
    };

    const next = () => {
        window.location.href =
            "/?" + toQuery({ page: parseInt(params.page) + 1 });
    };

    const previous = () => {
        window.location.href =
            "/?" + toQuery({ page: parseInt(params.page) - 1 });
    };

    let ideaElements;
    if (status == Status.Success && ideas.length > 0) {
        ideaElements = (
            <div className="container mx-auto">
                {ideas.map((idea) => (
                    <div className="my-2" key={idea.id}>
                        <IdeaSummary idea={idea} />
                    </div>
                ))}
            </div>
        );
    } else if (status == Status.Loading) {
        ideaElements = <Spinner />;
    } else {
        ideaElements = (
            <p className="ms-2">There are no ideas posted here yet.</p>
        );
    }

    return (
        <div>
            <div className="d-flex">
                <div className="me-auto p-2">
                    <h1>Home</h1>
                </div>
                <div className="p-2">
                    <button
                        type="btn btn-primary"
                        onClick={onCLick}
                        className="btn btn-outline-primary btn-lg"
                    >
                        <svg
                            width="1em"
                            height="1em"
                            viewBox="0 0 16 16"
                            className="bi bi-plus"
                            fill="currentColor"
                            xmlns="http://www.w3.org/2000/svg"
                        >
                            <path
                                fillRule="evenodd"
                                d="M8 4a.5.5 0 0 1 .5.5v3h3a.5.5 0 0 1 0 1h-3v3a.5.5 0 0 1-1 0v-3h-3a.5.5 0 0 1 0-1h3v-3A.5.5 0 0 1 8 4z"
                            />
                        </svg>
                        New Idea
                    </button>
                </div>
            </div>
            {ideaElements}
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
                    {!lastPage && ideas.length > 0 && (
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
        </div>
    );
}

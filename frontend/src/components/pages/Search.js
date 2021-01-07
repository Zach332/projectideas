import React, { useEffect } from "react";
import IdeaSummary from "./../IdeaSummary";
import axios from "axios";
import { Status } from "../../State";
import Spinner from "../general/Spinner";
import { toParams, toQuery } from "../utils/Routing";

export default function Search() {
    const [ideas, setIdeas] = React.useState([]);
    const [status, setStatus] = React.useState(Status.Loading);
    const [lastPage, setLastPage] = React.useState(true);
    const params = toParams(window.location.search.replace(/^\?/, ""));
    if (!params.page) params.page = 1;

    useEffect(() => {
        if (params.query) {
            executeSearch();
        }
    }, []);

    const executeSearch = () => {
        axios
            .get(
                "/api/ideas/search?" +
                    toQuery({ query: params.query, page: params.page })
            )
            .then((response) => {
                setIdeas(response.data.ideaPreviews);
                setLastPage(response.data.lastPage);
                setStatus(Status.Success);
            });
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
        ideaElements = <p className="ms-2">No ideas match your search.</p>;
    }

    return (
        <div>
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

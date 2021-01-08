import React, { useEffect } from "react";
import IdeaSummary from "./../IdeaSummary";
import axios from "axios";
import { Status } from "../../State";
import Spinner from "../general/Spinner";
import { toParams, toQuery } from "../utils/Routing";

export default function Search() {
    const [ideas, setIdeas] = React.useState([]);
    const [status, setStatus] = React.useState(Status.NotSubmitted);
    const [query, setQuery] = React.useState("");
    const [lastPage, setLastPage] = React.useState(true);
    const params = toParams(window.location.search.replace(/^\?/, ""));
    if (!params.page) params.page = 1;

    useEffect(() => {
        if (params.query) {
            setQuery(params.query);
            setStatus(Status.Loading);
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
            "/search?" +
            toQuery({ query: query, page: parseInt(params.page) + 1 });
    };

    const previous = () => {
        window.location.href =
            "/search?" +
            toQuery({ query: query, page: parseInt(params.page) - 1 });
    };

    const handleInputChange = (event) => {
        setQuery(event.target.value);
        console.log("/search?" + toQuery({ query: query, page: 1 }));
    };

    const handleSubmit = () => {
        window.location.href = "/search?" + toQuery({ query: query, page: 1 });
        event.preventDefault();
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
    } else if (!(status == Status.NotSubmitted)) {
        ideaElements = <p className="ms-2">No ideas match your search.</p>;
    }

    return (
        <div>
            <form className="py-4" onSubmit={handleSubmit}>
                <div className="row w-75 mx-auto">
                    <div className="col me-auto">
                        <input
                            type="text"
                            value={query}
                            className="form-control"
                            id="title"
                            onChange={handleInputChange}
                        />
                    </div>
                    <div className="col-auto my-auto">
                        <button
                            className="btn btn-sm btn-secondary"
                            type="submit"
                        >
                            <svg
                                xmlns="http://www.w3.org/2000/svg"
                                width="16"
                                height="16"
                                fill="currentColor"
                                className="bi bi-search"
                                viewBox="0 0 16 16"
                            >
                                <path d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001c.03.04.062.078.098.115l3.85 3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85a1.007 1.007 0 0 0-.115-.1zM12 6.5a5.5 5.5 0 1 1-11 0 5.5 5.5 0 0 1 11 0z" />
                            </svg>
                        </button>
                    </div>
                </div>
            </form>
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

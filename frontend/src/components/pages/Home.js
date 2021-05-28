import { useEffect, useState } from "react";
import axios from "axios";
import { Status, useGlobalState } from "../../State";
import { toParams, toQuery } from "../utils/Routing";
import LoadingDiv from "./../general/LoadingDiv";
import { Helmet } from "react-helmet-async";
import { Globals } from "../../GlobalData";
import { useHistory, useLocation } from "react-router-dom";
import IdeaSummaryUpvotes from "../ideaComponents/IdeaSummaryUpvotes";
import Error from "./../general/Error";

export default function Home() {
    let history = useHistory();
    let location = useLocation();
    const [user] = useGlobalState("user");
    const [ideas, setIdeas] = useState([]);
    const [status, setStatus] = useState(Status.Loading);
    const [lastPage, setLastPage] = useState(true);
    const [sort, setSort] = useState("hotness");
    const params = toParams(location.search.replace(/^\?/, ""));
    if (!params.page) params.page = 1;

    useEffect(() => {
        setStatus(Status.Loading);
        axios
            .get(
                process.env.REACT_APP_API +
                    "ideas?" +
                    toQuery({ page: params.page, sort: sort })
            )
            .then((response) => {
                setIdeas(response.data.ideaPreviews);
                setLastPage(response.data.lastPage);
                setStatus(Status.Success);
            })
            .catch((err) => {
                console.log("Error retrieving homepage: " + err);
                setStatus(Status.Failure);
            });
    }, [location, sort]);

    const onClick = () => {
        history.push("/new-idea");
    };

    const next = () => {
        history.push("/?" + toQuery({ page: parseInt(params.page) + 1 }));
    };

    const previous = () => {
        history.push("/?" + toQuery({ page: parseInt(params.page) - 1 }));
    };

    let ideaElements;
    if (status == Status.Success && ideas.length > 0) {
        ideaElements = (
            <div className="container mx-auto">
                {ideas.map((idea) => (
                    <div className="my-2" key={idea.id}>
                        <IdeaSummaryUpvotes idea={idea} />
                    </div>
                ))}
            </div>
        );
    } else {
        ideaElements = (
            <p className="ms-2">There are no ideas posted here yet.</p>
        );
    }

    if (status === Status.Failure) {
        return (
            <Error
                pageTitle="Ideas"
                errorMessage="The server encountered an error. Please try reloading the page later."
                showHome={false}
            />
        );
    }

    return (
        <div>
            <Helmet>
                <title>Ideas | {Globals.Title}</title>
            </Helmet>
            {!user.loggedIn && (
                <div className="bg-light p-3">
                    <h3>Ideas with impact</h3>
                    Here, you can post and find ideas for technologies—websites,
                    apps, and more—that real people will use. If there is a
                    technology that you wish existed, post the idea here. And if
                    you want to develop a solution based on an idea, you can
                    create or join a project based on that idea.
                </div>
            )}
            <div className="d-flex align-items-center">
                <div className="p-2 me-3">
                    <h1>Ideas</h1>
                </div>
                <select
                    className="form-select w-auto me-auto"
                    onChange={(event) => setSort(event.target.value)}
                    value={sort}
                    style={{ height: 40 }}
                >
                    <option value="hotness">Hot</option>
                    <option value="upvotes">Top</option>
                    <option value="recency">New</option>
                </select>
                <div className="p-2">
                    <button
                        type="btn btn-primary"
                        onClick={onClick}
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

            <LoadingDiv isLoading={status == Status.Loading}>
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
            </LoadingDiv>
        </div>
    );
}

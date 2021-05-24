import { useEffect, useState } from "react";
import IdeaSummary from "../ideaComponents/IdeaSummary";
import axios from "axios";
import { Status } from "../../State";
import { toParams, toQuery } from "../utils/Routing";
import ProjectSummary from "./../projectComponents/ProjectSummary";
import { Helmet } from "react-helmet-async";
import { Globals } from "../../GlobalData";
import { useHistory, useLocation } from "react-router-dom";
import LoadingDiv from "../general/LoadingDiv";

export default function Tags() {
    let history = useHistory();
    let location = useLocation();
    const [posts, setPosts] = useState([]);
    const [status, setStatus] = useState(Status.Loading);
    const [lastPage, setLastPage] = useState(true);
    const params = toParams(window.location.search.replace(/^\?/, ""));
    if (!params.page) params.page = 1;

    useEffect(() => {
        setStatus(Status.Loading);
        axios
            .get(
                process.env.REACT_APP_API +
                    "" +
                    params.type +
                    "s/tags?" +
                    toQuery({ tag: params.tag, page: params.page })
            )
            .then((response) => {
                if (params.type === "idea") {
                    setPosts(response.data.ideaPreviews);
                }
                if (params.type === "project") {
                    setPosts(response.data.projectPreviews);
                }
                setLastPage(response.data.lastPage);
                setStatus(Status.Success);
            });
    }, [location]);

    const next = () => {
        history.push(
            "/tags?" +
                toQuery({
                    type: params.type,
                    tag: params.tag,
                    page: parseInt(params.page) + 1,
                })
        );
    };

    const previous = () => {
        history.push(
            "/tags?" +
                toQuery({
                    type: params.type,
                    tag: params.tag,
                    page: parseInt(params.page) - 1,
                })
        );
    };

    let postElements;
    if (posts.length > 0) {
        postElements = (
            <div className="container mx-auto">
                {posts.map((post) =>
                    params.type === "idea" ? (
                        <div className="my-2" key={post.id}>
                            <IdeaSummary idea={post} />
                        </div>
                    ) : (
                        <div className="my-2" key={post.id}>
                            <ProjectSummary project={post} />
                        </div>
                    )
                )}
            </div>
        );
    } else {
        postElements = (
            <p className="ms-2">No {params.type}s match this tag.</p>
        );
    }

    return (
        <div>
            <Helmet>
                <title>
                    {params.type.charAt(0).toUpperCase() + params.type.slice(1)}
                    {"s "}
                    matching tag: {params.tag} | {Globals.Title}
                </title>
            </Helmet>
            <div className="d-flex">
                <div className="me-auto p-2">
                    <h1>Tag: {params.tag}</h1>
                </div>
            </div>
            <LoadingDiv isLoading={status == Status.Loading}>
                {postElements}
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
                        {!lastPage && posts.length > 0 && (
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

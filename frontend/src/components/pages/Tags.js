import React, { useEffect } from "react";
import IdeaSummary from "../ideaComponents/IdeaSummary";
import axios from "axios";
import { Status } from "../../State";
import Spinner from "../general/Spinner";
import { toParams, toQuery } from "../utils/Routing";
import ProjectSummary from "./../projectComponents/ProjectSummary";
import { Helmet } from "react-helmet";
import { Globals } from "../../GlobalData";

export default function Tags() {
    const [posts, setPosts] = React.useState([]);
    const [status, setStatus] = React.useState(Status.Loading);
    const [lastPage, setLastPage] = React.useState(true);
    const params = toParams(window.location.search.replace(/^\?/, ""));
    if (!params.page) params.page = 1;

    useEffect(() => {
        axios
            .get(
                "/api/" +
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
    }, []);

    const next = () => {
        window.location.href =
            "/tags?" +
            toQuery({
                type: params.type,
                tag: params.tag,
                page: parseInt(params.page) + 1,
            });
    };

    const previous = () => {
        window.location.href =
            "/tags?" +
            toQuery({
                type: params.type,
                tag: params.tag,
                page: parseInt(params.page) - 1,
            });
    };

    let postElements;
    if (status == Status.Success && posts.length > 0) {
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
    } else if (status == Status.Loading) {
        postElements = <Spinner />;
    } else {
        postElements = (
            <p className="ms-2">No {params.type}s match this tag.</p>
        );
    }

    return (
        <div>
            <Helmet>
                <title>
                    {params.type.charAt(0).toUpperCase() + params.type.slice(1)}{" "}
                    Tags matching {params.tag} | {Globals.Title}
                </title>
            </Helmet>
            <div className="d-flex">
                <div className="me-auto p-2">
                    <h1>Tag: {params.tag}</h1>
                </div>
            </div>
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
        </div>
    );
}

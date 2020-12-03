import React from "react";

export default function IdeaSummary({ idea }) {
    const removeMd = require("remove-markdown");
    var ideaLink = "/idea/" + idea.id;
    var date = new Date(idea.timePosted * 1000);

    return (
        <a
            href={ideaLink}
            className="list-group-item list-group-item-action flex-column align-items-start my-3 rounded border"
        >
            <div className="d-flex w-100 justify-content-between">
                <h5 className="mb-1">{idea.title}</h5>
                <small className="text-muted">
                    {date.toLocaleDateString()}
                </small>
            </div>
            <p className="mb-1">{removeMd(idea.content)}</p>
            <small className="text-muted">By {idea.authorUsername}</small>
        </a>
    );
}

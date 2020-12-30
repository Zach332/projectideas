import React from "react";
import { formatTime } from "../TimeFormatter";

export default function IdeaSummary({ idea }) {
    const removeMd = require("remove-markdown");
    var ideaLink = "/idea/" + idea.id;
    const MAX_LENGTH = 320;

    return (
        <a
            href={ideaLink}
            className="list-group-item list-group-item-action flex-column align-items-start my-3 rounded border"
        >
            <div className="d-flex justify-content-between">
                <h5 className="mb-1">{idea.title}</h5>
                <small className="text-muted">
                    {formatTime(idea.timePosted)}
                </small>
            </div>
            <p
                className="mb-1"
                style={{
                    wordBreak: "break-word",
                }}
            >
                {removeMd(idea.content).substring(0, MAX_LENGTH)}
                {removeMd(idea.content).length > MAX_LENGTH && "..."}
            </p>
            <small className="text-muted">By {idea.authorUsername}</small>
        </a>
    );
}

import { formatTime } from "../utils/TimeFormatter";
import { Link } from "react-router-dom";

export default function IdeaSummary({ idea }) {
    const removeMd = require("remove-markdown");
    var ideaLink = "/idea/" + idea.id;
    const MAX_LENGTH = 320;

    return (
        <Link
            to={ideaLink}
            className="list-group-item list-group-item-action flex-column align-items-start rounded border"
        >
            <div className="d-flex flex-wrap justify-content-between">
                <h5 className="mb-1">{idea.title}</h5>
                <small className="text-muted">
                    {formatTime(idea.timeCreated)}
                </small>
            </div>
            <p className="mb-1">
                {removeMd(idea.content).substring(0, MAX_LENGTH)}
                {removeMd(idea.content).length > MAX_LENGTH && "..."}
            </p>
            <small className="text-muted">By {idea.authorUsername}</small>
        </Link>
    );
}

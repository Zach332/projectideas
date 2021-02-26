import React from "react";
import Upvotes from "../postComponents/Upvotes";
import IdeaSummary from "./IdeaSummary";

export default function IdeaSummaryUpvotes({ idea }) {
    return (
        <div className="d-flex align-items-center">
            <Upvotes idea={idea} />
            <IdeaSummary idea={idea} />
        </div>
    );
}

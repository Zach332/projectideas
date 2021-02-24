import React from "react";
import IdeaSummary from "./IdeaSummary";

export default function IdeaSummaryUpvotes({ idea }) {
    const filledUpvote = (
        <svg
            xmlns="http://www.w3.org/2000/svg"
            width="28"
            height="28"
            fill="currentColor"
            className="bi bi-caret-up-fill text-primary"
            viewBox="0 0 16 16"
        >
            <path d="M7.247 4.86l-4.796 5.481c-.566.647-.106 1.659.753 1.659h9.592a1 1 0 0 0 .753-1.659l-4.796-5.48a1 1 0 0 0-1.506 0z" />
        </svg>
    );
    const unfilledUpvote = (
        <svg
            xmlns="http://www.w3.org/2000/svg"
            width="28"
            height="28"
            fill="currentColor"
            className="bi bi-caret-up"
            viewBox="0 0 16 16"
        >
            <path d="M3.204 11h9.592L8 5.519 3.204 11zm-.753-.659l4.796-5.48a1 1 0 0 1 1.506 0l4.796 5.48c.566.647.106 1.659-.753 1.659H3.204a1 1 0 0 1-.753-1.659z" />
        </svg>
    );
    return (
        <div className="d-flex align-items-center">
            <div
                className="d-flex flex-column align-items-center"
                style={{ width: 50 }}
            >
                <div>{idea.userHasUpvoted ? unfilledUpvote : filledUpvote}</div>
                <div>{"375"}</div>
            </div>
            <IdeaSummary idea={idea}></IdeaSummary>
        </div>
    );
}

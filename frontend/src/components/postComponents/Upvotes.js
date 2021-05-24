import { useEffect, useState } from "react";
import axios from "axios";
import { useGlobalState } from "../../State";

export default function Upvotes({ post, postType }) {
    const [userHasUpvoted, setUserHasUpvoted] = useState(post.userHasUpvoted);
    const [upvotes, setUpvotes] = useState(post.upvoteCount);
    const [user] = useGlobalState("user");

    useEffect(() => {
        setUpvotes(post.upvoteCount);
        setUserHasUpvoted(post.userHasUpvoted);
    }, [post]);

    const toggleUpvote = () => {
        if (userHasUpvoted) {
            setUpvotes((prev) => prev - 1);
            axios.post(
                process.env.REACT_APP_API +
                    "" +
                    postType +
                    "s/" +
                    post.id +
                    "/unupvote"
            );
        } else {
            setUpvotes((prev) => prev + 1);
            axios.post(
                process.env.REACT_APP_API +
                    "" +
                    postType +
                    "s/" +
                    post.id +
                    "/upvote"
            );
        }
        setUserHasUpvoted((prev) => !prev);
    };

    const filledUpvote = (
        <svg
            xmlns="http://www.w3.org/2000/svg"
            width="28"
            height="15"
            fill="currentColor"
            className="bi bi-caret-up-fill text-primary"
            viewBox="0 4 16 7"
        >
            <path d="M7.247 4.86l-4.796 5.481c-.566.647-.106 1.659.753 1.659h9.592a1 1 0 0 0 .753-1.659l-4.796-5.48a1 1 0 0 0-1.506 0z" />
        </svg>
    );
    const unfilledUpvote = (
        <svg
            xmlns="http://www.w3.org/2000/svg"
            width="28"
            height="15"
            fill="currentColor"
            className="bi bi-caret-up"
            viewBox="0 4 16 7"
        >
            <path d="M3.204 11h9.592L8 5.519 3.204 11zm-.753-.659l4.796-5.48a1 1 0 0 1 1.506 0l4.796 5.48c.566.647.106 1.659-.753 1.659H3.204a1 1 0 0 1-.753-1.659z" />
        </svg>
    );

    if (upvotes === undefined) {
        return <div></div>;
    }

    return (
        <button
            className="btn d-flex flex-column align-items-center p-0 border-0"
            style={{ width: 50, boxShadow: "none" }}
            onClick={toggleUpvote}
            disabled={!user.loggedIn}
        >
            <div>{userHasUpvoted ? filledUpvote : unfilledUpvote}</div>
            <div>{upvotes}</div>
        </button>
    );
}

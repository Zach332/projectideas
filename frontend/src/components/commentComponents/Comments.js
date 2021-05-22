import { useEffect, useState } from "react";
import axios from "axios";
import { useToasts } from "react-toast-notifications";
import Comment from "./Comment";
import { useGlobalState } from "../../State";
import { motion, AnimateSharedLayout } from "framer-motion";

export default function Comments({ ideaId }) {
    const { addToast } = useToasts();
    const [user] = useGlobalState("user");
    const [rows, setRows] = useState(1);
    const [showButton, setShowButton] = useState("hidden");
    const [comment, setComment] = useState("");
    const [comments, setComments] = useState([]);
    const [rerender, setRerender] = useState(0);
    var commentInput;

    const onFocus = () => {
        setRows(Math.max(comment.split("\n").length, 5));
        setShowButton("visible");
    };
    const onBlur = () => {
        if (comment === "") {
            setRows(1);
            setShowButton("hidden");
        }
    };

    const setNewComment = (quoteUsername, quoteContent) => {
        setComment(
            (comment) =>
                comment +
                "> " +
                quoteUsername +
                ":\n" +
                quoteContent.replace(/^/gm, "> ") +
                "\n"
        );
        commentInput.focus();
    };

    useEffect(() => {
        if (comment != "") {
            setRows(Math.max(comment.split("\n").length, 5));
        }
    }, [comment]);

    useEffect(() => {
        axios
            .get(
                "https://projectideas.herokuapp.com/api/ideas/" +
                    ideaId +
                    "/comments"
            )
            .then((response) => {
                setComments(response.data);
            });
    }, [rerender]);

    const handleInputChange = (event) => {
        setComment(event.target.value);
    };
    const handleSubmit = (event) => {
        axios
            .post(
                "https://projectideas.herokuapp.com/api/ideas/" +
                    ideaId +
                    "/comments",
                {
                    content: comment,
                }
            )
            .then(() => {
                setComment("");
                setRows(1);
                setShowButton("hidden");
                setRerender((rerender) => rerender + 1);
                addToast("Your comment was added successfully.", {
                    appearance: "success",
                    autoDismiss: true,
                });
            })
            .catch((err) => {
                console.log("Error submitting comment: " + err);
                addToast("Your comment was not submitted. Please try again.", {
                    appearance: "error",
                });
            });
        event.preventDefault();
    };

    return (
        <AnimateSharedLayout>
            <div className={"mt-5" + (window.innerWidth <= 768 ? "" : " w-75")}>
                <form className="mb-3" onSubmit={handleSubmit}>
                    <div className="form-row align-items-top">
                        <div className="col w-100">
                            <textarea
                                type="text"
                                className="form-control mb-2"
                                id="inlineFormInput"
                                ref={(ref) => {
                                    commentInput = ref;
                                }}
                                value={comment}
                                rows={rows}
                                onBlur={onBlur}
                                onFocus={onFocus}
                                onChange={handleInputChange}
                                disabled={!user.loggedIn}
                                placeholder={
                                    user.loggedIn
                                        ? "Add a comment"
                                        : "Login to comment"
                                }
                            />
                        </div>
                        <div className="col-auto align-top">
                            <button
                                type="submit"
                                disabled={comment === ""}
                                className="btn btn-primary mb-1"
                                style={{ visibility: showButton }}
                            >
                                Submit
                            </button>
                        </div>
                    </div>
                </form>
                <motion.div layout>
                    {comments.map((comment) => (
                        <Comment
                            key={comment.id}
                            comment={comment}
                            parentId={ideaId}
                            setRerender={setRerender}
                            setNewComment={setNewComment}
                        />
                    ))}
                </motion.div>
            </div>
        </AnimateSharedLayout>
    );
}

import React, { useEffect } from "react";
import axios from "axios";
import { useToasts } from "react-toast-notifications";
import Comment from "./Comment";
import { useGlobalState } from "../State";
import { motion, AnimateSharedLayout } from "framer-motion";

export default function Comments({ ideaId }) {
    const { addToast } = useToasts();
    const [user] = useGlobalState("user");
    const [rows, setRows] = React.useState(1);
    const [showButton, setShowButton] = React.useState("hidden");
    const [comment, setComment] = React.useState("");
    const [comments, setComments] = React.useState([]);
    const [rerender, setRerender] = React.useState(0);

    const onFocus = () => {
        setRows(5);
        setShowButton("visible");
    };
    const onBlur = () => {
        if (comment === "") {
            setRows(1);
            setShowButton("hidden");
        }
    };

    useEffect(() => {
        axios.get("/api/ideas/" + ideaId + "/comments").then((response) => {
            setComments(response.data);
        });
    }, [rerender]);

    const handleInputChange = (event) => {
        setComment(event.target.value);
    };
    const handleSubmit = (event) => {
        axios
            .post("/api/ideas/" + ideaId + "/comments", {
                content: comment,
            })
            .then(() => {
                setComment("");
                setRows(1);
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
            <div className="w-75 mt-5">
                <form className="mb-3" onSubmit={handleSubmit}>
                    <div className="form-row align-items-top">
                        <div className="col w-100">
                            <textarea
                                type="text"
                                className="form-control mb-2"
                                id="inlineFormInput"
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
                                className="btn btn-primary mb-2"
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
                        />
                    ))}
                </motion.div>
            </div>
        </AnimateSharedLayout>
    );
}

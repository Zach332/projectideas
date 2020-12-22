import React from "react";
import axios from "axios";
import Modal from "./Modal";
import { useLeavePageWarning } from "./hooks/LeavePageWarning";
import { motion } from "framer-motion";
import { useToasts } from "react-toast-notifications";

export default function Message({ message, setRerender }) {
    const [messageToSend, setMessageToSend] = React.useState("");

    useLeavePageWarning(messageToSend != "");

    const { addToast } = useToasts();

    const handleMessageChange = (event) => {
        setMessageToSend(event.target.value);
    };

    const messageForm = (
        <div className="mx-auto">
            <form className="py-4">
                <textarea
                    className="form-control"
                    value={messageToSend}
                    id="content"
                    rows="8"
                    placeholder="Your message"
                    onChange={handleMessageChange}
                ></textarea>
            </form>
        </div>
    );

    const sendMessage = () => {
        axios
            .post("/api/messages/" + message.senderUsername, {
                content: messageToSend,
            })
            .then(() => {
                addToast("Your message was sent.", {
                    appearance: "success",
                });
                setMessageToSend("");
            })
            .catch((err) => {
                console.log("Error submitting message: " + err);
                addToast("Your message was not sent. Please try again.", {
                    appearance: "error",
                });
            });
    };

    const deleteMessage = () => {
        axios
            .delete("/api/messages/received/" + message.id)
            .then(() => {
                addToast("The message was deleted.", {
                    appearance: "success",
                    autoDismiss: true,
                });
                setRerender((rerender) => rerender + 1);
            })
            .catch((err) => {
                console.log("Error deleting message: " + err);
                addToast("The message was not deleted. Please try again.", {
                    appearance: "error",
                });
            });
    };

    return (
        <motion.div
            layout
            className="list-group-item flex-column align-items-start my-2 rounded border"
        >
            <div className="dropdown">
                <button
                    className="btn btn-sm btn-outline-secondary float-end"
                    type="button"
                    id="dropdownMenuButton"
                    data-bs-toggle="dropdown"
                    aria-haspopup="true"
                    aria-expanded="false"
                >
                    <svg
                        width="1em"
                        height="1em"
                        viewBox="0 0 16 16"
                        className="bi bi-three-dots-vertical"
                        fill="currentColor"
                        xmlns="http://www.w3.org/2000/svg"
                    >
                        <path
                            fillRule="evenodd"
                            d="M9.5 13a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0zm0-5a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0zm0-5a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0z"
                        />
                    </svg>
                </button>
                <div
                    className="dropdown-menu"
                    aria-labelledby="dropdownMenuButton"
                >
                    <a
                        className="dropdown-item"
                        data-bs-toggle="modal"
                        data-bs-target={"#sendMessage" + message.id}
                    >
                        Reply
                    </a>
                    <a
                        className="dropdown-item text-danger"
                        data-bs-toggle="modal"
                        data-bs-target={"#deleteMessage" + message.id}
                    >
                        Delete message
                    </a>
                </div>
            </div>
            <h6 className="card-subtitle my-2">
                {message.unread && (
                    <span className="badge badge-pill bg-primary me-2">
                        New
                    </span>
                )}
                {message.senderUsername != null
                    ? "From " + message.senderUsername
                    : "To " + message.recipientUsername}
                <span className="text-muted">
                    {" "}
                    on {new Date(message.timeSent * 1000).toLocaleDateString()}
                </span>
            </h6>
            <p className="mb-1 ms-2" style={{ whiteSpace: "pre" }}>
                {message.content}
            </p>
            <Modal
                id={"sendMessage" + message.id}
                title={
                    "Send message to " +
                    (message.senderUsername != null
                        ? message.senderUsername
                        : message.recipientUsername)
                }
                body={messageForm}
                submit="Send"
                onClick={sendMessage}
            />
            <Modal
                id={"deleteMessage" + message.id}
                title="Delete message"
                body="Are you sure you want to delete this message?"
                submit="Delete"
                onClick={deleteMessage}
            />
        </motion.div>
    );
}

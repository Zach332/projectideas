import axios from "axios";
import { motion } from "framer-motion";
import SendMessageModal from "./SendMessageModal";
import { formatTime } from "../utils/TimeFormatter";
import Modal from "../layout/Modal";
import { useToasts } from "react-toast-notifications";
import { Link } from "react-router-dom";

export default function Message({ message, setRerender }) {
    const { addToast } = useToasts();

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

    let recipientHeader;
    if (message.senderUsername != null) {
        if (message.groupMessage) {
            recipientHeader = (
                <div>
                    From {message.senderUsername} to team:{" "}
                    <Link to={"/project/" + message.recipientProjectId}>
                        {message.recipientProjectName}
                    </Link>
                </div>
            );
        } else {
            recipientHeader = <div>From {message.senderUsername}</div>;
        }
    } else {
        if (message.groupMessage) {
            recipientHeader = (
                <div>
                    To team:{" "}
                    <Link to={"/project/" + message.recipientProjectId}>
                        {message.recipientProjectName}
                    </Link>
                </div>
            );
        } else {
            recipientHeader = <div>To {message.recipientUsername}</div>;
        }
    }

    return (
        <motion.div
            layout
            className={
                "list-group-item flex-column align-items-start my-2 rounded border" +
                (message.groupMessage ? " border border-primary" : "")
            }
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
                    {message.groupMessage && (
                        <a
                            className="dropdown-item"
                            data-bs-toggle="modal"
                            data-bs-target={"#sendGroupMessage" + message.id}
                        >
                            Reply all
                        </a>
                    )}
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
                {recipientHeader}
                <span className="text-muted">
                    {" "}
                    {formatTime(message.timeSent)}
                </span>
            </h6>
            <div className="mb-1 ms-2" style={{ whiteSpace: "pre-wrap" }}>
                {message.content}
            </div>
            <Modal
                id={"deleteMessage" + message.id}
                title="Delete message"
                body="Are you sure you want to delete this message?"
                submit="Delete"
                onClick={deleteMessage}
            />
            <SendMessageModal
                recipient={
                    message.senderUsername != null
                        ? message.senderUsername
                        : message.recipientUsername
                }
                id={"sendMessage" + message.id}
            />
            <SendMessageModal
                recipient={message.recipientProjectName}
                recipientId={message.recipientProjectId}
                id={"sendGroupMessage" + message.id}
                isProject={true}
            />
        </motion.div>
    );
}

import { motion } from "framer-motion";
import axios from "axios";
import { useToasts } from "react-toast-notifications";

export default function ProjectJoinRequestPreview({
    request,
    project,
    acceptRequest,
    denyRequest,
}) {
    const { addToast } = useToasts();

    const respondToJoinRequest = (accept) => {
        axios
            .post(
                "/api/projects/" +
                    project.id +
                    "/joinrequests/" +
                    encodeURIComponent(request.username) +
                    "?accept=" +
                    accept
            )
            .then(() => {
                if (accept) {
                    acceptRequest(request.username);
                    addToast("Member added successfully.", {
                        appearance: "success",
                        autoDismiss: true,
                    });
                } else {
                    denyRequest(request.username);
                }
            })
            .catch((err) => {
                console.log("Error responding to join request: " + err);
                addToast("An error occurred. Please try again.", {
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
                    Respond
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="16"
                        height="16"
                        fill="currentColor"
                        className="bi bi-caret-down-fill"
                        viewBox="0 0 16 16"
                    >
                        <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z" />
                    </svg>
                </button>
                <div
                    className="dropdown-menu"
                    aria-labelledby="dropdownMenuButton"
                >
                    <a
                        className="dropdown-item"
                        onClick={() => respondToJoinRequest(true)}
                    >
                        Accept
                    </a>
                    <a
                        className="dropdown-item text-danger"
                        onClick={() => respondToJoinRequest(false)}
                    >
                        Deny
                    </a>
                </div>
            </div>
            <h6 className="card-subtitle my-2">{"From " + request.username}</h6>
            <p className="mb-1 ms-2" style={{ whiteSpace: "pre" }}>
                {request.requestMessage}
            </p>
        </motion.div>
    );
}

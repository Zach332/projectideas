import React, { useEffect } from "react";
import { useParams } from "react-router-dom";
import Success from "../general/Success";
import axios from "axios";
import NotFound from "./NotFound";
import { Status } from "../../State";
import { useToasts } from "react-toast-notifications";
import { motion, AnimateSharedLayout } from "framer-motion";
import ProjectJoinRequestButton from "../projectComponents/ProjectJoinRequestButton";
import ProjectJoinRequestModal from "./../projectComponents/ProjectJoinRequestModal";
import ProjectJoinRequestPreview from "../projectComponents/ProjectJoinRequestPreview";
import SendMessageModal from "../messageComponents/SendMessageModal";
import EditProject from "./EditProject";
import Modal from "../layout/Modal";

export default function Project() {
    const { addToast } = useToasts();
    const [status, setStatus] = React.useState(Status.Loading);
    const [rerender, setRerender] = React.useState(0);
    const [project, setProject] = React.useState({
        teamMemberUsernames: [],
        lookingForMembers: true,
        githubLink: "",
        id: "",
        joinRequests: "",
    });
    let params = useParams();

    useEffect(() => {
        axios.get("/api/projects/" + params.id).then((response) => {
            if (!response.data) {
                setStatus(Status.NotFound);
            } else {
                setProject(response.data);
            }
        });
    }, [rerender]);

    const flipLookingForMembers = () => {
        axios
            .put(
                "/api/projects/" +
                    project.id +
                    "/update?lookingForMembers=" +
                    !project.lookingForMembers
            )
            .then(() => {
                setProject((project) => ({
                    ...project,
                    lookingForMembers: !project.lookingForMembers,
                }));
                addToast("Your project was changed successfully.", {
                    appearance: "success",
                    autoDismiss: true,
                });
            })
            .catch((err) => {
                console.log("Error changing lookingForMembers: " + err);
                addToast("An error occurred. Please try again.", {
                    appearance: "error",
                });
            });
    };

    const acceptRequest = (username) => {
        setProject({
            ...project,
            teamMemberUsernames: project.teamMemberUsernames.concat([username]),
            joinRequests: project.joinRequests.filter(
                (request) => request.username !== username
            ),
        });
    };

    const denyRequest = (username) => {
        setProject({
            ...project,
            joinRequests: project.joinRequests.filter(
                (request) => request.username !== username
            ),
        });
    };

    const submitRequest = () => {
        setRerender((rerender) => rerender + 1);
    };

    const edit = () => {
        setStatus(Status.NotSubmitted);
    };

    const leave = () => {
        axios
            .post("/api/projects/" + project.id + "/leave")
            .then(() => {
                setProject((project) => ({
                    ...project,
                    userIsTeamMember: false,
                }));
                addToast("You have left this team successfully.", {
                    appearance: "success",
                    autoDismiss: true,
                });
            })
            .catch((err) => {
                console.log("Error leaving team: " + err);
                addToast("An error occurred. Please try again.", {
                    appearance: "error",
                });
            });
    };

    var githubLink;
    if (project.githubLink === "") {
        if (project.userIsTeamMember) {
            githubLink = <div></div>;
        } else {
            githubLink = <div></div>;
        }
    } else {
        githubLink = <a href={githubLink}>{githubLink}</a>;
    }

    if (status === Status.NotFound) {
        return <NotFound />;
    }

    if (status === Status.NotSubmitted) {
        return <EditProject originalProject={project} setStatus={setStatus} />;
    }

    if (status === Status.Success) {
        return <Success />;
    }

    return (
        <div>
            <div className="d-flex">
                <div className="me-auto">
                    <h1>{project.name}</h1>
                </div>
                <div className="d-flex align-items-center">
                    <ProjectJoinRequestButton project={project} />
                    {githubLink}
                </div>
            </div>
            {project.tags && project.tags.length > 0 && (
                <div className="mw-100">
                    {project.tags.map((tag) => (
                        <span
                            className="badge rounded-pill bg-dark me-2"
                            key={tag}
                        >
                            {tag}
                        </span>
                    ))}
                </div>
            )}
            <button
                type="button"
                data-bs-toggle="modal"
                data-bs-target="#sendMessage"
                className="btn btn-outline-secondary btn-md my-2"
            >
                Message team
            </button>
            <span className="ms-3">
                <button
                    type="button"
                    onClick={edit}
                    className="btn btn-outline-secondary btn-md my-2"
                >
                    Edit project
                </button>
            </span>
            {project.userIsTeamMember && project.joinRequests.length > 0 && (
                <div
                    className="mt-3 p-2"
                    style={{ backgroundColor: "#bdf1fc" }}
                >
                    <AnimateSharedLayout>
                        <h4>Join requests</h4>
                        <motion.div layout className="container mx-auto">
                            {project.joinRequests.map((joinRequest) => (
                                <ProjectJoinRequestPreview
                                    key={joinRequest.username}
                                    request={joinRequest}
                                    project={project}
                                    acceptRequest={acceptRequest}
                                    denyRequest={denyRequest}
                                />
                            ))}
                        </motion.div>
                    </AnimateSharedLayout>
                </div>
            )}
            {project.description !== "" && (
                <div className="p-2 m-3" style={{ whiteSpace: "pre-wrap" }}>
                    {project.description}
                </div>
            )}
            <table className="table">
                <thead className="thead-dark">
                    <tr>
                        <th scope="col">Members</th>
                    </tr>
                </thead>
                <tbody>
                    {project.teamMemberUsernames.map((username) => (
                        <tr key={username}>
                            <td>{username}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
            {project.userIsTeamMember && (
                <div>
                    <div className="form-check form-switch">
                        <input
                            className="form-check-input"
                            type="checkbox"
                            id="lookingForMembers"
                            onChange={flipLookingForMembers}
                            checked={project.lookingForMembers}
                        />
                        <label
                            className="form-check-label"
                            htmlFor="lookingForMembers"
                        >
                            Looking for new members
                        </label>
                    </div>
                    <button
                        type="button"
                        data-bs-toggle="modal"
                        data-bs-target="#leaveConfirmation"
                        className="btn btn-danger btn-sm mt-4"
                    >
                        Leave team
                    </button>
                </div>
            )}
            <ProjectJoinRequestModal
                project={project}
                submitRequest={submitRequest}
            />
            <SendMessageModal
                recipient={project.name}
                recipientId={project.id}
                id="sendMessage"
                isProject={true}
            />
            <Modal
                id="leaveConfirmation"
                title="Leave team"
                body="Are you sure you want to leave this project? It will be deleted if all members leave."
                submit="Leave"
                onClick={leave}
            />
        </div>
    );
}

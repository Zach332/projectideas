import React, { useEffect } from "react";
import { useParams } from "react-router-dom";
import Success from "../general/Success";
import axios from "axios";
import NotFound from "./NotFound";
import { Status } from "../../State";
import { toQuery } from "../utils/Routing";
import { useToasts } from "react-toast-notifications";
import { motion, AnimateSharedLayout } from "framer-motion";
import ProjectJoinRequestButton from "../projectComponents/ProjectJoinRequestButton";
import ProjectJoinRequestModal from "./../projectComponents/ProjectJoinRequestModal";
import ProjectJoinRequestPreview from "../projectComponents/ProjectJoinRequestPreview";
import SendMessageModal from "../messageComponents/SendMessageModal";
import EditProject from "./EditProject";
import Modal from "../layout/Modal";
import ProjectGitHubLinkModal from "../projectComponents/ProjectGitHubLinkModal";
import LoadingDiv from "../general/LoadingDiv";
import { Helmet } from "react-helmet";
import { Globals } from "../../GlobalData";

export default function Project() {
    const { addToast } = useToasts();
    const [status, setStatus] = React.useState(Status.Loading);
    const [rerender, setRerender] = React.useState(0);
    const [project, setProject] = React.useState({
        name: "",
        teamMemberUsernames: [],
        lookingForMembers: true,
        publicProject: true,
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
                setStatus(Status.Loaded);
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
                    publicProject: true,
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

    const flipPublicProject = () => {
        axios
            .put(
                "/api/projects/" +
                    project.id +
                    "/updatepublicstatus?publicProject=" +
                    !project.publicProject
            )
            .then(() => {
                setProject((project) => ({
                    ...project,
                    publicProject: !project.publicProject,
                }));
                addToast("Your project was changed successfully.", {
                    appearance: "success",
                    autoDismiss: true,
                });
            })
            .catch((err) => {
                console.log("Error changing publicProject: " + err);
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

    const editGitHubLink = (newLink) => {
        setProject({
            ...project,
            githubLink: newLink,
        });
    };

    const submitLink = () => {
        axios
            .put("/api/projects/" + project.id, {
                name: project.name,
                description: project.description,
                lookingForMembers: project.lookingForMembers,
                tags: project.tags,
                githubLink: project.githubLink,
            })
            .then(() => {
                addToast("Your idea was updated successfully.", {
                    appearance: "success",
                    autoDismiss: true,
                });
            })
            .catch((err) => {
                console.log("Error updating project: " + err);
                addToast("Your project was not updated. Please try again.", {
                    appearance: "error",
                });
            });
    };

    const edit = () => {
        setStatus(Status.NotSubmitted);
    };

    const leave = () => {
        axios
            .post("/api/projects/" + project.id + "/leave")
            .then(() => {
                setRerender((rerender) => rerender + 1);
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

    const appendHttp = (link) => {
        return link.startsWith("http") ? link : "https://" + link;
    };

    const removeHttp = (link) => {
        if (link.startsWith("http://")) {
            return link.substring(7);
        }
        if (link.startsWith("https://")) {
            return link.substring(8);
        }
        return link;
    };

    const searchTag = (tagName) => {
        window.location.href =
            "/tags?" + toQuery({ type: "project", tag: tagName });
    };

    var githubLink;
    if (!project.githubLink || project.githubLink === "") {
        if (project.userIsTeamMember) {
            githubLink = (
                <button
                    type="button"
                    data-bs-toggle="modal"
                    data-bs-target="#gitHubLink"
                    className="btn btn-outline-secondary btn-md my-2"
                >
                    Add GitHub link
                </button>
            );
        } else {
            githubLink = <div></div>;
        }
    } else {
        if (project.userIsTeamMember) {
            githubLink = (
                <div className="d-flex">
                    <a
                        href={appendHttp(project.githubLink)}
                        target="_blank"
                        rel="external noreferrer"
                    >
                        {removeHttp(project.githubLink)}
                    </a>
                    <div
                        className="btn btn-sm d-flex align-items-center"
                        data-bs-toggle="modal"
                        data-bs-target="#gitHubLink"
                    >
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            width="16"
                            height="16"
                            fill="currentColor"
                            className="bi bi-pencil-fill"
                            viewBox="0 0 16 16"
                        >
                            <path d="M12.854.146a.5.5 0 0 0-.707 0L10.5 1.793 14.207 5.5l1.647-1.646a.5.5 0 0 0 0-.708l-3-3zm.646 6.061L9.793 2.5 3.293 9H3.5a.5.5 0 0 1 .5.5v.5h.5a.5.5 0 0 1 .5.5v.5h.5a.5.5 0 0 1 .5.5v.5h.5a.5.5 0 0 1 .5.5v.207l6.5-6.5zm-7.468 7.468A.5.5 0 0 1 6 13.5V13h-.5a.5.5 0 0 1-.5-.5V12h-.5a.5.5 0 0 1-.5-.5V11h-.5a.5.5 0 0 1-.5-.5V10h-.5a.499.499 0 0 1-.175-.032l-.179.178a.5.5 0 0 0-.11.168l-2 5a.5.5 0 0 0 .65.65l5-2a.5.5 0 0 0 .168-.11l.178-.178z" />
                        </svg>
                    </div>
                </div>
            );
        } else {
            githubLink = (
                <a
                    href={appendHttp(project.githubLink)}
                    target="_blank"
                    rel="external noreferrer"
                >
                    {removeHttp(project.githubLink)}
                </a>
            );
        }
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
        <LoadingDiv isLoading={status === Status.Loading}>
            <Helmet>
                <title>
                    {project.name} | {Globals.Title}
                </title>
            </Helmet>
            <div className="d-flex">
                <div className="me-auto">
                    <h1>{project.name}</h1>
                </div>
                <div className="d-flex align-items-center">
                    <li className="list-group-item border-0">
                        <ProjectJoinRequestButton project={project} />
                        <div className="text-end">{githubLink}</div>
                    </li>
                </div>
            </div>
            {project.tags && project.tags.length > 0 && (
                <div className="mw-100">
                    {project.tags.map((tag) => (
                        <span
                            className="badge btn rounded-pill btn-primary me-2"
                            onClick={() => searchTag(tag)}
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
                    {!project.lookingForMembers && (
                        <div className="form-check form-switch">
                            <input
                                className="form-check-input"
                                type="checkbox"
                                id="publicProject"
                                onChange={flipPublicProject}
                                checked={project.publicProject}
                            />
                            <label
                                className="form-check-label"
                                htmlFor="publicProject"
                            >
                                Public
                            </label>
                        </div>
                    )}
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
            <ProjectGitHubLinkModal
                id="gitHubLink"
                gitHubLink={project.githubLink}
                setGitHubLink={editGitHubLink}
                submitLink={submitLink}
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
        </LoadingDiv>
    );
}

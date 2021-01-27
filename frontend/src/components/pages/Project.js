import React, { useEffect } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";
import NotFound from "./NotFound";
import { Status } from "../../State";
import { useToasts } from "react-toast-notifications";
import { motion, AnimateSharedLayout } from "framer-motion";
import ProjectJoinRequestButton from "../ProjectJoinRequestButton";
import ProjectJoinRequestModal from "./../ProjectJoinRequestModal";
import ProjectJoinRequestPreview from "./../ProjectJoinRequestPreview";

export default function Project() {
    const { addToast } = useToasts();
    const [status, setStatus] = React.useState(Status.Loading);
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
                console.log(response.data);
            }
        });
    }, []);

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
            <br />
            <hr />
            <div className="p-2 m-3" style={{ whiteSpace: "pre-wrap" }}>
                {project.description}
            </div>
            <hr />
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
            )}
            <ProjectJoinRequestModal project={project} />
        </div>
    );
}

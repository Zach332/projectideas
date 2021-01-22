import React, { useEffect } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";
import NotFound from "./NotFound";
import { Status } from "../../State";
import { useToasts } from "react-toast-notifications";

export default function Project() {
    const { addToast } = useToasts();
    const [status, setStatus] = React.useState(Status.Loading);
    const [project, setProject] = React.useState({
        teamMemberUsernames: [],
        lookingForMembers: true,
        githubLink: "",
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

    const sendJoinRequest = (event) => {
        axios
            .post("/api/projects/" + project.id + "/joinrequests")
            .then(() => {
                project.userHasRequestedToJoin = true;
                addToast("Your request was submitted.", {
                    appearance: "success",
                    autoDismiss: true,
                });
            })
            .catch((err) => {
                console.log("Error submitting request: " + err);
                addToast("Your request was not submitted. Please try again.", {
                    appearance: "error",
                });
            });
        event.preventDefault();
    };

    var joinRequestButton = <div></div>;
    if (project.userIsTeamMember) {
        joinRequestButton = <div></div>;
    } else if (project.userHasRequestedToJoin) {
        joinRequestButton = (
            <button
                className="btn btn-md btn-success"
                disabled={true}
                type="button"
                id="requestToJoin"
            >
                Join request sent
            </button>
        );
    } else if (project.lookingForMembers) {
        joinRequestButton = (
            <button
                className="btn btn-md btn-primary"
                type="button"
                id="requestToJoin"
                onClick={sendJoinRequest}
            >
                Request to join
            </button>
        );
    }

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
                    {joinRequestButton}
                    {githubLink}
                </div>
            </div>
            <p style={{ whiteSpace: "pre-wrap" }}>{project.description}</p>
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
        </div>
    );
}

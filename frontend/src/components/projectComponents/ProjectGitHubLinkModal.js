import Modal from "../layout/Modal";
import $ from "jquery";

export default function ProjectGitHubLinkModal({
    id,
    gitHubLink,
    setGitHubLink,
    submitLink,
}) {
    const handleLinkChange = (event) => {
        setGitHubLink(event.target.value);
    };

    const submitLinkPreventDefault = (event) => {
        event.preventDefault();
        submitLink();
        $("#" + id + "close").trigger("click");
    };

    const linkForm = (
        <div className="mx-auto">
            <form className="py-4" onSubmit={submitLinkPreventDefault}>
                <input
                    className="form-control"
                    value={gitHubLink}
                    placeholder="Enter a link to this project's code repository"
                    onChange={handleLinkChange}
                ></input>
            </form>
        </div>
    );

    return (
        <div>
            <Modal
                id={id}
                title={"Edit repository link"}
                body={linkForm}
                submit="Submit"
                onClick={submitLink}
            />
        </div>
    );
}

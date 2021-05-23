import { useHistory } from "react-router-dom";

export default function LoginWarning() {
    let history = useHistory();

    const onClick = () => {
        history.push("/login");
    };

    return (
        <div className="p-4 bg-light">
            <h1 className="display-4">Login required</h1>
            <p className="lead">You must login to view this page.</p>
            <hr className="my-4" />
            <button
                className="btn btn-primary btn-lg"
                data-bs-dismiss="modal"
                onClick={onClick}
                role="button"
            >
                Login here
            </button>
        </div>
    );
}

import { Link } from "react-router-dom";

export default function LoginWarning() {
    return (
        <div className="p-4 bg-light">
            <h1 className="display-4">Login required</h1>
            <p className="lead">You must login to view this page.</p>
            <hr className="my-4" />
            <Link className="btn btn-primary btn-lg" to="/login" role="button">
                Login here
            </Link>
        </div>
    );
}

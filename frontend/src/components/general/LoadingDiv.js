import Spinner from "../general/Spinner";

export default function LoadingDiv({ isLoading, children, ...props }) {
    if (!isLoading) {
        return <div {...props}>{children}</div>;
    } else {
        return (
            <div className="mt-5">
                <Spinner />
            </div>
        );
    }
}

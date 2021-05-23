import Error from "./../general/Error";

export default function NotFound() {
    return (
        <Error
            pageTitle="Page Not Found"
            errorMessage="The link you used does not appear to be valid."
        />
    );
}

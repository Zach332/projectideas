import { Helmet } from "react-helmet-async";
import { Globals } from "../../GlobalData";
import { Link } from "react-router-dom";

export default function Privacy() {
    return (
        <div>
            <Helmet>
                <title>Privacy | {Globals.Title}</title>
            </Helmet>
            <h2>Privacy Policy</h2>
            <p>
                Your privacy is important to us. It is projectideas&apos; policy
                to respect your privacy regarding any information we may collect
                from you across our website,{" "}
                <Link to="http://projectideas.tech">
                    http://projectideas.tech
                </Link>
                , and other sites we own and operate.
            </p>
            <p>
                We only ask for personal information when we truly need it to
                provide a service to you. We collect it by fair and lawful
                means, with your knowledge and consent. We also let you know why
                we’re collecting it and how it will be used.
            </p>
            <p>
                We only retain collected information for as long as necessary to
                provide you with your requested service. What data we store,
                we’ll protect within commercially acceptable means to prevent
                loss and theft, as well as unauthorized access, disclosure,
                copying, use or modification.
            </p>
            <p>
                We don’t share any personally identifying information publicly
                or with third-parties, except when required to by law.
            </p>
            <p>
                Our website may link to external sites that are not operated by
                us. Please be aware that we have no control over the content and
                practices of these sites, and cannot accept responsibility or
                liability for their respective privacy policies.
            </p>
            <p>
                You are free to refuse our request for your personal
                information, with the understanding that we may be unable to
                provide you with some of your desired services.
            </p>
            <p>
                Your continued use of our website will be regarded as acceptance
                of our practices around privacy and personal information. If you
                have any questions about how we handle user data and personal
                information, feel free to contact us.
            </p>
            <p>This policy is effective as of 24 September 2020.</p>
        </div>
    );
}

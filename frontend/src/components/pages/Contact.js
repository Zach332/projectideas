import SendMessageModal from "./../messageComponents/SendMessageModal";
import { Helmet } from "react-helmet-async";
import { Globals } from "../../GlobalData";

export default function Contact() {
    return (
        <div>
            <Helmet>
                <title>Contact | {Globals.Title}</title>
            </Helmet>
            <h1>Contact Us</h1>
            <p>
                We&apos;d love to hear your thoughts on the website. Feel free
                to send us a message! You can also submit issues on our{" "}
                <a
                    href="https://github.com/Zach332/projectideas"
                    rel="noreferrer"
                    target="_blank"
                >
                    GitHub
                </a>
                .
            </p>
            <button
                type="button"
                data-bs-toggle="modal"
                data-bs-target="#sendMessageProjectideas"
                className="btn btn-outline-secondary btn-md my-2"
            >
                Message projectideas
            </button>
            <SendMessageModal
                recipient="projectideas"
                id={"sendMessageProjectideas"}
            />
        </div>
    );
}

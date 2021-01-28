import React from "react";
import axios from "axios";
import Modal from "../layout/Modal";
import { useLeavePageWarning } from "../hooks/LeavePageWarning";
import { useToasts } from "react-toast-notifications";

export default function MessageModals({ recipient, id }) {
    const [messageToSend, setMessageToSend] = React.useState("");

    useLeavePageWarning(messageToSend != "");

    const { addToast } = useToasts();

    const handleMessageChange = (event) => {
        setMessageToSend(event.target.value);
    };

    const messageForm = (
        <div className="mx-auto">
            <form className="py-4">
                <textarea
                    className="form-control"
                    value={messageToSend}
                    id="content"
                    rows="8"
                    placeholder="Your message"
                    onChange={handleMessageChange}
                ></textarea>
            </form>
        </div>
    );

    const sendMessage = () => {
        axios
            .post("/api/messages/" + recipient, {
                content: messageToSend,
            })
            .then(() => {
                addToast("Your message was sent.", {
                    appearance: "success",
                    autoDismiss: true,
                });
                setMessageToSend("");
            })
            .catch((err) => {
                console.log("Error submitting message: " + err);
                addToast("Your message was not sent. Please try again.", {
                    appearance: "error",
                });
            });
    };

    return (
        <div>
            <Modal
                id={"sendMessage" + id}
                title={"Send message to " + recipient}
                body={messageForm}
                submit="Send"
                onClick={sendMessage}
            />
        </div>
    );
}

import React from "react";
import axios from "axios";
import Modal from "../layout/Modal";
import { useLeavePageWarning } from "../hooks/LeavePageWarning";
import { useToasts } from "react-toast-notifications";
import LoginWarning from "./../logins/LoginWarning";
import { useGlobalState } from "../../State";
import { Prompt } from "react-router-dom";

export default function SendMessageModal({
    recipient,
    id,
    recipientId,
    isProject,
}) {
    const [user] = useGlobalState("user");
    const [messageToSend, setMessageToSend] = React.useState("");

    useLeavePageWarning(messageToSend != "");

    const { addToast } = useToasts();

    const handleMessageChange = (event) => {
        setMessageToSend(event.target.value);
    };

    const messageForm = user.loggedIn ? (
        <div className="mx-auto">
            <form className="py-4">
                <textarea
                    className="form-control"
                    value={messageToSend}
                    rows="8"
                    placeholder="Your message"
                    onChange={handleMessageChange}
                ></textarea>
            </form>
        </div>
    ) : (
        <LoginWarning />
    );

    const sendMessage = () => {
        let api;
        if (isProject) {
            api = "/api/messages/projects/" + encodeURIComponent(recipientId);
        } else {
            api = "/api/messages/" + encodeURIComponent(recipient);
        }
        axios
            .post(api, {
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
            <Prompt
                when={messageToSend != ""}
                message="You have unsaved changes; are you sure you want to leave?"
            />
            <Modal
                id={id}
                title={"Send message to " + recipient}
                body={messageForm}
                submit="Send"
                onClick={sendMessage}
                customFooter={user.loggedIn ? null : <div></div>}
            />
        </div>
    );
}

import React, { useEffect } from "react";
import axios from "axios";
import Message from "../messageComponents/Message";
import LoginWarning from "../logins/LoginWarning";
import { useGlobalState } from "../../State";
import { motion, AnimateSharedLayout } from "framer-motion";
import LoadingDiv from "./../general/LoadingDiv";
import { Status } from "./../../State";

export default function Messages() {
    const [messages, setMessages] = React.useState([]);
    const [user] = useGlobalState("user");
    const [rerender, setRerender] = React.useState(0);
    const [status, setStatus] = React.useState(Status.Loading);

    const [rotateMode, setRotateMode] = React.useState(0);
    const [mode, setMode] = React.useState("Received");

    useEffect(() => {
        setStatus(Status.Loading);
        if (mode === "Received") {
            axios.get("/api/messages/received").then((response) => {
                setMessages(response.data);
                setStatus(Status.Loaded);
                axios.post("/api/messages/received/markallasread");
            });
        } else {
            axios.get("/api/messages/sent").then((response) => {
                setMessages(response.data);
                setStatus(Status.Loaded);
            });
        }
    }, [rerender, mode]);

    const flipMode = () => {
        setRotateMode(rotateMode + 360);
        if (mode === "Received") {
            setMode("Sent");
        } else {
            setMode("Received");
        }
    };

    if (!user.loggedIn) {
        return <LoginWarning />;
    }
    return (
        <div>
            <AnimateSharedLayout>
                <h1>Messages</h1>
                <motion.button
                    type="button"
                    animate={{ rotateY: rotateMode }}
                    onClick={flipMode}
                    className="btn btn-outline-secondary"
                >
                    {mode}
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="16"
                        height="16"
                        fill="currentColor"
                        className="bi bi-arrow-down-up ms-2"
                        viewBox="0 0 16 16"
                    >
                        <path
                            fillRule="evenodd"
                            d="M11.5 15a.5.5 0 0 0 .5-.5V2.707l3.146 3.147a.5.5 0 0 0 .708-.708l-4-4a.5.5 0 0 0-.708 0l-4 4a.5.5 0 1 0 .708.708L11 2.707V14.5a.5.5 0 0 0 .5.5zm-7-14a.5.5 0 0 1 .5.5v11.793l3.146-3.147a.5.5 0 0 1 .708.708l-4 4a.5.5 0 0 1-.708 0l-4-4a.5.5 0 0 1 .708-.708L4 13.293V1.5a.5.5 0 0 1 .5-.5z"
                        />
                    </svg>
                </motion.button>
                <LoadingDiv isLoading={status === Status.Loading}>
                    <motion.div layout className="container pt-2 mx-auto">
                        {messages.map((message) => (
                            <Message
                                key={message.id}
                                message={message}
                                setRerender={setRerender}
                            />
                        ))}
                    </motion.div>
                </LoadingDiv>
            </AnimateSharedLayout>
        </div>
    );
}

import React, { useEffect } from "react";
import axios from "axios";
import Message from "../messageComponents/Message";
import LoginWarning from "../logins/LoginWarning";
import { useGlobalState } from "../../State";
import { motion, AnimateSharedLayout } from "framer-motion";
import LoadingDiv from "./../general/LoadingDiv";
import { Status } from "./../../State";
import { Helmet } from "react-helmet";
import { Globals } from "../../GlobalData";
import { useHistory, useLocation } from "react-router-dom";
import { toParams, toQuery } from "../utils/Routing";

export default function Messages() {
    let location = useLocation();
    let history = useHistory();
    const [messages, setMessages] = React.useState([]);
    const [user] = useGlobalState("user");
    const [rerender, setRerender] = React.useState(0);
    const [status, setStatus] = React.useState(Status.Loading);

    const [rotateMode, setRotateMode] = React.useState(0);
    const [mode, setMode] = React.useState("Received");

    const [lastPage, setLastPage] = React.useState(true);
    const params = toParams(location.search.replace(/^\?/, ""));
    if (!params.page) params.page = 1;

    useEffect(() => {
        setStatus(Status.Loading);
        if (mode === "Received") {
            axios
                .get("/api/messages/received?" + toQuery({ page: params.page }))
                .then((response) => {
                    setMessages(response.data.receivedMessages);
                    setStatus(Status.Loaded);
                    setLastPage(response.data.lastPage);
                    axios.post("/api/messages/received/markallasread");
                });
        } else {
            axios
                .get("/api/messages/sent?" + toQuery({ page: params.page }))
                .then((response) => {
                    setMessages(response.data.sentMessages);
                    setStatus(Status.Loaded);
                    setLastPage(response.data.lastPage);
                });
        }
    }, [rerender, mode, location]);

    const flipMode = () => {
        setRotateMode(rotateMode + 360);
        if (mode === "Received") {
            setMode("Sent");
        } else {
            setMode("Received");
        }
        history.push("/messages?" + toQuery({ page: 1 }));
    };

    const next = () => {
        history.push(
            "/messages?" + toQuery({ page: parseInt(params.page) + 1 })
        );
    };

    const previous = () => {
        history.push(
            "/messages?" + toQuery({ page: parseInt(params.page) - 1 })
        );
    };

    if (!user.loggedIn) {
        return <LoginWarning />;
    }
    return (
        <div>
            <AnimateSharedLayout>
                <h1>Messages</h1>
                <Helmet>
                    <title>Messages | {Globals.Title}</title>
                </Helmet>
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
                    <div className="d-flex">
                        <div className="me-auto p-2">
                            {params.page > 1 && (
                                <button
                                    type="btn btn-primary"
                                    className="btn btn-primary btn-md"
                                    onClick={previous}
                                >
                                    Previous
                                </button>
                            )}
                        </div>
                        <div className="p-2">
                            {!lastPage && messages.length > 0 && (
                                <button
                                    type="btn btn-primary"
                                    className="btn btn-primary btn-md"
                                    onClick={next}
                                >
                                    Next
                                </button>
                            )}
                        </div>
                    </div>
                </LoadingDiv>
            </AnimateSharedLayout>
        </div>
    );
}

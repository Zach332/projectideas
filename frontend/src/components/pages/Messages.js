import React, { useEffect } from "react";
import axios from "axios";
import Message from "../Message";
import LoginWarning from "../logins/LoginWarning";
import { useGlobalState } from "../../State";

export default function Messages() {
    const [messages, setMessages] = React.useState([]);
    const [user] = useGlobalState("user");

    useEffect(() => {
        axios.get("/api/messages/received").then((response) => {
            setMessages(response.data);
        });
    }, []);

    if (!user.loggedIn) {
        return <LoginWarning />;
    }

    return (
        <div>
            <h1>Messages</h1>
            <div className="container pt-2 mx-auto">
                {messages.map((message) => (
                    <Message key={message.id} message={message} />
                ))}
            </div>
        </div>
    );
}

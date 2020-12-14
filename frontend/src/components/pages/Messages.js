import React, { useEffect } from "react";
import axios from "axios";
import Message from "../Message";
import LoginWarning from "../logins/LoginWarning";
import { useGlobalState } from "../../State";
import { motion, AnimateSharedLayout } from "framer-motion";

export default function Messages() {
    const [messages, setMessages] = React.useState([]);
    //const [activeMessage, setActiveMessage] = React.useState([])
    const [user] = useGlobalState("user");
    const [rerender, setRerender] = React.useState(0);

    useEffect(() => {
        axios.get("/api/messages/received").then((response) => {
            setMessages(response.data);
            axios.post("/api/messages/received/markallasread");
        });
    }, [rerender]);

    if (!user.loggedIn) {
        return <LoginWarning />;
    }
    return (
        <div>
            <AnimateSharedLayout>
                <h1>Messages</h1>
                <motion.div layout className="container pt-2 mx-auto">
                    {messages.map((message) => (
                        <Message
                            key={message.id}
                            message={message}
                            setRerender={setRerender}
                        />
                    ))}
                </motion.div>
            </AnimateSharedLayout>
        </div>
    );
}

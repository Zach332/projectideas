import axios from "axios";
import { useEffect, useState } from "react";
import CheckMark from "../../check.svg";
import { Status } from "../../State";
import Spinner from "../general/Spinner";
import XMark from "../../x.svg";
import { toParams } from "../utils/Routing";
import SubscriptionPreferences from "./../userComponents/SubscriptionPreferences";

export default function Unsubscribe() {
    const [status, setStatus] = useState(Status.Loading);
    const [message, setMessage] = useState("");
    const [notificationPreference, setNotificationPreference] = useState(
        "Unsubscribed"
    );
    const params = toParams(location.search.replace(/^\?/, ""));

    useEffect(() => {
        axios
            .post(
                "https://projectideas.herokuapp.com/api/email/notificationPreference/" +
                    params.id,
                {
                    notificationPreference: "Unsubscribed",
                }
            )
            .then(() => {
                setStatus(Status.Success);
                setMessage(
                    "You were successfully unsubscribed. If you would like to resubscribe, you can choose a new notification setting below."
                );
            })
            .catch((err) => {
                if (err.response.status == 404) {
                    setStatus(Status.Failure);
                    setMessage(
                        "The unsubscribe link you used is not valid. Please ensure your unsubscribe link is correct, or try logging into your account and changing your notification preferences from your profile."
                    );
                } else {
                    setStatus(Status.Failure);
                    setMessage(
                        "An error occured when unsubscribing your email address. Please try logging into your account and changing your notification preferences from your profile."
                    );
                }
            });
    }, []);

    const submitNewPreference = (newPreference) => {
        return new Promise((resolve, reject) => {
            axios
                .post(
                    "https://projectideas.herokuapp.com/api/email/notificationPreference/" +
                        params.id,
                    {
                        notificationPreference: newPreference,
                    }
                )
                .then(() => {
                    setMessage(
                        "Notification preferences changed successfully."
                    );
                    resolve();
                })
                .catch((err) => {
                    reject(err);
                });
        });
    };

    if (status === Status.Success) {
        return (
            <div>
                <div className="text-center mb-5">
                    <img
                        src={CheckMark}
                        className="mx-auto d-block p-4"
                        alt="Success"
                    />
                    <p>{message}</p>
                </div>
                <SubscriptionPreferences
                    preference={notificationPreference}
                    setPreference={setNotificationPreference}
                    submitPreference={submitNewPreference}
                />
            </div>
        );
    }

    if (status == Status.Failure) {
        return (
            <div className="text-center">
                <img
                    src={XMark}
                    className="mx-auto d-block m-4"
                    width="215px"
                    height="215px"
                    alt="Invite failed"
                />
                <p>{message}</p>
            </div>
        );
    }

    return <Spinner />;
}

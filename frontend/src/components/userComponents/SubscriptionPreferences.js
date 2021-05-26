import { useToasts } from "react-toast-notifications";

export default function SubscriptionPreferences({
    preference,
    setPreference,
    submitPreference,
}) {
    const { addToast } = useToasts();

    const isPreference = (option) => {
        return preference == option;
    };

    const changeNotificationPreference = (e) => {
        let newPreference = e.target.id;
        submitPreference(newPreference)
            .then(() => {
                setPreference(newPreference);
                addToast("Notification preferences changed successfully", {
                    appearance: "success",
                    autoDismiss: true,
                });
            })
            .catch((err) => {
                console.log("Error updating notification preferences: " + err);
                addToast(
                    "Error updating username preferences. Please try again.",
                    {
                        appearance: "error",
                    }
                );
            });
    };

    return (
        <div>
            <h5>Email Notification Preference</h5>
            <form className="mb-5">
                <div className="form-check">
                    <input
                        className="form-check-input"
                        type="radio"
                        id="AllNewMessages"
                        onChange={changeNotificationPreference}
                        checked={isPreference("AllNewMessages")}
                    />
                    <label
                        className="form-check-label"
                        htmlFor="AllNewMessages"
                    >
                        All new messages
                    </label>
                </div>
                <div className="form-check">
                    <input
                        className="form-check-input"
                        type="radio"
                        id="Default"
                        onChange={changeNotificationPreference}
                        checked={isPreference("Default")}
                    />
                    <label className="form-check-label" htmlFor="Default">
                        Default
                    </label>
                </div>
                <div className="form-check">
                    <input
                        className="form-check-input"
                        type="radio"
                        id="Unsubscribed"
                        onChange={changeNotificationPreference}
                        checked={isPreference("Unsubscribed")}
                    />
                    <label className="form-check-label" htmlFor="Unsubscribed">
                        Unsubscribed
                    </label>
                </div>
            </form>
        </div>
    );
}

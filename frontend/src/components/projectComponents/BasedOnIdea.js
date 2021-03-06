import IdeaSummary from "../ideaComponents/IdeaSummary";
import { useState, useEffect } from "react";
import axios from "axios";

export default function BasedOnIdea({ ideaId }) {
    const [idea, setIdea] = useState(null);

    useEffect(() => {
        axios
            .get(process.env.REACT_APP_API + "ideas/" + ideaId)
            .then((response) => {
                if (response.data && !response.data.deleted) {
                    setIdea(response.data);
                }
            });
    }, []);

    return (
        <div>
            {idea != null && (
                <div className="mt-5">
                    <p>Inspired by the idea:</p>
                    <div className="m-3">
                        <IdeaSummary idea={idea} />
                    </div>
                </div>
            )}
        </div>
    );
}

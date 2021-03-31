import Upvotes from "../postComponents/Upvotes";
import IdeaSummary from "./IdeaSummary";

export default function IdeaSummaryUpvotes({ idea }) {
    return (
        <div className="d-flex align-items-center">
            <Upvotes post={idea} postType="idea" />
            <IdeaSummary idea={idea} />
        </div>
    );
}

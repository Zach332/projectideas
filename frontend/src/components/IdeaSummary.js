import React from 'react'

export default function IdeaSummary({idea}) {
    var ideaLink = "/idea/"+idea.id

	return (
        <a href={ideaLink} className="list-group-item list-group-item-action flex-column align-items-start m-3 rounded border">
            <div className="d-flex w-100 justify-content-between">
                <h5 className="mb-1">{idea.title}</h5>
                <small className="text-muted">3 days ago</small>
            </div>
            <p className="mb-1">{idea.content}</p>
            <small className="text-muted">By {idea.authorUsername}</small>
        </a>
	);
}
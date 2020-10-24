import React from 'react'

export default function IdeaSummary({idea}) {
    var ideaLink = "/idea/"+idea.id

	return (
        <a href={ideaLink} class="list-group-item list-group-item-action flex-column align-items-start m-3 rounded border">
            <div class="d-flex w-100 justify-content-between">
                <h5 class="mb-1">{idea.title}</h5>
                <small class="text-muted">3 days ago</small>
            </div>
            <p class="mb-1">{idea.content}</p>
            <small class="text-muted">By {idea.authorUsername}</small>
        </a>
	);
}
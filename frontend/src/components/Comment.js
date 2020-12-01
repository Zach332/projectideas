import React from 'react'

export default function Comment({comment}) {
	return (
        <div className="list-group-item flex-column align-items-start my-2 rounded border">
            <p className="mb-1">{comment.content}</p>
            <small className="text-muted">{comment.authorUsername}</small>
        </div>
	);
}
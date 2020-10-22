import React from 'react'

export default function IdeaSummary({idea}) {

	return (
		<div className="card">
            <div className="card-body">
                <h5 className="card-title">{idea.content}</h5>
                <p className="card-text">{idea.authorId}</p>
            </div>
        </div>
	);
}

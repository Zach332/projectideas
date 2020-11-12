import React from 'react'
import ReactMarkdown from 'react-markdown'

export default function IdeaCard(props) {
    return (
        <div className="card m-4">
            <div className="card-header">
                <h1>{props.title}</h1>
            </div>
            <div className="card-body embed-responsive">
                <ReactMarkdown>{props.content}</ReactMarkdown>
            </div>
        </div>
    )
}

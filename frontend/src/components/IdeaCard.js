import React from 'react'
import ReactMarkdown from 'react-markdown'

export default function IdeaCard(props) {
    return (
        <div class="card m-4">
            <div class="card-header">
                <h1>{props.title}</h1>
            </div>
            <div class="card-body">
                <ReactMarkdown>{props.content}</ReactMarkdown>
            </div>
        </div>
    )
}

import React from 'react'
import axios from 'axios'
import { useLeavePageWarning } from '../hooks/LeavePageWarning'
import { Status } from '../../State'
import { useToasts } from 'react-toast-notifications'
import IdeaCard from '../IdeaCard'

export default function EditIdea({originalIdea, setStatus}) {
    const { addToast } = useToasts()
    const [ idea, setIdea ] = React.useState(originalIdea)
    const [ edited, setEdited ] = React.useState(false)

    useLeavePageWarning(edited)

    const handleInputChange = (event) => {
        const target = event.target;
        const name = target.id;
        setEdited(true)
        setIdea(
            idea => ({
                ...idea,
                [name]: target.value
            })
        );
    }

    const handleSubmit = (event) => {
        axios.put("/api/ideas/"+idea.id, {
            title: idea.title,
            content: idea.content
        }).then(() => {
            addToast("Your idea was updated successfully.", { appearance: 'success' })
            setStatus(Status.Success)
        }).catch(err => {
            console.log("Error updating idea: " + err);
            addToast("Your idea was not updated. Please try again.", { appearance: 'error' })
        })
        event.preventDefault()
    }

    return (
            <div className="mx-auto">
                <form className="py-4" onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="title">Title</label>
                        <input type="text" value={idea.title} className="form-control" id="title" onChange={handleInputChange}/>
                    </div>
                    <div className="form-group">
                        <label htmlFor="content">Details</label>
                        <textarea value={idea.content} className="form-control" id="content" rows="10" onChange={handleInputChange}></textarea>
                    </div>
                    <button type="submit" className="btn btn-primary">Update Idea</button>
                </form>
                <p>Basic <a href="/markdown" target="_blank">Markdown</a> is supported. A preview of your idea is below.</p>
                <IdeaCard title={idea.title} content={idea.content} />
            </div>
        )
}

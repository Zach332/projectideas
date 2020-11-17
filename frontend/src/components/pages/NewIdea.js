import React from 'react'
import axios from 'axios'
import CheckMark from "../../check.svg"
import LoginWarning from '../logins/LoginWarning'
import { useGlobalState, Status } from '../../State'
import { useToasts } from 'react-toast-notifications'
import IdeaCard from '../IdeaCard'

export default function NewIdea() {
    const { addToast } = useToasts()
    const [idea, setIdea] = React.useState([{ title: '' , content: ''}])
    const [ status, setStatus] = React.useState(Status.NotSubmitted)
    const [ user ] = useGlobalState('user')

    const handleInputChange = (event) => {
        const target = event.target;
        const name = target.id;
        setIdea(
            idea => ({
                ...idea,
                [name]: target.value
            })
        );
    }

    const handleSubmit = (event) => {
        axios.post("/api/ideas", {
            authorUsername: user.username,
            title: idea.title,
            content: idea.content
        }).then(() => {
            setStatus(Status.Success)
        }).catch(err => {
            console.log("Error submitting post: " + err);
            setStatus(Status.Failure)
            addToast("Your post was not submitted. Please try again.", { appearance: 'error' })
        })
        event.preventDefault()
    }

    if(!user.loggedIn) {
        return <LoginWarning />
    }

    if(status === Status.NotSubmitted || status === Status.Failure) {
        return (
            <div className="mx-auto">
                <form className="py-4" onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="title">Title</label>
                        <input type="text" className="form-control" id="title" onChange={handleInputChange}/>
                    </div>
                    <div className="form-group">
                        <label htmlFor="content">Details</label>
                        <textarea className="form-control" id="content" rows="3" onChange={handleInputChange}></textarea>
                    </div>
                    <button type="submit" className="btn btn-primary">Post Idea</button>
                </form>
                <p>Basic <a href="https://www.markdownguide.org/cheat-sheet/" target="_blank">Markdown</a> is supported. A preview of your idea is below.</p>
                <IdeaCard title={idea.title} content={idea.content} />
            </div>
        )
    } else if(status === Status.Success) {
        return (
            <div>
                <img src={CheckMark} className="mx-auto d-block pt-4" alt="Successful login" />
            </div>
        )
    }
}

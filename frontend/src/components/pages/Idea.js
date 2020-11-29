import React, { useEffect } from 'react'
import Success from "../Success"
import { useParams } from 'react-router-dom'
import axios from 'axios'
import IdeaCard from '../IdeaCard'
import NotFound from './NotFound'
import EditIdea from './EditIdea'
import { useGlobalState, Status } from '../../State'
import { useToasts } from 'react-toast-notifications'

export default function Idea() {
    const { addToast } = useToasts()
    const [status, setStatus] = React.useState(Status.Loading)
    const [idea, setIdea] = React.useState([])
    const [ user ] = useGlobalState('user')
    let params = useParams()

    useEffect(() => {
        axios.get("/api/ideas/"+params.id).then((response) => {
            if(!response.data) {
                setStatus(Status.NotFound)
            } else {
                setIdea(response.data)
            }
        })
    },[])

    const deleteIdea = () => {
        axios.delete("/api/ideas/"+params.id)
        .then(() => {
            setStatus(Status.Success)
            addToast("Your idea was deleted.", { appearance: 'success', autoDismiss: true })
        }).catch(err => {
            console.log("Error deleting idea: " + err);
            addToast("Your idea was not deleted. Please try again.", { appearance: 'error' })
        })
    }

    const edit = () => {
        setStatus(Status.NotSubmitted)
    }

    if(status === Status.NotFound) {
        return <NotFound />
    }

    if(status === Status.Success) {
        return <Success />
    }

    if(status === Status.NotSubmitted) {
        return <EditIdea originalIdea={idea} setStatus={setStatus}/>
    }

    let more
    if(user.username === idea.authorUsername) {
        more = (
            <li className="list-group-item">
                <div className="dropdown">
                    <button className="btn btn-secondary dropdown-toggle" type="button" id="dropdownMenuButton" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                        More
                    </button>
                    <div className="dropdown-menu" aria-labelledby="dropdownMenuButton">
                        <a className="dropdown-item" onClick={edit}>Edit idea</a>
                        <a className="dropdown-item text-danger" data-toggle="modal" data-target="#deleteConfirmation">Delete idea</a>
                    </div>
                    </div>
            </li>
        )
    }
    
    var date = new Date(idea.timePosted * 1000)
    return (
        <div className="container-fluid">
            <div className="row justify-content-center">
                <div className="col-lg-8 col-md-8 col-sm-auto mb-2">
                    <IdeaCard title={idea.title} content={idea.content} />
                </div>
                <div className="col-md-auto col-sm-auto">
                    <ul className="card list-group list-group-flush">
                        <li className="list-group-item">By {idea.authorUsername}<br></br>on {date.toLocaleDateString()}</li>
                        <li className="list-group-item">
                            <button type="button" className="btn btn-outline-secondary btn-md">
                                Message author
                            </button>
                        </li>
                        <li className="list-group-item">
                            <button type="button" data-toggle="modal" className="btn btn-primary btn-md">
                                Work on this idea
                            </button>
                        </li>
                        {more}
                    </ul>
                </div>
            </div>
            <div className="modal fade" id="deleteConfirmation" tabIndex="-1" role="dialog" aria-labelledby="deleteConfirmationLabel" aria-hidden="true">
                <div className="modal-dialog">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h4 className="modal-title" id="deleteConfirmationLabel">Delete Idea</h4>
                            <button type="button" className="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span className="sr-only">Close</span></button>
                        </div>
                        <div className="modal-body">
                            Are you sure you want to delete this idea? The data cannot be recovered.
                        </div>
                        <div className="modal-footer">
                            <button type="button" className="btn btn-default" data-dismiss="modal">Cancel</button>
                            <button type="button" className="btn btn-danger" data-dismiss="modal" onClick={deleteIdea}>Delete</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}
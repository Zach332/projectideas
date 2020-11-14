import React, { useEffect } from 'react'
import { useParams } from 'react-router-dom'
import axios from 'axios'
import IdeaCard from '../IdeaCard'

export default function Idea() {
    const [idea, setIdea] = React.useState([])
    let params = useParams()

    useEffect(() => {
        axios.get("/api/ideas/"+params.id).then((response) => {
            setIdea(response.data)
        })
    },[])

    
    var date = new Date(idea.timePosted * 1000)
    return (
        <div class="container-fluid">
            <div class="row">
                <div class="col-lg-8 col-md-8 col-sm-auto mb-2">
                    <IdeaCard title={idea.title} content={idea.content} />
                </div>
                <div class="col-md-auto col-sm-auto">
                    <ul class="card list-group list-group-flush">
                        <li class="list-group-item">By {idea.authorUsername}<br></br>on {date.toLocaleDateString()}</li>
                        <li class="list-group-item">
                            <button type="button" className="btn btn-outline-secondary btn-md">
                                Message author
                            </button>
                        </li>
                        <li class="list-group-item">
                            <button type="button" className="btn btn-primary btn-md">
                                Work on this idea
                            </button>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    )
}
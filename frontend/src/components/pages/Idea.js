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

    
    return (
        <div>
            <IdeaCard title={idea.title} content={idea.content} />
        </div>
    )
}
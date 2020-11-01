import React, { useEffect } from 'react'
import { useParams } from 'react-router-dom'
import axios from 'axios'

export default function Idea() {
    const [idea, setIdea] = React.useState([])
    let params = useParams()

    useEffect(() => {
        axios.get("/api/ideas/"+params.id).then((response) => {
            setIdea(response.data)
        })
    },[])

    
    return (
        <div className="mx-auto">
            <h1>{idea.title}</h1>
            {idea.content}
        </div>
    )
}
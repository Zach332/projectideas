import React, { useEffect } from 'react'
import IdeaSummary from './../IdeaSummary'
import axios from 'axios'

export default function Home() {
    const [ideas, setIdeas] = React.useState([])

    useEffect(() => {
        axios.get("/api/ideas").then((response) => {
            setIdeas(response.data)
            console.log(response.data)
        })
    },[])

    
    return (
        <div className="mx-auto">
            <h1>Home</h1>
            {ideas.map(idea => <IdeaSummary key={idea.id} idea={idea} />)}
        </div>
    )
}
import React, { useEffect } from 'react'
import axios from 'axios'
import { useToasts } from 'react-toast-notifications'

export default function Comments({ideaId}) {
    const { addToast } = useToasts()
    const [rows, setRows] = React.useState(1)
    const [showButton, setShowButton] = React.useState('hidden')
    const [comment, setComment] = React.useState('')

    const onFocus = () => {
        setRows(5)
        setShowButton('visible')
    }

    useEffect(() => {
        axios.get("/api/ideas/"+ideaId+"/comments").then((response) => {
            console.log(response)
            console.log(response.data)
        })
    },[])

    const handleInputChange = (event) => {
        setComment(event.target.value)
    }
    const handleSubmit = (event) => {
        axios.post("/api/ideas/"+ideaId+"/comments", {
            content: comment
        }).then(() => {
            setComment('')
            addToast("Your comment was added successfully.", { appearance: 'success' })
        }).catch(err => {
            console.log("Error submitting comment: " + err);
            addToast("Your comment was not submitted. Please try again.", { appearance: 'error' })
        })
        event.preventDefault()
    }
    
    return (
        <div className="w-75 mt-5">
           <form onSubmit={handleSubmit}>
                <div className="form-row align-items-top">
                    <div className="col w-100">
                        <textarea type="text" className="form-control mb-2" id="inlineFormInput" rows={rows} onFocus={onFocus} onChange={handleInputChange} placeholder="Write a comment "/>
                    </div>
                    <div className="col-auto align-top">
                        <button type="submit" className="btn btn-primary mb-2" style={{visibility:showButton}}>Submit</button>
                    </div>
                </div>
            </form> 
        </div>
    )
}

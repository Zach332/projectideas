import React, { useEffect } from 'react'
import axios from 'axios'
import { useToasts } from 'react-toast-notifications'
import Comment from './Comment'

export default function Comments({ideaId}) {
    const { addToast } = useToasts()
    const [rows, setRows] = React.useState(1)
    const [showButton, setShowButton] = React.useState('hidden')
    const [comment, setComment] = React.useState('')
    const [comments, setComments] = React.useState([])
    const [rerender, setRerender] = React.useState(0)

    const onFocus = () => {
        setRows(5)
        setShowButton('visible')
    }

    useEffect(() => {
        axios.get("/api/ideas/"+ideaId+"/comments").then((response) => {
            setComments(response.data)
        })
    },[rerender])

    const handleInputChange = (event) => {
        setComment(event.target.value)
    }
    const handleSubmit = (event) => {
        axios.post("/api/ideas/"+ideaId+"/comments", {
            content: comment
        }).then(() => {
            setComment('')
            setRerender(rerender => rerender+1)
            addToast("Your comment was added successfully.", { appearance: 'success' })
        }).catch(err => {
            console.log("Error submitting comment: " + err);
            addToast("Your comment was not submitted. Please try again.", { appearance: 'error' })
        })
        event.preventDefault()
    }
    
    return (
        <div className="w-75 mt-5">
           <form className="mb-3" onSubmit={handleSubmit}>
                <div className="form-row align-items-top">
                    <div className="col w-100">
                        <textarea type="text" className="form-control mb-2" id="inlineFormInput" rows={rows} onFocus={onFocus} onChange={handleInputChange} placeholder="Write a comment "/>
                    </div>
                    <div className="col-auto align-top">
                        <button type="submit" className="btn btn-primary mb-2" style={{visibility:showButton}}>Submit</button>
                    </div>
                </div>
            </form>
            {comments.map(comment => <Comment key={comment.id} comment={comment} parentId={ideaId} setRerender={setRerender}/>)}
        </div>
    )
}

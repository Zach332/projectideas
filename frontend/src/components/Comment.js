import React from 'react'
import axios from 'axios'
import { useGlobalState } from '../State'
import { useToasts } from 'react-toast-notifications'
import { motion, AnimatePresence } from 'framer-motion'

export default function Comment({comment, parentId, setRerender}) {
    const { addToast } = useToasts()
    const [ user ] = useGlobalState('user')
    const deleteComment = () => {
        axios.delete("/api/ideas/"+parentId+"/comments/"+comment.id)
        .then(() => {
            addToast("Your comment was deleted.", { appearance: 'success', autoDismiss: true })
            setRerender(rerender => rerender+1)
        }).catch(err => {
            console.log("Error deleting comment: " + err);
            addToast("Your comment was not deleted. Please try again.", { appearance: 'error' })
        })
    }

	return (
        <motion.div layout className="list-group-item flex-column align-items-start my-2 rounded border">
            {(user.username === comment.authorUsername || user.isAdmin) &&
                <div className="dropdown">
                    <button className="btn btn-sm btn-outline-secondary float-right" type="button" id="dropdownMenuButton" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                        <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-three-dots-vertical" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                            <path fill-rule="evenodd" d="M9.5 13a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0zm0-5a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0zm0-5a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0z"/>
                        </svg>
                    </button>
                    <div className="dropdown-menu" aria-labelledby="dropdownMenuButton">
                        <a className="dropdown-item text-danger" onClick={deleteComment}>Delete comment</a>
                    </div>
                </div>
            }
            <p className="mb-1">{comment.content}</p>
            <small className="text-muted">{comment.authorUsername}</small>
        </motion.div>
	);
}
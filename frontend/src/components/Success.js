import React, { useEffect } from 'react'
import CheckMark from "../check.svg"
import { useKeyPress } from "./hooks/Keys"

export default function Success() {
    const enterPress = useKeyPress('Enter');

    useEffect(() => {
        if(enterPress) {
            window.location.href = "/"
        }
    },[enterPress])

    const onClick = () => {
        window.location.href = '/'
    }

    return (
        <div>
            <img src={CheckMark} className="mx-auto d-block p-4" alt="Successful login" />
            <div className="text-center">
                <button type="button" className="btn btn-link btn-lg" onClick={onClick}>
                    Homepage (press enter)
                    <svg width="1em" height="1em" viewBox="0 0 16 16" className="bi ml-1 bi-arrow-return-left" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                        <path fill-rule="evenodd" d="M14.5 1.5a.5.5 0 0 1 .5.5v4.8a2.5 2.5 0 0 1-2.5 2.5H2.707l3.347 3.346a.5.5 0 0 1-.708.708l-4.2-4.2a.5.5 0 0 1 0-.708l4-4a.5.5 0 1 1 .708.708L2.707 8.3H12.5A1.5 1.5 0 0 0 14 6.8V2a.5.5 0 0 1 .5-.5z"/>
                    </svg>
                </button>
            </div>
        </div>
    )
}

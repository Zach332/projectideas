import React, { useEffect } from 'react'
import CheckMark from "../../check.svg"
import { toParams } from '../utils/Routing'
import axios from 'axios'
import { login } from '../../State'

export default function LoginLanding() {

    useEffect(() => {
        const params = toParams(window.location.search.replace(/^\?/, ''))
        onSuccess(params)
    }, [])

    const onSuccess = (data) => {
        if (!data.code) {
          onFailure(new Error('\'code\' not found'));
        }
        axios.post("/api/login/github", {
            code: data.code
        }).then((response) => {
            login(response.data.username, response.data.id)
        })
    }
    const onFailure = (data) => {
        console.log("Failure to retrieve code from login data: "+data)
    }

    return (
        <div>
            <img src={CheckMark} className="mx-auto d-block pt-4" alt="Successful login" />
        </div>
    )
}

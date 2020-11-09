import React, { useEffect } from 'react'
import CheckMark from "../../check.svg"
import XMark from "../../x.svg"
import { toParams } from '../utils/Routing'
import axios from 'axios'
import { login } from '../../State'

export default function LoginLanding() {
    const [status, setStatus] = React.useState('loading')


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
            setStatus('success')
        }).catch(err => {
            onFailure(err);
        })
    }
    const onFailure = (error) => {
        console.log("Failure to retrieve code from login data: "+error)
        setStatus('failure')
    }

    let result
    if(status == 'success') {
        result = <img src={CheckMark} className="mx-auto d-block pt-4" alt="Successful login" />
    } else if(status == 'failure') {
        result = (
            <div className="text-center">
                <img src={XMark} className="mx-auto w-25 d-block py-4" alt="Login failed" />
                <h2>Login failed</h2>
                <a className="btn btn-primary mt-4 btn-lg" href="/login" role="button">Try again</a>
            </div>
        )
    }

    return (
        <div>
            {result}
        </div>
    )
}

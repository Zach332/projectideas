import React from 'react'
import CheckMark from "../../check.svg"
import { toParams } from '../utils/Routing'
import axios from 'axios';

export default function LoginLanding() {
    const onSuccess = (data) => {
        if (!data.code) {
          onFailure(new Error('\'code\' not found'));
        }
        axios.post("/api/login/github", {
            code: data.code
        }).then(res => {
            console.log("Response: "+res);
            console.log(res.access_token);
        })
    }
    const onFailure = (data) => {
        console.log("Failure to retrieve code from login data: "+data)
    }
    const params = toParams(window.location.search.replace(/^\?/, ''))
    onSuccess(params)
    return (
        <div>
            <img src={CheckMark} class="mx-auto d-block pt-4" alt="Successful login" />
        </div>
    )
}

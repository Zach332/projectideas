import React from 'react'
import CheckMark from "../../check.svg"
import { toParams } from '../utils/Routing'

export default function LoginLanding() {
    const onSuccess = (data) => {
        if (!data.code) {
          onFailure(new Error('\'code\' not found'));
        }
        console.log(data.code)
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

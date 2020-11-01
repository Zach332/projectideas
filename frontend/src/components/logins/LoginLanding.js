import React, { useEffect } from 'react'
import CheckMark from "../../check.svg"
import { toParams } from '../utils/Routing'
import axios from 'axios'
import { login, persistenceKey, useGlobalState } from '../../State'

export default function LoginLanding() {
    const [user] = useGlobalState('user');

    useEffect(() => {
        localStorage.setItem(persistenceKey, JSON.stringify(user))
    }, [user]);

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
            login(response.data.id, response.data.id)
        })
    }
    const onFailure = (data) => {
        console.log("Failure to retrieve code from login data: "+data)
    }

    return (
        <div>
            <img src={CheckMark} class="mx-auto d-block pt-4" alt="Successful login" />
        </div>
    )
}

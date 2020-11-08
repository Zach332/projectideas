import React, { useEffect } from 'react'
import { login, logout, useGlobalState } from '../../State'
import LoginWarning from '../logins/LoginWarning'
import axios from 'axios'

export default function Profile() {
    const [ user ] = useGlobalState('user')
    const [userData, setUserData] = React.useState([])
    const [changingUsername, setChangingUsername] = React.useState(false)

    useEffect(() => {
        axios.get("/api/users/"+user.id).then((response) => {
            setUserData(response.data)
        })
    },[])

    const handleInputChange = (event) => {
        const target = event.target;
        const name = target.id;
        setUserData(
            userData => ({
                ...userData,
                [name]: target.value
            })
        );
    }
    const handleSubmit = (event) => {
        axios.put("/api/users/"+user.id, {
            username: userData.username,
            email: userData.email
        }).then((_response) => {
            login(userData.username, user.id)
        })
        setChangingUsername(false)
        event.preventDefault()
    }
    const handleChange = (event) => {
        setChangingUsername(true)
        event.preventDefault()
    }

    let usernameForm
    if(!changingUsername) {
        usernameForm = (
            <form className="form-inline my-5" onSubmit={handleChange}>
                <label htmlFor="username" className="col-sm-2 col-form-label">Username</label>
                <div className="mx-sm-3">
                    <input type="text" className="form-control" id="username" value={userData.username} readOnly/>
                </div>
                <button type="submit" className="btn btn-primary">Change</button>
            </form>
        )
    } else {
        usernameForm = (
            <form className="form-inline my-5" onSubmit={handleSubmit}>
                <label htmlFor="username" className="col-sm-2 col-form-label">Username</label>
                <div className="mx-sm-3">
                    <input type="text" className="form-control" id="username" placeholder="AwesomeNewUsername" onChange={handleInputChange} />
                </div>
                <button type="submit" className="btn btn-primary">Submit</button>
            </form>
        )
    }

    const onCLick = () => {
        logout()
    }

    if(!user.loggedIn) {
        return <LoginWarning />
    }
    
    return (
        <div>
            <h1>Profile</h1>
            {usernameForm}
            <form className="form-inline my-5">
                <label htmlFor="email" className="col-sm-2 col-form-label">Email</label>
                <div className="mx-sm-3">
                    <input type="text" className="form-control" id="email" value={userData.email} readOnly/>
                </div>
                <small id="emailCOmment" class="form-text text-muted">
                    Primary email from GitHub
                </small>
            </form>
                <button type="button" onClick={onCLick} className="btn btn-danger btn-md">
                    Log Out
                </button>
        </div>
    )
}
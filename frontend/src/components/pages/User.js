import React, { useEffect } from 'react'
import { login, useGlobalState } from '../../State'
import axios from 'axios'

export default function User() {
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
                <div className="form-group mx-sm-3 mb-2">
                    <input type="text" className="form-control" id="username" placeholder={userData.username} readOnly/>
                </div>
                <button type="submit" className="btn btn-primary mb-2">Change</button>
            </form>
        )
    } else {
        usernameForm = (
            <form className="form-inline my-5" onSubmit={handleSubmit}>
                <label htmlFor="username" className="col-sm-2 col-form-label">Username</label>
                <div className="form-group mx-sm-3 mb-2">
                    <input type="text" className="form-control" id="username" placeholder="AwesomeNewUsername" onChange={handleInputChange} />
                </div>
                <button type="submit" className="btn btn-primary mb-2">Submit</button>
            </form>
        )
    }

    
    return (
        <div>
            <h1>Profile</h1>
            {usernameForm}
            <p><span className="pr-5"><strong>Email</strong></span> {userData.email}
                <br></br><small>Primary email from GitHub</small>
            </p>
        </div>
    )
}
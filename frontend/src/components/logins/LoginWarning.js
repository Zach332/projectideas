import React from 'react'

export default function LoginWarning() {
    return (
        <div className="jumbotron bg-light-danger">
            <h1 className="display-4">Login required</h1>
            <p className="lead">You must login to view this page.</p>
            <hr className="my-4"/>
            <a className="btn btn-primary btn-lg" href="/login" role="button">Login here</a>
        </div>
    )
}

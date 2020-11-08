import React from 'react'

export default function LoginWarning() {
    return (
        <div class="jumbotron bg-light-danger">
            <h1 class="display-4">Login required</h1>
            <p class="lead">You must login to view this page.</p>
            <hr class="my-4"/>
            <a class="btn btn-primary btn-lg" href="/login" role="button">Login here</a>
        </div>
    )
}

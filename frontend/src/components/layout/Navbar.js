import React from 'react'
import NameLogo from "../../namelogo.png"
import { useGlobalState } from '../../State'

export default function Navbar() {
    const [ user ] = useGlobalState('user')

    let login
    
    if(user.loggedIn) {
        login = (
            <li className="nav-item active">
                <a className="nav-link" href="/about">{user.username}</a>
            </li>
        )
    } else {
        login = (
            <li className="navbar-btn">
                <a href="/login" className="btn btn-outline-success my-2 my-sm-0">Login</a>
            </li>
        )
    }
    
    return (
        <div>
            <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
                <a className="navbar-brand" href="/">
                    <img src={NameLogo} alt="" />
                </a>
                <button className="navbar-toggler mr-auto" type="button" data-toggle="collapse" data-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                    <span className="navbar-toggler-icon"></span>
                </button>
                <div className="collapse navbar-collapse" id="navbarNav">
                    <ul className="navbar-nav">
                        <li className="nav-item active">
                            <a className="nav-link" href="/about">About</a>
                        </li>
                    </ul>
                </div>
                <div className="nav navbar-nav navbar-right" id="navbarLogin">
                    <ul className="navbar-nav">
                        {login}
                    </ul>
                </div>
            </nav>
        </div>
    )
}

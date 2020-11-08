import React from 'react';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom'
import Navbar from './components/layout/Navbar'
import Home from './components//pages/Home'
import About from './components/pages/About'
import Privacy from './components/pages/Privacy'
import NotFound from './components/pages/NotFound'
import Idea from './components/pages/Idea'
import Profile from './components/pages/Profile'
import NewIdea from './components/pages/NewIdea'
import GitHubLogin from './components/logins/GitHubLogin'
import LoginLanding from './components/logins/LoginLanding'

function App () {
    return (
        <Router>
            <div className="App">
                <Navbar />
                <div className="container mx-auto">
                    <Switch>
                        <Route path="/" exact component={Home} />
                        <Route path="/login" exact component={GitHubLogin} />
                        <Route path="/login/oauth2/code/github" exact component={LoginLanding} />
                        <Route path="/new-idea" exact component={NewIdea} />
                        <Route path="/about" exact component={About} />
                        <Route path="/privacy" exact component={Privacy} />
                        <Route path="/idea/:id" exact component={Idea} />
                        <Route path="/profile" exact component={Profile} />
                        <Route component={NotFound} />
                    </Switch>
                </div>
            </div>
        </Router>
    );
}

export default App;

import React from 'react';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom'
import Navbar from './components/layout/Navbar'
import ServerTime from './components/ServerTime'
import About from './components/pages/About'
import Privacy from './components/pages/Privacy'
import NotFound from './components/pages/NotFound'

function App () {
    return (
        <Router>
          <div className="App">
            <Navbar />
            <div className="container mx-auto">
              <Switch>
                <Route path="/" exact component={ServerTime} />
                <Route path="/about" exact component={About} />
                <Route path="/privacy" exact component={Privacy} />
                <Route component={NotFound} />
              </Switch>
            </div>
          </div>
        </Router>
      );
}

export default App;

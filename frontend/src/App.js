import { BrowserRouter as Router, Route, Switch } from "react-router-dom";
import { ToastProvider } from "react-toast-notifications";
import Navbar from "./components/layout/Navbar";
import Home from "./components/pages/Home";
import About from "./components/pages/About";
import Privacy from "./components/pages/Privacy";
import NotFound from "./components/pages/NotFound";
import Idea from "./components/pages/Idea";
import Project from "./components/pages/Project";
import Profile from "./components/pages/Profile";
import CreateIdea from "./components/pages/CreateIdea";
import Messages from "./components/pages/Messages";
import Markdown from "./components/pages/Markdown";
import Search from "./components/pages/Search";
import Tags from "./components/pages/Tags";
import JoinProject from "./components/pages/JoinProject";
import CreateProject from "./components/pages/CreateProject";
import GitHubLogin from "./components/logins/Login";
import LoginLandingGithub from "./components/logins/LoginLandingGithub";
import LoginLandingGoogle from "./components/logins/LoginLandingGoogle";
import MyProjects from "./components/pages/MyProjects";
import Projects from "./components/pages/Projects";
import StyleDiv from "./components/general/StyleDiv";
import Footer from "./components/layout/Footer";
import { HelmetProvider } from "react-helmet-async";
import "bootstrap/dist/js/bootstrap.bundle.min.js";

function App() {
    return (
        <StyleDiv>
            <ToastProvider>
                <HelmetProvider>
                    <Router>
                        <div
                            className="App"
                            style={{
                                height: "100vh",
                                display: "flex",
                                flexDirection: "column",
                            }}
                        >
                            <Navbar />
                            <div className="container mx-auto">
                                <Switch>
                                    <Route path="/" exact component={Home} />
                                    <Route
                                        path="/projects"
                                        exact
                                        component={Projects}
                                    />
                                    <Route
                                        path="/my-projects"
                                        exact
                                        component={MyProjects}
                                    />
                                    <Route
                                        path="/login"
                                        exact
                                        component={GitHubLogin}
                                    />
                                    <Route
                                        path="/login/oauth2/code/github"
                                        exact
                                        component={LoginLandingGithub}
                                    />
                                    <Route
                                        path="/login/oauth2/code/google"
                                        exact
                                        component={LoginLandingGoogle}
                                    />
                                    <Route
                                        path="/new-idea"
                                        exact
                                        component={CreateIdea}
                                    />
                                    <Route
                                        path="/search"
                                        exact
                                        component={Search}
                                    />
                                    <Route
                                        path="/tags"
                                        exact
                                        component={Tags}
                                    />
                                    <Route
                                        path="/about"
                                        exact
                                        component={About}
                                    />
                                    <Route
                                        path="/privacy"
                                        exact
                                        component={Privacy}
                                    />
                                    <Route
                                        path="/idea/:id"
                                        exact
                                        component={Idea}
                                    />
                                    <Route
                                        path="/project/:id"
                                        exact
                                        component={Project}
                                    />
                                    <Route
                                        path="/join/idea/:id"
                                        exact
                                        component={JoinProject}
                                    />
                                    <Route
                                        path="/create/idea/:id"
                                        exact
                                        component={CreateProject}
                                    />
                                    <Route
                                        path="/profile"
                                        exact
                                        component={Profile}
                                    />
                                    <Route
                                        path="/messages"
                                        exact
                                        component={Messages}
                                    />
                                    <Route
                                        path="/markdown"
                                        exact
                                        component={Markdown}
                                    />
                                    <Route component={NotFound} />
                                </Switch>
                            </div>
                            <Footer />
                        </div>
                    </Router>
                </HelmetProvider>
            </ToastProvider>
        </StyleDiv>
    );
}

export default App;

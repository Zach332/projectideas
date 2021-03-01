import { createGlobalState } from "react-hooks-global-state";
import axios from "axios";

export const Status = {
    Loading: "Loading",
    Loaded: "Loaded",
    Success: "Success",
    Failure: "Failure",
    NotSubmitted: "NotSubmitted",
    NotFound: "NotFound",
};

export const userPersistenceKey = "user_persistent_storage";
export const newIdeaPersistenceKey = "new_idea_persistent_storage";
export const themePersistenceKey = "theme_persistent_storage";

const firstUserState = {
    loggedIn: false,
    id: null,
    username: null,
    admin: false,
};

const firstNewideaState = {
    title: "",
    content: "",
    tags: [],
};

const firstThemeState = {
    mode: "dark",
};

const userFromStorage = localStorage.getItem(userPersistenceKey);
const userObject = JSON.parse(userFromStorage);
const user =
    userFromStorage === null || !userObject.id ? firstUserState : userObject;
const newIdeaFromStorage = localStorage.getItem(newIdeaPersistenceKey);
const newIdeaObject = JSON.parse(newIdeaFromStorage);
const newIdea = newIdeaFromStorage === null ? firstNewideaState : newIdeaObject;
const themeFromStorage = localStorage.getItem(themePersistenceKey);
const themeObject = JSON.parse(themeFromStorage);
const theme = themeObject === null ? firstThemeState : themeObject;

const initialState = { user, newIdea, theme };

axios.defaults.headers.common["authorization"] = initialState.user.id;

const { setGlobalState, useGlobalState } = createGlobalState(initialState);

export const login = (username, id, admin) => {
    setGlobalState("user", (v) => ({
        ...v,
        loggedIn: true,
        username: username,
        id: id,
        admin: admin,
    }));
    axios.defaults.headers.common["authorization"] = id;
};

export const logout = () => {
    setGlobalState("user", firstUserState);
};

export const post = () => {
    setGlobalState("newIdea", firstNewideaState);
};

export const setTheme = (newMode) => {
    setGlobalState("theme", (prevTheme) => ({
        ...prevTheme,
        mode: newMode,
    }));
};

export { useGlobalState };

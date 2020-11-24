import { createGlobalState } from 'react-hooks-global-state';
import axios from 'axios'

export const Status = {
    Loading: "Loading",
    Success: "Success",
    Failure: "Failure",
    NotSubmitted: "NotSubmitted"
}

export const userPersistenceKey = 'user_persistent_storage'
export const newIdeaPersistenceKey = 'new_idea_persistent_storage'

const firstUserState = {
    user: {
        loggedIn: false,
        id: '',
        username: ''
    }
};

const firstNewideaState = {
    newIdea: {
        title: '',
        content: ''
    }
}

const userFromStorage = localStorage.getItem(userPersistenceKey)
const userObject = JSON.parse(userFromStorage)
const user = userFromStorage === null || !userObject.id
    ? firstUserState
    : userObject
const newIdeaFromStorage = localStorage.getItem(newIdeaPersistenceKey)
const newIdeaObject = JSON.parse(newIdeaFromStorage)
const newIdea = newIdeaFromStorage === null
    ? firstNewideaState
    : newIdeaObject

const initialState = { user , newIdea}
console.log(initialState)
    
axios.defaults.headers.common['authorization'] = initialState.user.id;

const { setGlobalState, useGlobalState } = createGlobalState(initialState);

export const login = (username, id) => {
    setGlobalState('user', (v) => ({ ...v, 
        loggedIn: true,
        username: username,
        id: id
    }))
    axios.defaults.headers.common['authorization'] = id;
};

export const logout = () => {
    setGlobalState('user', firstUserState)
};

export const post = () => {
    setGlobalState('newIdea', firstNewideaState)
};

export { useGlobalState };
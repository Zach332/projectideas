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

const firstState = {
    user: {
        loggedIn: false,
        id: '',
        username: ''
    },
    newIdea: {
        title: '',
        content: ''
    }
};

const userFromStorage = localStorage.getItem(userPersistenceKey)
const user = JSON.parse(userFromStorage)
const newIdeaFromStorage = localStorage.getItem(newIdeaPersistenceKey)
const newIdea = JSON.parse(newIdeaFromStorage)

const initialState = userFromStorage === null || !user.id
	? firstState
    : { user , newIdea}
    
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
    setGlobalState('user', firstState)
};

export { useGlobalState };
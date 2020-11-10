import { createGlobalState } from 'react-hooks-global-state';
import axios from 'axios'

export const Status = {
    Loading: "Loading",
    Success: "Success",
    Failure: "Failure",
    NotSubmitted: "NotSubmitted"
}

export const persistenceKey = 'projectideas_persistent_storage'

const firstState = {
    user: {
        loggedIn: false,
        id: '',
        username: ''
    }
};

const userFromStorage = localStorage.getItem(persistenceKey)
const user = JSON.parse(userFromStorage)

const initialState = userFromStorage === null || !user.id
	? firstState
    : { user }
    
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
import { createGlobalState } from 'react-hooks-global-state';

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

const { setGlobalState, useGlobalState } = createGlobalState(initialState);

export const login = (username, id) => {
    setGlobalState('user', (v) => ({ ...v, 
        loggedIn: true,
        username: username,
        id: id
    }))
};

export { useGlobalState };
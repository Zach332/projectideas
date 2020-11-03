import { createGlobalState } from 'react-hooks-global-state';

export const persistenceKey = 'projectideas_persistent_storage'

const firstState = {
    user: {
        loggedIn: false,
        UID: '',
        username: ''
    }
};

const userFromStorage = localStorage.getItem(persistenceKey)
const user = JSON.parse(userFromStorage)

const initialState = userFromStorage === null || !user.UID
	? firstState
	: { user }

const { setGlobalState, useGlobalState } = createGlobalState(initialState);

export const login = (username, UID) => {
    setGlobalState('user', (v) => ({ ...v, 
        loggedIn: true,
        username: username,
        UID: UID
    }))
};

export { useGlobalState };
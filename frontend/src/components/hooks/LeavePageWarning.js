import { useEffect } from 'react'

export function useLeavePageWarning(warnUser) {
    const alertUser = (e) => {
        if(!warnUser) {
            return undefined
        }
        e.preventDefault()
        e.returnValue = ''
    }

    useEffect(() => {
        window.addEventListener('beforeunload', alertUser)
        return () => {
            window.removeEventListener('beforeunload', alertUser)
        };
    });
}
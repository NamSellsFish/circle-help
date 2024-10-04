import { useSelector } from 'react-redux'

export default function useVerify() {
    // @ts-ignore
    const { token } = useSelector(state => state.user)
    if (!token) return false
    return true
}
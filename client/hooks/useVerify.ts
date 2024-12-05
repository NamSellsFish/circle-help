import { useAppSelector } from "~/hooks";

export default function useVerify() {
    // @ts-ignore
    const { token } = useAppSelector(state => state.user)
    if (!token) return false
    return true
}
import { RouteParamInput, Routes, useLocalSearchParams, useRouter } from 'expo-router'

export default function useChangeRoute() {
    const router = useRouter()
    const params = useLocalSearchParams()

    const changeRoute = (newQueries: Partial<RouteParamInput<Routes>>) => {
        router.setParams({ ...params, ...newQueries })
        // router.setParams({ ...newQueries })

    }

    return changeRoute
}
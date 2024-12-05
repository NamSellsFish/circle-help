import { RouteParamInput, Routes, useLocalSearchParams } from "expo-router"
import { useChangeRoute } from "~/hooks"

//? Assets
const params = useLocalSearchParams()
const changeRoute = useChangeRoute()

const handleChangeRoute = (newQueries: Partial<RouteParamInput<Routes>>) => {
    changeRoute({
        ...params,
        page: 0,
        ...newQueries,
    })
}
export default handleChangeRoute
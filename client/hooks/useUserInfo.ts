import { router } from 'expo-router'

import { useAppDispatch } from './useRedux'
import useVerify from './useVerify'

import { userLogout } from '~/store'
import { useGetUserInfoQuery } from '~/services/user.service'

export default function useUserInfo() {
    const dispatch = useAppDispatch()
    const isVerify = useVerify()

    const { data, isLoading, error, isError } = useGetUserInfoQuery(undefined, {
        skip: !isVerify,
    })

    const isLoginVerify = !isVerify ? false : isLoading ? false : !!data?.data

    const mustAuthAction = (nextAction: () => void) => {
        if (!isLoginVerify) {
            return router.push('/login')
        }
        nextAction()
    }

    // Error case mainly because sending wrong token
    if (isError) { dispatch(userLogout()); console.error(`Error message: ${error}`) }

    return {
        userInfo: data?.user,
        isVerify,
        isLoginVerify,
        mustAuthAction,
        isLoading,
        error,
        isError,
    }
}

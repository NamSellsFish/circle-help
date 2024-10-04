import { PropsWithChildren, useEffect } from "react"
import { router } from 'expo-router'
import { Image, Text, TouchableOpacity, View } from 'react-native'
import { useUserInfo } from '~/hooks'

type AuthWrapperProps = PropsWithChildren<{ tips?: string }>

export default function AuthWrapper({ children, tips = 'Sign in now to do you job' }: AuthWrapperProps) {
    const { userInfo, isVerify, isLoading } = useUserInfo()
    //const { userInfo, isVerify, isLoading } = { userInfo: null, isVerify: true, isLoading: false }

    const handleJumpLogin = () => {
        router.push('/login')
    }
    return (
        <>{isLoading ? null : !isVerify || !userInfo ?
            <>
                <View className="flex items-center justify-center h-full space-y-4 bg-white">
                    <Image source={require('../../../assets/favicon.png')} className="w-[100vw] h-[58vw]" />
                    <View className="px-4 space-y-2 flex items-center justify-center">
                        <Text className="text-lg">You are not log in</Text>
                        <Text className="text-sm">{tips}</Text>
                    </View>
                    <TouchableOpacity
                        onPress={handleJumpLogin}
                        className="py-2 px-8 flex-center bg-red-500 rounded-full"
                    >
                        <Text className="text-sm text-white">
                            Go to Log In
                        </Text>
                    </TouchableOpacity>
                </View>
            </>
            :
            <>{children}</>}</>
    )
}
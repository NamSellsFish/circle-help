import { Stack } from 'expo-router'
import React from 'react'
import { Text, View, Image } from 'react-native'

export default function NotFoundPage() {
    const MOCK_EXPO_PUBLIC_BASE_URL = process.env.MOCK_EXPO_PUBLIC_BASE_URL
    //? Render(s)
    return (
        <>
            <Stack.Screen
                options={{
                    title: '404 Not Found!',
                    headerBackTitleVisible: false,
                }}
            />
            <View className="flex h-full flex-col items-center justify-center py-8 gap-y-6 bg-white">
                <Text className="text-base font-semibold text-black">404 Not Found!</Text>
                <Image
                    style={{
                        width: '100%',
                        resizeMode: 'contain',
                    }}
                    source={require('../assets/favicon.png')}
                    alt="404"
                />
            </View>
        </>
    )
}

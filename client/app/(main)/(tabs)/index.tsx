import { Stack } from "expo-router";
import React from "react";
import { Text } from 'react-native';
import AuthWrapper from "~/components/mobile/atoms/AuthWrapper";
import { useUserInfo } from "~/hooks";


function ProfileScreen() {
    const { userInfo, isLoading } = useUserInfo()
    return (
        <>
            <Stack.Screen
                options={{
                    headerShown: false,
                }}
            />
            <AuthWrapper>
                <Text className="text-xl font-bold text-red-400">Hello Sun. Is this your account? {JSON.stringify(userInfo)}</Text>
            </AuthWrapper>
        </>
    );
}

export default ProfileScreen;
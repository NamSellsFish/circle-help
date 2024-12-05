import { Entypo, MaterialCommunityIcons } from "@expo/vector-icons";
import { Stack } from "expo-router";
import React, { PropsWithChildren, useState } from "react";
import { Text, TextInput, View } from 'react-native';
import { AuthWrapper, Button, Icons, UserEmailModal, UserNameModal } from "~/components";
import { useAppDispatch, useAppSelector, useDisclosure, useUserInfo } from "~/hooks";
import { useGetCompartmentsQuery } from "~/services";
import { resetAttendanceInfoItem, userLogout } from "~/store";

type InfoFieldProps = PropsWithChildren<{ label: string, info: string | undefined, editHandler: () => void, isLoading: boolean }>

function ProfileScreen() {
    //? Assets
    const [isShowNameModal, nameModalHandlers] = useDisclosure()
    const [isShowEmailModal, emailModalHandlers] = useDisclosure()
    const { userInfo, isLoading, error, isError, isVerify } = useUserInfo()

    //? Get User Data
    //const { userInfo, isLoading } = { 'userInfo': { 'email': 'root@admin.com', 'password': 'password', 'username': 'root', 'role': 'staff' }, 'isLoading': false }
    const dispatch = useAppDispatch()
    const { token } = useAppSelector(state => state.user)
    const attendance = useAppSelector(state => state.attendance)

    const handleLogout = () => {
        dispatch(userLogout())
    }

    //? Local Component
    const InfoField = ({ label, info, editHandler, isLoading }: InfoFieldProps) => {
        const [isPasswordSecure, setIsPasswordSecure] = useState(true);

        return (
            <View className="flex px-5">
                <View className="flex flex-row items-center justify-between py-4 border-b border-gray-200">
                    <View className="flex gap-y-2">
                        <Text className="text-xs font-bold text-gray-700">{label}</Text>
                        {isLoading ? (
                            <Text className="h-5 text-sm">Loading ...</Text>
                        ) : (
                            label !== 'Password' ? <Text className="h-5 text-sm">{info}</Text>
                                :
                                <View className="flex-row justify-between w-[80vw] ">
                                    <Text className="h-5 text-sm">{isPasswordSecure ? '*'.repeat(info ? info.length : 0) : info}</Text>
                                    <Entypo className="absolute -top-3 -right-3" onPress={() => isPasswordSecure ? setIsPasswordSecure(false) : setIsPasswordSecure(true)} name={`${isPasswordSecure ? 'eye' : 'eye-with-line'}`} size={20} color="black" />
                                </View>

                        )}
                    </View>
                    {isLoading || label === 'Role' ? null : info ? (
                        <Icons.Feather
                            onPress={editHandler}
                            name="edit"
                            size={16}
                            className="cursor-pointer icon"
                        />
                    ) : (
                        <Icons.Feather
                            onPress={editHandler}
                            name="plus"
                            size={16}
                            className="cursor-pointer icon"
                        />
                    )}
                </View>
            </View>
        )
    }

    return (
        <>
            <Stack.Screen
                options={{
                    headerShown: token !== '',
                    title: 'Profile',
                    headerRight: props => <MaterialCommunityIcons onPress={handleLogout} name="logout" size={24} color="black" />,
                }}
            />
            <AuthWrapper>
                {!isLoading && userInfo && (
                    <>
                        <UserNameModal
                            isShow={isShowNameModal === -111}
                            onClose={nameModalHandlers.close}
                            editedData={userInfo.user.username}
                        />
                        <UserEmailModal
                            isShow={isShowEmailModal === -111}
                            onClose={emailModalHandlers.close}
                            editedData={userInfo.user.email}
                        />
                        {/*<UserPasswordModal
                            isShow={isShowPasswordModal}
                            onClose={passwordModalHandlers.close}
                            editedData={userInfo.password}
                        /> */}
                    </>
                )}
                <View className=" flex-col justify-around h-full bg-white pt-[20%]">
                    <InfoField
                        label="Username"
                        info={userInfo?.user.username}
                        editHandler={() => nameModalHandlers.open()}
                        isLoading={isLoading}
                    />
                    <InfoField
                        label="Email"
                        info={userInfo?.user.email}
                        editHandler={() => emailModalHandlers.open()}
                        isLoading={isLoading}
                    />
                    {/* <InfoField
                        label="Password"
                        info={userInfo?.password}
                        editHandler={passwordModalHandlers.open}
                        isLoading={isLoading}
                    /> */}
                    <InfoField
                        label="Role"
                        info={userInfo?.user.role}
                        editHandler={emailModalHandlers.open}
                        isLoading={isLoading}
                    />
                     {/* <Button onPress={() => { dispatch(resetAttendanceInfoItem() )}}>Hihi</Button>  */}
                    {/* <Button onPress={() => { alert(JSON.stringify(attendance) )}}>Hihi</Button> */}
                </View>
            </AuthWrapper>
        </>
    );
}

export default ProfileScreen;
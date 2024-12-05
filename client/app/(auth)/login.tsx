import { Stack, useRouter } from "expo-router";
import { yupResolver } from "@hookform/resolvers/yup";
import { useForm } from "react-hook-form";
import { View } from 'react-native';
import { Button, TextField, HandleResponse, ResponsiveImage } from '~/components'
import { useEffect } from "react";
import { logInSchema } from "~/utils";
import { useAppDispatch, useAppSelector, useUserInfo } from "~/hooks";
import React from "react";
import { userLogIn } from "~/store";
import { useLoginMutation } from "~/services";


function LoginScreen() {

    //? Assets
    const dispatch = useAppDispatch()
    const router = useRouter()

    //? Login User
    const [login, { data: userData, isSuccess, isError, isLoading, error }] = useLoginMutation()

    //? Form Hook
    const {
        handleSubmit,
        formState: { errors: formErrors },
        setFocus,
        control,
    } = useForm({
        resolver: yupResolver(logInSchema),
        defaultValues: { email: '', password: '' },
    })

    //? Focus On Mount
    useEffect(() => {
        setFocus('email')
    }, [])

    //? Handlers
    const onSubmit = ({ email, password }: { email: string, password: string }) => {
        if (email && password) {
            login({
                body: { "user": { email, password } },
            })
        }
    }

    const onSuccess = () => {
        // alert(JSON.stringify(userData?.headers))
        dispatch(userLogIn(userData?.headers?.get('set-cookie')))
        // dispatch(userLogIn("token"))
        router.back()
    }

    return (
        <>
            <Stack.Screen
                options={{
                    title: 'Login',
                    headerShown: true,
                }}
            />
            {/*  Handle Login Response */}
            {(isSuccess || isError) && (
                <HandleResponse
                    isError={isError}
                    isSuccess={isSuccess}
                    error={`${JSON.stringify(error)}` || 'Abnormal!'}
                    //@ts-ignore
                    message={`Wellcome back ${userData?.data?.user?.username}`}
                    onSuccess={onSuccess}
                />
            )}
            <View className="h-[100%]  bg-white pt-10">
                <View className="w-[100vw] px-8 py-6 space-y-4">
                    <View className="space-y-0">
                        <ResponsiveImage alt={"Circle K"} style={{ width: '100%', opacity: 1, height: '50%', resizeMode: 'contain' }} source={'https://credentials.goldena.vn/wp-content/uploads/2023/06/Circle-K-Symbol.png'} />
                        <TextField
                            errors={formErrors.email}
                            placeholder="Please enter your account email"
                            name="email"
                            keyboardType="email-address"
                            autoCapitalize="none"
                            //@ts-ignore
                            control={control}
                        />

                        <TextField
                            errors={formErrors.password}
                            secureTextEntry
                            placeholder="Please enter your account password"
                            name="password"
                            //@ts-ignore
                            control={control}
                        />
                        <Button onPress={handleSubmit(onSubmit)} moreContainerClassNames="py-3 px-8" moreTextClassNames="text-white">
                            Sign In
                        </Button>
                    </View>

                </View>
            </View>

        </>
    );
}

export default LoginScreen;
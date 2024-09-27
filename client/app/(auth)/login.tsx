import { Stack, useRouter } from "expo-router";
import { yupResolver } from "@hookform/resolvers/yup";
import { useForm } from "react-hook-form";
import { View } from 'react-native';
import { Button, TextField, HandleResponse } from '~/components'
import { useEffect } from "react";
import { logInSchema } from "~/utils/validation";
import { useLoginMutation } from "~/services/user.service";
import { useAppDispatch } from "~/hooks/useRedux";
import { userLogIn } from "~/store";

function LoginScreen() {

    //? Assets
    const dispatch = useAppDispatch()
    const router = useRouter()

    //? Login User
    const [login, { data, isSuccess, isError, isLoading, error }] = useLoginMutation()

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
        dispatch(userLogIn(data.user.token))
        router.push('/profile')
    }

    return (
        <>
            <Stack.Screen
                options={{
                    title: 'Login',
                    headerShown: false,
                }}
            />
            {/*  Handle Login Response */}
            {(isSuccess || isError) && (
                <HandleResponse
                    isError={isError}
                    isSuccess={isSuccess}
                    // @ts-ignore
                    error={error?.data?.detail || 'Abnormal!'}
                    message={`Wellcome back ${data?.user.email}`}
                    onSuccess={onSuccess}
                />
            )}
            <View className="h-[100%]  bg-white pt-10">
                <View className="w-[100vw] px-8 py-6 space-y-4">
                    <View className="space-y-0">
                        <TextField
                            errors={formErrors.email}
                            placeholder="Please enter your account email"
                            name="email"
                            keyboardType="email-address"
                            autoCapitalize="none"
                            control={control}
                        />

                        <TextField
                            errors={formErrors.password}
                            secureTextEntry
                            placeholder="Please enter your account password"
                            name="password"
                            control={control}
                        />
                        <Button isLoading={false} onPress={handleSubmit(onSubmit)}>
                            Sign In
                        </Button>
                    </View>

                </View>
            </View>

        </>
    );
}

export default LoginScreen;
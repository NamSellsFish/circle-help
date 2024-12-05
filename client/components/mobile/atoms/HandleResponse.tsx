import { useEffect } from 'react'
import Toast from 'react-native-toast-message'

type HandleResponseProps = { isSuccess: boolean, isError: boolean, error: string, message: string, onSuccess?: () => void, onError?: () => void }

export default function HandleResponse({ isSuccess, isError, error, message, onSuccess, onError }: HandleResponseProps) {

    //? Re-Renders
    useEffect(() => {
        if (isSuccess) {
            if (onSuccess) onSuccess()
            Toast.show({
                type: 'success',
                text2: message,
            })
        }

        if (isError) {
            if (onError) onError()
            Toast.show({
                type: 'error',
                text2: error,
            })
        }
    }, [])

    return null
}

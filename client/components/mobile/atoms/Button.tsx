import { PropsWithChildren } from "react";
import { TouchableOpacity, TouchableOpacityProps, Text } from "react-native";
import Loading from "./Loading";

type ButtonProps = PropsWithChildren<{ isLoading: boolean, moreContainerClassNames?: string, moreTextClassNames?: string, isRounded?: boolean }> & TouchableOpacityProps

export default function Button({ children, isLoading = false, moreContainerClassNames = '', moreTextClassNames = '', isRounded = false, ...restProps }: ButtonProps) {
    return (
        <TouchableOpacity
            disabled={isLoading}
            className={`flex items-center outline-none rounded-md active:scale-[.98] bg-red-500 button ${isRounded ? 'rounded-3xl' : ''} ${moreContainerClassNames}`}
            {...restProps}

        >
            {isLoading ? <Loading /> : <Text className={`${moreTextClassNames}`}>{children}</Text>}
        </TouchableOpacity>

    )
}
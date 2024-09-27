import { PropsWithChildren } from "react";
import { TouchableOpacity, TouchableOpacityProps, Text } from "react-native";
import Loading from "./Loading";

type ButtonProps = PropsWithChildren<{ isLoading: boolean, moreClassNames?: string, isRounded?: boolean }> & TouchableOpacityProps

export default function Button({ children, isLoading = false, moreClassNames = '', isRounded = false, ...restProps }: ButtonProps) {
    return (
        <TouchableOpacity
            disabled={isLoading}
            className={`py-3 px-8 flex items-center outline-none rounded-md active:scale-[.98] bg-red-500 button ${isRounded ? 'rounded-3xl' : ''} ${moreClassNames}`}
            {...restProps}

        >
            {isLoading ? <Loading /> : <Text className="text-white">{children}</Text>}
        </TouchableOpacity>

    )
}
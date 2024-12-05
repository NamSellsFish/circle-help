import { FontAwesome } from "@expo/vector-icons";
import React from "react";
import { PropsWithChildren } from "react";
import { Pressable, PressableProps, View, Text, StyleProp, ViewStyle } from "react-native";
import ReactNativeModal from "react-native-modal";


type ModalProps = PropsWithChildren<{ isShow?: boolean, onClose?: () => void, moreClassNames?: string, style?: StyleProp<ViewStyle>, animationIn?: 'slideInRight', animationOut?: 'slideOutRight' }>

function Modal({ children, isShow, onClose, animationIn, animationOut, ...rnModalProps }: ModalProps) {
    const a = 'bounce'
    //? Render(s)
    return (
        <ReactNativeModal isVisible={isShow} onBackdropPress={onClose} animationIn={animationIn} animationOut={animationOut} {...rnModalProps}>
            {React.Children.map(children, child => {
                if (React.isValidElement(child)) {
                    return React.cloneElement(child as React.ReactElement<any>, { onClose })
                }

                return child
            })}
        </ReactNativeModal>
    )
}


function Content({ children, onClose, moreClassNames, ...restProps }: ModalProps) {
    return (
        <View className={moreClassNames} {...restProps} >
            {React.Children.map(children, child => {
                if (React.isValidElement(child)) {
                    return React.cloneElement(child as React.ReactElement<any>, { onClose: onClose })
                }

                return child
            })}
        </View>
    )
}

function Header({ children, onClose, modalHeaderLabel }: ModalProps & { modalHeaderLabel: string }) {
    return (
        <View className="flex flex-row items-center justify-between pb-2 border-b-2 border-gray-200 mb-2">
            <Text className="text-sm font-extrabold">{modalHeaderLabel}</Text>
            <Pressable onPress={onClose} className="p-1">
                {React.Children.map(children, child => {
                    if (React.isValidElement(child)) {
                        return React.cloneElement(child as React.ReactElement<any>, { onClose: onClose })
                    }

                    return child
                })}
            </Pressable>
        </View>
    )
}

function Body({ children }: ModalProps) {
    return <>{children}</>
}

const _default = Object.assign(Modal, {
    Modal,
    Content,
    Header,
    Body,
})

export default _default
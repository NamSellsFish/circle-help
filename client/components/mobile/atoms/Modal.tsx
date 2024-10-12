import { FontAwesome } from "@expo/vector-icons";
import React from "react";
import { PropsWithChildren } from "react";
import { Pressable, PressableProps, View, Text } from "react-native";
import ReactNativeModal from "react-native-modal";

type ModalProps = PropsWithChildren<{ isShow?: boolean, onClose?: () => void, moreClassNames: string }>

function Modal({ children, isShow, onClose, ...rnModalProps }: ModalProps) {

    //? Render(s)
    return (
        <ReactNativeModal isVisible={isShow} {...rnModalProps}>
            {React.Children.map(children, child => {
                if (React.isValidElement(child)) {
                    return React.cloneElement(child as React.ReactElement<any>, { onClose })
                }

                return child
            })}
        </ReactNativeModal>
    )
}


function Content({ children, onClose, moreClassNames }: ModalProps) {
    return (
        <View className={moreClassNames}>
            {React.Children.map(children, child => {
                if (React.isValidElement(child)) {
                    return React.cloneElement(child as React.ReactElement<any>, { onClose: onClose })
                }

                return child
            })}
        </View>
    )
}

function Header({ children, onClose }: ModalProps) {
    return (
        <View className="flex flex-row items-center justify-between pb-2 border-b-2 border-gray-200 mb-2">
            <Text className="text-sm">{children}</Text>
            <Pressable onPress={onClose} className="p-1">
                <FontAwesome onPress={() => { }} name="close" size={16} className="icon text-red-600" />
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
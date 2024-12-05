import { PropsWithChildren, useState } from "react";
import { Button, Modal } from "~/components";
import { Text, View } from "react-native";
import { CheckBox, Icon } from '@rneui/themed';
import React from "react";
import { useForm } from "react-hook-form";
import { Foundation } from "@expo/vector-icons";

type ShelvesStockingConfirmModalProps = PropsWithChildren<{ isShow: boolean, onClose: () => void, modalHeaderLabel: string, data?: object }>


export default function ShelvesStockingConfirmModal({ isShow, onClose, data, modalHeaderLabel }: ShelvesStockingConfirmModalProps) {
    const initialOptions = ['NEW ARRIVALS, FROM INVENTORY TO SHELVES', 'EXPIRED PRODUCTS, FROM SHELVES TO INVENTORY', 'slow-moving products', 'event-specific products'];
    const [autoArrangeOptions, setAutoArrangeOptions] = useState(initialOptions);

    const handleAutoArrangeOptionsSelection = (option: string) => {
        setAutoArrangeOptions(autoArrangeOptions.map(item => {
            if (item === option) {
                return item === item.toLowerCase() ? item.toUpperCase() : item.toLowerCase();
            }
            return item;
        }))
    }

    return (
        <Modal isShow={isShow} onClose={() => { setAutoArrangeOptions(initialOptions); onClose() }}>
            <Modal.Content
                onClose={onClose}
                moreClassNames="flex flex-col px-5 py-3 bg-white rounded-lg gap-y-5 "
            >
                <Modal.Header onClose={onClose} modalHeaderLabel={modalHeaderLabel} >
                    <Foundation name="refresh" size={24} color="black" onPress={() => setAutoArrangeOptions(initialOptions)} />
                </Modal.Header>
                <Modal.Body>
                    <View style={{ flexDirection: 'column' }}>
                        {autoArrangeOptions.map((option, index) => (
                            <CheckBox checked={option != option.toLowerCase()} onPress={() => handleAutoArrangeOptionsSelection(option)} key={index} title={option.toLowerCase()} textStyle={{ textTransform: 'capitalize' }} />
                        ))
                        }
                    </View>
                    <View style={{ flexDirection: 'row', justifyContent: 'space-around', flexWrap: 'wrap' }}>
                        <Button style={{ marginBottom: 2, backgroundColor: 'lightgreen', width: '25%' }} onPress={() => alert('Hello')} moreContainerClassNames="py-3 px-2" moreTextClassNames="font-semibold text-center" isLoading={false} >Confirm All Options</Button>
                        <Button style={{ marginBottom: 2, paddingTop: '10%' }} onPress={() => { setAutoArrangeOptions(initialOptions); onClose() }} moreTextClassNames="font-semibold" isLoading={false} moreContainerClassNames="py-3 px-8" >Cancel</Button>
                        <Button style={{ marginBottom: 2, backgroundColor: 'lightgreen', width: '25%' }} onPress={() => alert('Hello')} moreContainerClassNames="py-3 px-2" moreTextClassNames="font-semibold text-center" isLoading={false} >Confirm Selected Options</Button>
                    </View>


                </Modal.Body>
            </Modal.Content>
        </Modal>
    )
}
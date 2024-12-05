import { PropsWithChildren, useState } from "react";
import { Button, HandleResponse, Modal } from "~/components";
import { Text, View } from "react-native";
import { CheckBox, Icon } from '@rneui/themed';
import React from "react";
import { useForm } from "react-hook-form";
import { Foundation } from "@expo/vector-icons";
import { useGetCompartmentsQuery, useStockShelvesAutomaticallyMutation } from "~/services";
import { useAppDispatch, useChangeRoute } from "~/hooks";
import { resetCartState } from "~/store";

type ShelvesStockingConfirmModalProps = PropsWithChildren<{ row?: number, isShow: boolean, onClose: () => void, modalHeaderLabel: string, data?: object }>


export default function ShelvesStockingConfirmModal({ row, isShow, onClose, data, modalHeaderLabel }: ShelvesStockingConfirmModalProps) {
    const initialOptions = ['NEW ARRIVALS, FROM INVENTORY TO SHELVES', 'EXPIRED PRODUCTS, FROM SHELVES TO INVENTORY', 'slow-moving products', 'event-specific products'];
    const [autoArrangeOptions, setAutoArrangeOptions] = useState(initialOptions);
    const [stockShelvesAutomatically, { isSuccess, isError }] = useStockShelvesAutomaticallyMutation()
    const changeRoute = useChangeRoute()
    const { refetch: refetchCompartments } = useGetCompartmentsQuery({ row: row });
    const dispatch = useAppDispatch()

    const handleAutoArrangeOptionsSelection = (option: string) => {
        setAutoArrangeOptions(autoArrangeOptions.map(item => {
            if (item === option) {
                return item === item.toLowerCase() ? item.toUpperCase() : item.toLowerCase();
            }
            return item;
        }))
    }

    const handleStockShelvesAllAutomatically = () => {
        stockShelvesAutomatically({
            body: {
                slowSellCheck: true,
                event: true
            }
        })
        setAutoArrangeOptions(initialOptions); onClose()
    }

    const handleStockShelvesSelectedAutomatically = () => {
        stockShelvesAutomatically({
            body: {
                slowSellCheck: autoArrangeOptions[2] !== initialOptions[2],
                event: autoArrangeOptions[3] !== initialOptions[3]
            }
        })
        setAutoArrangeOptions(initialOptions); onClose()
    }

    const onSuccess = () => {
        refetchCompartments()
        changeRoute({})
        dispatch(resetCartState())
    }

    return (
        <>
            {/*  Handle Login Response */}
            {(isSuccess || isError) && (
                <HandleResponse
                    isError={isError}
                    isSuccess={isSuccess}
                    // @ts-ignore data.data?.errors?.body[0]
                    error={'Abnormal!'}
                    message={`Stocking shelves successfully`}
                    onSuccess={onSuccess}
                />
            )}
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
                                <CheckBox disabled={index === 0 || index === 1} checked={option != option.toLowerCase()} onPress={() => handleAutoArrangeOptionsSelection(option)} key={index} title={option.toLowerCase()} textStyle={{ textTransform: 'capitalize' }} />
                            ))
                            }
                        </View>
                        <View style={{ flexDirection: 'row', justifyContent: 'space-around', flexWrap: 'wrap' }}>
                            <Button onPress={handleStockShelvesAllAutomatically} style={{ marginBottom: 2, backgroundColor: 'lightgreen', width: '25%' }} moreContainerClassNames="py-3 px-2" moreTextClassNames="font-semibold text-center" isLoading={false} >Confirm All Options</Button>
                            <Button style={{ marginBottom: 2, paddingTop: '10%' }} onPress={() => { setAutoArrangeOptions(initialOptions); onClose() }} moreTextClassNames="font-semibold" isLoading={false} moreContainerClassNames="py-3 px-8" >Cancel</Button>
                            <Button onPress={handleStockShelvesSelectedAutomatically} style={{ marginBottom: 2, backgroundColor: 'lightgreen', width: '25%' }} moreContainerClassNames="py-3 px-2" moreTextClassNames="font-semibold text-center" isLoading={false} >Confirm Selected Options</Button>
                        </View>


                    </Modal.Body>
                </Modal.Content>
            </Modal>
        </>
    )
}
import { FontAwesome, Foundation } from "@expo/vector-icons"
import { RootState } from "@reduxjs/toolkit/query"
import { RouteParamInput, Routes, useLocalSearchParams } from "expo-router"
import React, { memo } from "react"
import { Pressable, View, Text, TextInput } from "react-native"
import { useSafeAreaInsets } from "react-native-safe-area-context"
import { shallowEqual } from "react-redux"
import { Icons, Modal } from "~/components"
import { useAppDispatch, useAppSelector, useChangeRoute, useDidMountEffect, useDisclosure } from "~/hooks"
import { FilterState, setKeepFilterModalOpen } from "~/store"

const Filter = memo(() => {

    //? Assets

    const [isFilters, filtersHandlers] = useDisclosure()
    const insets = useSafeAreaInsets()
    const params = useLocalSearchParams();
    const maxPrice = params?.maxPrice?.toString() ?? ''
    const minPrice = params?.minPrice?.toString() ?? ''
    const maxQuantity = params?.maxQuantity?.toString() ?? ''
    const minQuantity = params?.minQuantity?.toString() ?? ''
    const changeRoute = useChangeRoute();

    //? Handlers
    const handlefilter = ({ name, value, type }: { name: string, value: any, type: string }) => {
        const filterValue = value
        changeRoute({ [name]: filterValue })
    }

    const handleResetAllFilters = () => {
        changeRoute({ maxPrice: '', minPrice: '', minQuantity: '', maxQuantity: '' })
    }

    return (
        <>
            <View className=" px-3">
                <Pressable className="flex flex-row items-center gap-x-1" onPress={() => filtersHandlers.open()}>
                    <Icons.Ionicons name="filter" size={16} className="text-neutral-600" />
                    <Text className="text-base text-neutral-600">Filter</Text>
                </Pressable>
                <Modal
                    isShow={isFilters === -111}
                    onClose={filtersHandlers.close}
                    animationIn="slideInRight"
                    animationOut="slideOutRight"
                >
                    <Modal.Content
                        onClose={filtersHandlers.close}
                        style={{ paddingTop: insets.top }}
                        moreClassNames="flex flex-col h-[100vh] w-[80vw] px-5 ml-[15vw] bg-white"
                    >
                        <Modal.Header onClose={filtersHandlers.close} modalHeaderLabel="Filter" >
                            <View className="flex flex-row gap-2">
                                <Foundation onPress={() => handleResetAllFilters()} name="refresh" size={24} className="icon text-blue-600" />
                                <FontAwesome onPress={() => filtersHandlers.close()} name="close" size={24} className="icon text-red-600" />
                            </View>
                        </Modal.Header>
                        <Modal.Body>
                            <View className="divide-y">
                                <View className="py-2">
                                    <View className="flex flex-row justify-between">
                                        <Text className="font-medium text-gray-700">Price</Text>
                                    </View>
                                    <View className="flex flex-row items-center justify-between gap-x-1">
                                        <Text className="text-base">From</Text>

                                        <TextInput
                                            className="w-3/4 px-1 text-xl text-left border-b border-gray-200 outline-none"
                                            keyboardType="number-pad"
                                            placeholder={`0.25`}
                                            placeholderTextColor={"grey"}
                                            value={`${minPrice}`}
                                            key={"minPrice"}
                                            onChangeText={value => handlefilter({ name: 'minPrice', type: 'input', value })}
                                        />
                                        <Text className="w-6 h-6">$</Text>
                                    </View>
                                    <View className="flex flex-row items-center justify-between mt-2 mb-4 gap-x-1">
                                        <Text className="text-base">To</Text>

                                        <TextInput
                                            className="w-3/4 px-1 text-xl text-left border-b border-gray-200 outline-none"
                                            keyboardType="number-pad"
                                            placeholder={`999 999`}
                                            placeholderTextColor={"grey"}
                                            value={`${maxPrice}`}
                                            key="maxPrice"
                                            onChangeText={value => handlefilter({ name: 'maxPrice', type: 'input', value })}
                                        />

                                        <Text className="w-6 h-6">$</Text>
                                    </View>
                                </View>
                                <View className="py-4">
                                    <View className="flex flex-row justify-between">
                                        <Text className="font-medium text-gray-700">Quantity</Text>
                                    </View>
                                    <View className="flex flex-row items-center justify-between gap-x-1">
                                        <Text className="text-base">From</Text>

                                        <TextInput
                                            className="w-3/4 px-1 text-xl text-left border-b border-gray-200 outline-none"
                                            keyboardType="number-pad"
                                            placeholder={`1`}
                                            placeholderTextColor={"grey"}
                                            value={`${minQuantity}`}
                                            key={"minQuantity"}
                                            onChangeText={value => handlefilter({ name: 'minQuantity', type: 'input', value })}
                                        />
                                        <Text className="w-11 h-6">item(s)</Text>
                                    </View>
                                    <View className="flex flex-row items-center justify-between mt-2 mb-4 gap-x-1">
                                        <Text className="text-base">To</Text>

                                        <TextInput
                                            className="w-3/4 px-1 text-xl text-left border-b border-gray-200 outline-none"
                                            keyboardType="number-pad"
                                            placeholder={`9999`}
                                            placeholderTextColor={"grey"}
                                            value={`${maxQuantity}`}
                                            key="maxQuantity"
                                            onChangeText={value => handlefilter({ name: 'maxQuantity', type: 'input', value })}
                                        />

                                        <Text className="w-11 h-6">item(s)</Text>
                                    </View>
                                </View>
                            </View>
                        </Modal.Body>
                    </Modal.Content>
                </Modal>
            </View>
        </>
    )
})
export default Filter;
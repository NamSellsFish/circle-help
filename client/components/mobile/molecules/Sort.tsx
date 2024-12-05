import { RouteParamInput, Routes, useLocalSearchParams } from "expo-router";
import { memo, useState } from "react";
import { Pressable, View, Text } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { Icons, Modal } from "~/components";
import { useChangeRoute, useDidMountEffect, useDisclosure } from "~/hooks";
import { SORT_OPTIONS } from '~/constants'
import React from "react";
import { FontAwesome, Foundation } from "@expo/vector-icons";
//import { handleChangeRoute } from "~/utils";

const Sort = memo(() => {
    //? Assets
    const [isSort, sortHandlers] = useDisclosure()
    const insets = useSafeAreaInsets()
    const [sortOptions, setSortOptions] = useState(SORT_OPTIONS)
    const params = useLocalSearchParams()
    const changeRoute = useChangeRoute()

    //? Handlers
    const handleSortChange = (item: any) => {
        const tempSortOptions = [...sortOptions]
        if (item === 'reset') {
            tempSortOptions[0].value = ''
            tempSortOptions[0].name = ''
            setSortOptions(tempSortOptions)
            return
        }

        if (tempSortOptions[0].name !== item.name || tempSortOptions[0].value === '') {
            tempSortOptions[0].value = 'asc'
            tempSortOptions[0].name = item.name
            setSortOptions(tempSortOptions)
            return
        }
        switch (tempSortOptions[0].value) {
            case 'asc':
                tempSortOptions[0].value = 'desc'
                setSortOptions(tempSortOptions)
                break
            case 'desc':
                tempSortOptions[0].value = ''
                tempSortOptions[0].name = ''
                setSortOptions(tempSortOptions)
                break
        }


    }

    useDidMountEffect(() => {
        changeRoute(
            {
                sortCol: sortOptions[0].name.toLowerCase().split(' ').map((word, index) => index === 0 ? word : word.charAt(0).toUpperCase() + word.slice(1)).join(''),
                sortOption: sortOptions[0].value,
                page: sortOptions[0].value === '' ? '' : params.page
            })
    }, [sortOptions])


    return (
        <>
            <View className=" px-3">
                <Pressable className="flex flex-row items-center gap-x-1" onPress={() => sortHandlers.open()}>
                    <Icons.FontAwesome5 name="sort-amount-down-alt" size={16} className="text-neutral-600" />
                    <Text className="text-base text-neutral-600">Sort {sortOptions[0].value !== '' ? `By ${sortOptions[0].name}` : ''}</Text>
                </Pressable>
                <Modal
                    isShow={isSort === -111}
                    onClose={sortHandlers.close}
                    animationIn="slideInRight"
                    animationOut="slideOutRight"
                >
                    <Modal.Content
                        onClose={sortHandlers.close}
                        style={{ paddingTop: insets.top }}
                        moreClassNames="flex flex-col h-[100vh] w-[80vw] px-5 ml-[15vw] bg-white"
                    >
                        <Modal.Header onClose={sortHandlers.close} modalHeaderLabel="Sort by" >
                            <View className="flex flex-row gap-2">
                                <Foundation onPress={() => handleSortChange('reset')} name="refresh" size={24} className="icon text-blue-600" />
                                <Foundation onPress={() => alert(JSON.stringify(params))} name="info" size={24} className="icon text-yellow-600" />
                                <FontAwesome onPress={() => sortHandlers.close()} name="close" size={24} className="icon text-red-600" />
                            </View>
                        </Modal.Header>
                        <Modal.Body>
                            <View className="divide-y divide-neutral-300">
                                {sortOptions.slice(1).map((item, i) => (
                                    <Pressable
                                        onPress={() => {
                                            handleSortChange(item)
                                        }}
                                        key={i}
                                        className="flex flex-row items-center justify-between"
                                    >
                                        <Pressable className="block py-3 text-left text-gray-700" key="sort">
                                            <Text>{item.name}</Text>
                                        </Pressable>
                                        {sortOptions[0].name === item.name && sortOptions[0].value === 'asc' && <Icons.Entypo name="arrow-with-circle-up" size={16} className="icon" />}
                                        {sortOptions[0].name === item.name && sortOptions[0].value === 'desc' && <Icons.Entypo name="arrow-with-circle-down" size={16} className="icon" />}
                                    </Pressable>
                                ))}
                            </View>
                        </Modal.Body>
                    </Modal.Content>
                </Modal>
            </View>
        </>
    )
}
)


export default Sort;
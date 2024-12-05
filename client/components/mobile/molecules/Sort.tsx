import { useLocalSearchParams } from "expo-router";
import { useState } from "react";
import { Pressable, View, Text } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { Icons, Modal } from "~/components";
import { useDisclosure } from "~/hooks";

export default function Sort() {
    //? Assets
    const [isSort, sortHandlers] = useDisclosure()
    const params = useLocalSearchParams()
    const insets = useSafeAreaInsets()

    //? State
    const [sort, setSort] = useState(sorts[0])

    //? Handlers
    const handleSortChange = item => {
        setSort(sorts[item.value - 1])
        handleChangeRoute({ sort: item.value })
        // @ts-ignore
        sortHandlers.close()
    }

    return (
        <>
            <View className=" px-3">
                <Pressable className="flex flex-row items-center gap-x-1" onPress={sortHandlers.open}>
                    <Icons.FontAwesome5 name="sort-amount-down-alt" size={16} className="text-neutral-600" />
                    <Text className="text-base text-neutral-600">{sort?.name}</Text>
                </Pressable>
                <Modal
                    isShow={isSort}
                    onClose={sortHandlers.close}
                    animationIn="slideInRight"
                    animationOut="slideOutRight"
                >
                    <Modal.Content
                        onClose={sortHandlers.close}
                        style={{ paddingTop: insets.top }}
                        moreClassNames="flex flex-col h-[100vh] w-[80vw] px-5 ml-[15vw] bg-white"
                    >
                        <Modal.Header onClose={sortHandlers.close}>Sort</Modal.Header>
                        <Modal.Body>
                            <View className="divide-y divide-neutral-300">
                                {sorts.map((item, i) => (
                                    <Pressable
                                        onPress={() => handleSortChange(item)}
                                        key={i}
                                        className="flex flex-row items-center justify-between"
                                    >
                                        <View className="block py-3 text-left text-gray-700" type="button" name="sort">
                                            <Text>{item.name}</Text>
                                        </View>
                                        {sort?.value === item.value && (
                                            <Icons.AntDesign name="checkcircleo" size={16} className="icon" />
                                        )}
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